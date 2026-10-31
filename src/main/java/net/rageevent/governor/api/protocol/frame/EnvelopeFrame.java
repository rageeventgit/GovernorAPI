package net.rageevent.governor.api.protocol.frame;

import io.netty.handler.codec.CorruptedFrameException;
import net.rageevent.governor.api.Envelope;

public record EnvelopeFrame(EnvelopeType type, long requestId, Envelope envelope, String error) {

    public static EnvelopeFrame message(Envelope envelope){
        return new EnvelopeFrame(EnvelopeType.MESSAGE,0,envelope,null);
    }
    public static EnvelopeFrame request(long requestId, Envelope envelope){
        return new EnvelopeFrame(EnvelopeType.REQUEST,requestId,envelope,null);
    }
    public static EnvelopeFrame response(long requestId, Envelope envelope){
        return new EnvelopeFrame(EnvelopeType.RESPONSE,requestId,envelope,null);
    }
    public static EnvelopeFrame error(long requestId, String error){
        return new EnvelopeFrame(EnvelopeType.ERROR,requestId,null,error);
    }

    public enum EnvelopeType {
        MESSAGE,
        REQUEST,
        RESPONSE,
        ERROR; //VERY BAD, DON'T WANT.

        private static final EnvelopeType[] TYPES = values();

        public byte code() {
            return (byte) ordinal();
        }

        public static EnvelopeType fromCode(byte code) {
            if ((code < 0 || code >= TYPES.length))
                throw new CorruptedFrameException("Unknown frame kind: $code");
            return TYPES[code];
        }
    }


}
