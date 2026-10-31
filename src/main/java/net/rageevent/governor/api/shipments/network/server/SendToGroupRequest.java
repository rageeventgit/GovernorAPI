package net.rageevent.governor.api.shipments.network.server;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.shipments.network.player.TransferResult;
import net.rageevent.myriad.server.ServerType;

import java.util.UUID;

public record SendToGroupRequest(UUID player, ServerType group) implements Transaction<TransferResult> {
}
