package net.rageevent.governor.api.shipments.network.player.data;

import com.fasterxml.jackson.databind.JsonNode;
import net.rageevent.governor.api.Envelope;

public record UserDataResponse(String message, JsonNode data) implements Envelope {
    public UserDataResponse(String message){
        this(message,null);
    }
}
