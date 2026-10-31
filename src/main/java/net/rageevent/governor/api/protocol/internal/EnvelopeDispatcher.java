package net.rageevent.governor.api.protocol.internal;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.exceptions.EnvelopeException;
import net.rageevent.governor.api.exceptions.GovernorException;
import net.rageevent.governor.api.shipments.system.HandshakeRequest;
import net.rageevent.governor.api.shipments.system.HandshakeResponse;
import net.rageevent.governor.api.shipments.system.PingRequest;
import net.rageevent.governor.api.shipments.system.PingResponse;
import net.rageevent.governor.api.protocol.EnvelopeEndpoint;
import net.rageevent.governor.api.protocol.Protocol;
import net.rageevent.governor.api.protocol.frame.EnvelopeFrame;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;

@RequiredArgsConstructor
public class EnvelopeDispatcher extends SimpleChannelInboundHandler<EnvelopeFrame> {

    private static final System.Logger LOGGER = System.getLogger(EnvelopeDispatcher.class.getName());

    private final EnvelopeEndpoint endpoint;
    private ChannelEnvelopeConnection connection;


    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        connection = new ChannelEnvelopeConnection(context.channel(), endpoint.registry(), endpoint.executor());

        if (endpoint.role() == EnvelopeEndpoint.Role.CLIENT) {
            connection.request(new HandshakeRequest(Protocol.VERSION, endpoint.localName(), endpoint.profile()), Protocol.HANDSHAKE_TIMEOUT)
                    .whenComplete((_, error) -> {
                        if (error == null) return;
                        LOGGER.log(System.Logger.Level.WARNING, "Handshake with $connection failed: ${GovernorException.unwrap(error).getMessage()}");
                        connection.close();
                    });
        }

        super.channelActive(context);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        connection.failAll(new GovernorException("Connection to $connection closed."));
        endpoint.listener().disconnect(connection);
        super.channelInactive(context);
    }

    @Override
    @SneakyThrows
    protected void channelRead0(ChannelHandlerContext context, EnvelopeFrame frame) {
        switch (frame.type()) {
            case RESPONSE -> {
                if (endpoint.role() == EnvelopeEndpoint.Role.CLIENT && frame.envelope() instanceof HandshakeResponse response)
                    handshakeAnswered(response);
                connection.complete(frame.requestId(), frame.envelope());
            }
            case ERROR -> connection.fail(frame.requestId(), new EnvelopeException("$connection: ${frame.error()}"));
            case MESSAGE -> onMessage(frame.envelope());
            case REQUEST -> onRequest(frame.requestId(), frame.envelope());
        }
    }

    private void onMessage(Envelope parcel) {
        String envelopeName = parcel.getClass().getSimpleName();
        if (!handshakeDone()) {
            LOGGER.log(System.Logger.Level.WARNING, "Dropped $envelopeName from $connection: handshake not finished");
            return;
        }
        submit(true, () -> {
            try {
                if (!endpoint.agent().handleMessage(connection, parcel)) {
                    LOGGER.log(System.Logger.Level.WARNING, "No handler for $envelopeName from " + connection);
                }
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Handler for $envelopeName from $connection failed", e);
            }
        });
    }

    private void onRequest(long requestId, Envelope parcel) {
        if (!(parcel instanceof Transaction<?> request)) {
            connection.respondError(requestId, "${parcel.getClass().getSimpleName()} was sent as a request but is not a Request");
            return;
        }
        if (request instanceof HandshakeRequest handshake && endpoint.role() == EnvelopeEndpoint.Role.SERVER) {
            handshake(requestId, handshake);
            return;
        }
        if (request instanceof PingRequest ping) {
            connection.response(requestId, ping, new PingResponse(ping.sentAt(), endpoint.localName()), null);
            return;
        }
        if (!handshakeDone()) {
            connection.respondError(requestId, "Handshake required before ${request.getClass().getSimpleName()}");
            return;
        }
        submit(false, () -> {
            CompletionStage<? extends Envelope> result;
            try {
                result = endpoint.agent().handleTransaction(connection, request);
            } catch (Throwable error) {
                result = CompletableFuture.failedFuture(error);
            }
            result.whenComplete((response, error) -> connection.response(requestId, request, response, error));
        });
    }

    private void handshake(long requestId, HandshakeRequest handshake) {
        String reason;
        if (connection.identified()) {
            reason = "Already identified as " + connection.name();
        } else if (handshake.protocolVersion() != Protocol.VERSION) {
            reason = "Protocol version mismatch: hub speaks ${Protocol.VERSION}, server speaks ${handshake.protocolVersion()}";
        } else if (handshake.name() == null || handshake.name().isBlank()) {
            reason = "Server name must not be blank";
        } else if (handshake.profile() == null) {
            reason = "Server profile is missing";
        } else {
            reason = endpoint.listener().handshake(connection, handshake);
        }

        if (reason != null) {
            LOGGER.log(System.Logger.Level.WARNING, "Turned away '${handshake.name()}' (${connection.remote()}): $reason");
            connection.response(requestId, handshake, HandshakeResponse.rejected(reason), null)
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        connection.identify(handshake.name());
        connection.response(requestId, handshake, HandshakeResponse.accepted(endpoint.localName()), null);
        endpoint.listener().ready(connection);
    }

    private void handshakeAnswered(HandshakeResponse response) {
        if (connection.identified()) return;
        if (!response.accepted()) {
            endpoint.listener().rejected(connection, response.reason());
            connection.close();
        } else {
            connection.identify(response.hub());
            endpoint.listener().ready(connection);
        }
    }

    private boolean handshakeDone() {
        return endpoint.role() == EnvelopeEndpoint.Role.CLIENT || connection.identified();
    }

    private void submit(boolean ordered, Runnable task) {
        try {
            if (ordered) connection.executeOrdered(task);
            else connection.execute(task);
        } catch (RejectedExecutionException e) {
            LOGGER.log(System.Logger.Level.DEBUG, "Dropped work from $connection: shutting down");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof IdleStateEvent idle) {
            if (idle.state() == IdleState.READER_IDLE) {
                LOGGER.log(System.Logger.Level.WARNING, "$connection sent nothing for ${Protocol.IDLE_TIMEOUT.toSeconds()}s, closing");
                ctx.close();
            } else if (idle.state() == IdleState.WRITER_IDLE && connection.identified()) {
                connection.request(PingRequest.now()).exceptionally(error -> null);
            }
            return;
        }
        super.userEventTriggered(ctx, event);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Object who = connection != null ? connection : ctx.channel().remoteAddress();
        if (cause instanceof IOException) {
            LOGGER.log(System.Logger.Level.INFO, "Connection to $who lost: ${cause.getMessage()}");
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "Closing connection to $who after an error", cause);
        }
        ctx.close();
    }
}
