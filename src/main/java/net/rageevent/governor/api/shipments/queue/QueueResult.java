package net.rageevent.governor.api.shipments.queue;

import net.rageevent.governor.api.Envelope;
import net.rageevent.myriad.server.ServerType;

public record QueueResult(Status status, ServerType queue, int position, int size) implements Envelope {

    public enum Status {
        JOINED,
        ALREADY_QUEUED,
        LEFT,
        NOT_QUEUED,
        /**
         * No server of that serverType is connected to the hub.
         */
        UNKNOWN_QUEUE,
        NOT_ONLINE
    }

}
