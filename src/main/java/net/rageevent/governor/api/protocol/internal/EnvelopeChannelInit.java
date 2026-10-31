package net.rageevent.governor.api.protocol.internal;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import net.rageevent.governor.api.protocol.EnvelopeEndpoint;
import net.rageevent.governor.api.protocol.Protocol;
import net.rageevent.governor.api.protocol.frame.EnvelopeFrameCodec;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class EnvelopeChannelInit extends ChannelInitializer<SocketChannel> {

    private final EnvelopeEndpoint endpoint;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline()
                .addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Protocol.MAX_FRAME_BYTES, 0, 4, 0, 4))
                .addLast("frameEncoder", new LengthFieldPrepender(4))
                .addLast("idle", new IdleStateHandler(Protocol.IDLE_TIMEOUT.toMillis(), Protocol.HEARTBEAT_INTERVAL.toMillis(), 0, TimeUnit.MILLISECONDS))
                .addLast("parcelCodec", new EnvelopeFrameCodec(endpoint.registry()))
                .addLast("dispatcher", new EnvelopeDispatcher(endpoint));
    }
}
