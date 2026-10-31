package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;

public record TransferResult(Status status, String server, String detail) implements Envelope {

    public enum Status{
        SUCCESS,
        ALREADY_CONNECTED,
        NOT_ONLINE,
        UNKNOWN_SERVER,

        NO_SERVER_AVAILABLE,
        FAILED
    }


}
