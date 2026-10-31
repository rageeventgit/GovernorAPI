package net.rageevent.governor.api.shipments.queue;

import net.rageevent.governor.api.Transaction;
import net.rageevent.myriad.server.ServerType;

import java.util.UUID;

public record JoinQueueRequest(UUID player, ServerType queue) implements Transaction<QueueResult> {
}
