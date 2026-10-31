package net.rageevent.governor.api.protocol.frame;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.RequiredArgsConstructor;
import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.protocol.EnvelopeRegistry;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
public class EnvelopeFrameCodec extends MessageToMessageCodec<ByteBuf, EnvelopeFrame> {

    private static final System.Logger LOGGER = System.getLogger(EnvelopeFrameCodec.class.getName());

    static final ObjectMapper JSON = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    private final EnvelopeRegistry registry;

    @Override
    protected void encode(ChannelHandlerContext context, EnvelopeFrame frame, List<Object> out) throws Exception {
        ByteBuf buffer = context.alloc().buffer();
        try {
            EnvelopeFrame.EnvelopeType envelopeType = frame.type();
            buffer.writeLong(envelopeType.code());
            buffer.writeLong(frame.requestId());

            if (envelopeType == EnvelopeFrame.EnvelopeType.ERROR) {
                buffer.writeCharSequence(frame.error(), StandardCharsets.UTF_8);
            } else {
                EnvelopeRegistry.Key key = registry.keyOf(frame.envelope().getClass());
                buffer.writeInt(key.mailbox().id());
                buffer.writeInt(key.id());
                buffer.writeBytes(JSON.writeValueAsBytes(frame.envelope()));
            }
        } catch (Throwable error) {
            buffer.release();
            throw error;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buffer, List<Object> out) {
        EnvelopeFrame.EnvelopeType type = EnvelopeFrame.EnvelopeType.fromCode(buffer.readByte());
        long requestId = buffer.readLong();

        if (type == EnvelopeFrame.EnvelopeType.ERROR) {
            out.add(EnvelopeFrame.error(requestId, buffer.toString(StandardCharsets.UTF_8)));
            return;
        }
        int groupId = buffer.readInt();
        int packetId = buffer.readInt();
        Envelope envelope;
        try {
            envelope = JSON.readValue((InputStream) new ByteBufInputStream(buffer), registry.typeOf(groupId, packetId));
        } catch (Exception e) {
            String problem = "Could not decode parcel (ServerType $groupId, Id $packetId): ${e.getMessage()}";
            LOGGER.log(System.Logger.Level.WARNING, "$type from ${context.channel().remoteAddress()} dropped. $problem");

            switch (type) {
                case REQUEST -> context.channel().writeAndFlush(EnvelopeFrame.error(requestId, problem));
                case RESPONSE -> out.add(EnvelopeFrame.error(requestId, problem));
                default -> {

                }
            }

            return;
        }
        out.add(new EnvelopeFrame(type, requestId, envelope, null));
    }
}
