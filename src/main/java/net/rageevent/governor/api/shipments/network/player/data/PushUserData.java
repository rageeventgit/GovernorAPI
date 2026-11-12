package net.rageevent.governor.api.shipments.network.player.data;

import com.fasterxml.jackson.databind.JsonNode;
import net.rageevent.governor.api.Transaction;

import java.util.UUID;

public record PushUserData(UUID player, JsonNode data) implements Transaction<UserDataResponse> {
}
