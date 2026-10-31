package net.rageevent.governor.api.shipments.queue;

import net.rageevent.governor.api.Transaction;
import net.rageevent.myriad.server.ServerType;

import java.util.UUID;

public record LeaveQueueRequest(UUID player) implements Transaction<QueueResult> {
}
