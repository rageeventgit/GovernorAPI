package net.rageevent.governor.api.shipments.system;

import net.rageevent.governor.api.Envelope;

public record PingResponse(long sentAt, String responder) implements Envelope {

    public long roundTripMillis() {
        return System.currentTimeMillis() - sentAt;
    }

}
