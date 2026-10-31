package net.rageevent.governor.api.protocol.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.EnvelopeConnection;
import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.exceptions.EnvelopeTimeoutException;
import net.rageevent.governor.api.exceptions.GovernorException;
import net.rageevent.governor.api.protocol.frame.EnvelopeFrame;
import net.rageevent.governor.api.protocol.EnvelopeRegistry;
import net.rageevent.governor.api.protocol.SerialExecutor;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

final class ChannelEnvelopeConnection implements EnvelopeConnection {

    private static final System.Logger LOG = System.getLogger(ChannelEnvelopeConnection.class.getName());

    private record PendingRequest(CompletableFuture<Envelope> future, Class<? extends Envelope> responseType,
                                  String requestName) {
    }

    private final Channel channel;
    private final EnvelopeRegistry registry;
    private final Executor executor;
    private final SerialExecutor orderedExecutor;
    private final AtomicLong nextRequestId = new AtomicLong();
    private final ConcurrentMap<Long, PendingRequest> pending = new ConcurrentHashMap<>();

    private volatile String name;

    ChannelEnvelopeConnection(Channel channel, EnvelopeRegistry registry, Executor executor) {
        this.channel = channel;
        this.registry = registry;
        this.executor = executor;
        this.orderedExecutor = new SerialExecutor(executor);
    }

    @Override
    public String name() {
        return name;
    }

    boolean identified() {
        return name != null;
    }

    void identify(String name) {
        this.name = name;
    }

    @Override
    public SocketAddress remote() {
        return channel.remoteAddress();
    }

    @Override
    public boolean active() {
        return channel.isActive();
    }

    @Override
    public void send(Envelope envelope) {
        Objects.requireNonNull(envelope);
        if (envelope instanceof Transaction<?>)
            throw new IllegalArgumentException("${envelope.getClass().getSimpleName()} is a transaction, use request() so its response has somewhere to go.");

        registry.keyOf(envelope.getClass());
        channel.writeAndFlush(EnvelopeFrame.message(envelope)).addListener(write -> {
            if (!write.isSuccess())
                LOG.log(System.Logger.Level.WARNING, "Could not send ${envelope.getClass().getSimpleName()} to $this");
        });

    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Envelope> CompletableFuture<E> request(Transaction<E> transaction, Duration timeout) {
        Objects.requireNonNull(transaction);
        Objects.requireNonNull(timeout);
        Class<? extends Envelope> responseType = registry.responseTypeOf(transaction.getClass());
        String requestName = transaction.getClass().getSimpleName();

        CompletableFuture<E> future = new CompletableFuture<>();

        if (!channel.isActive()) {
            future.completeExceptionally(new GovernorException("Cannot send $requestName: to connection $this is closed."));
            return future;
        }
        long requestId = nextRequestId.incrementAndGet();
        CompletableFuture<Envelope> untyped = (CompletableFuture<Envelope>) future;
        pending.put(requestId, new PendingRequest(untyped, responseType, requestName));

        ScheduledFuture<?> timer = channel.eventLoop().schedule(
                () -> fail(requestId, new EnvelopeTimeoutException("$requestName to $this got no response within ${timeout.toMillis()} ms.")),
                timeout.toNanos(), TimeUnit.NANOSECONDS);
        future.whenComplete((response, error) -> {
            timer.cancel(false);
            pending.remove(requestId);
        });

        channel.writeAndFlush(EnvelopeFrame.request(requestId, transaction)).addListener(write -> {
            if (!write.isSuccess())
                fail(requestId, new GovernorException("Could not send $requestName to $this", write.cause()));
        });

        return future;
    }


    @Override
    public void close() {
        channel.close();
    }

    void complete(long requestId, Envelope response) {
        PendingRequest pendingRequest = pending.remove(requestId);
        if (pendingRequest == null) {
            LOG.log(System.Logger.Level.DEBUG, "Ignoring response to transaction $requestId from $this: it already finished.");
            return;
        }

        if (!pendingRequest.responseType().isInstance(response)) {
            deliver(pendingRequest, null,
                    new GovernorException("${pendingRequest.requestName()} expected ${pendingRequest.responseType().getSimpleName()} but $this answered with ${response.getClass().getSimpleName()}"));
            return;
        }
        deliver(pendingRequest, response, null);
    }

    void fail(long requestId, Throwable error) {
        PendingRequest request = pending.remove(requestId);
        if (request != null) deliver(request, null, error);
    }

    void failAll(Throwable error) {
        for (Long requestId : pending.keySet()) {
            fail(requestId, error);
        }
    }

    void executeOrdered(Runnable task) {
        orderedExecutor.execute(task);
    }

    void execute(Runnable task) {
        executor.execute(task);
    }

    ChannelFuture response(long requestId, Transaction<?> transaction, Envelope response, Throwable error) {
        String requestName = transaction.getClass().getSimpleName();
        if (error == null && response == null)
            error = new GovernorException("Agent for $requestName returned null");

        if (error == null) {
            Class<? extends Envelope> expected = registry.responseTypeOf(transaction.getClass());
            if (!expected.isInstance(response))
                error = new GovernorException("Agent for $requestName returned ${response.getClass().getSimpleName()} instead of ${response.getClass().getSimpleName()}.");
        }

        if (error != null) {
            Throwable cause = GovernorException.unwrap(error);

            if (cause instanceof GovernorException) {
                LOG.log(System.Logger.Level.WARNING, "$requestName from $this failed: ${cause.getMessage()}");
            } else
                LOG.log(System.Logger.Level.WARNING, "$requestName from $this failed", cause);

            return respondError(requestId,describe(cause));
        }
        EnvelopeFrame frame = EnvelopeFrame.response(requestId,response);

        return channel.writeAndFlush(frame).addListener(write -> {
            if(write.isSuccess()) return;
            LOG.log(System.Logger.Level.WARNING,"Could not answer $requestName from $this",write.cause());
            if(channel.isActive()) respondError(requestId,"Could not send the response: ${describe(write.cause())}");
        });
    }

    ChannelFuture respondError(long requestId, String message) {
        return channel.writeAndFlush(EnvelopeFrame.error(requestId, message));
    }

    private void deliver(PendingRequest request, Envelope response, Throwable error) {
        Runnable completion = error == null
                ? () -> request.future().complete(response)
                : () -> request.future().completeExceptionally(error);
        try {
            executor.execute(completion);
        } catch (RejectedExecutionException shuttingDown) {
            completion.run();
        }
    }

    private static String describe(Throwable error) {
        String message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        return error instanceof GovernorException ? message : error.getClass().getSimpleName() + ": " + message;
    }

    @Override public String toString() {
        return name != null ? name : String.valueOf(channel.remoteAddress());
    }
}
