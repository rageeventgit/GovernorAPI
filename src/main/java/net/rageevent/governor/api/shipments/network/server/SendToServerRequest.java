package net.rageevent.governor.api.shipments.network.server;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.shipments.network.player.TransferResult;

import java.util.UUID;

public record SendToServerRequest(UUID player, String server) implements Transaction<TransferResult> {
}
