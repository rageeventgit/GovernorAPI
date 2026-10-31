package net.rageevent.governor.api.shipments.system;

import net.rageevent.governor.api.Transaction;

public record PingRequest(long sentAt) implements Transaction<PingResponse> {

    public static PingRequest now() {
        return new PingRequest(System.currentTimeMillis());
    }

}
