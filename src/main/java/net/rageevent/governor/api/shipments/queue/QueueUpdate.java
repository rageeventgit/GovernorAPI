package net.rageevent.governor.api.shipments.queue;

import net.rageevent.governor.api.UserNotice;
import net.rageevent.myriad.api.server.ServerModeType;

import java.util.UUID;

public record QueueUpdate(UUID player, ServerModeType queue, int position, int size) implements UserNotice {
}
