package net.rageevent.governor.api.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.rageevent.myriad.server.LobbyType;
import net.rageevent.myriad.server.ServerModeType;
import net.rageevent.myriad.server.ServerType;

import java.io.IOException;
import java.util.Optional;

public class ServerModeDeserializer extends StdDeserializer<ServerModeType> {

    protected ServerModeDeserializer() {
        super(ServerType.class);
    }


    @Override
    public ServerModeType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);

        if (!node.isTextual()) return null;
        Optional<ServerModeType> serverType = ServerModeType.TYPES.stream().filter(type -> type.name().equalsIgnoreCase(node.asText())).findFirst();
        return serverType.orElse(null);
    }
}
