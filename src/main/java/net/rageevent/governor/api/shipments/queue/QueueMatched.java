package net.rageevent.governor.api.shipments.queue;

import net.rageevent.governor.api.UserNotice;
import net.rageevent.myriad.server.ServerType;

import java.util.UUID;

public record QueueMatched(UUID player, ServerType queue, String server) implements UserNotice {
}
