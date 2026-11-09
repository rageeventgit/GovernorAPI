package net.rageevent.governor.api.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.rageevent.myriad.api.server.LobbyType;
import net.rageevent.myriad.api.server.ServerType;

import java.io.IOException;
import java.util.Optional;

public class ServerTypeDeserializer extends StdDeserializer<ServerType> {

    protected ServerTypeDeserializer() {
        super(ServerType.class);
    }


    @Override
    public ServerType deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);

        if(!node.isTextual()) return LobbyType.MAIN;

        Optional<ServerType> serverType = ServerType.TYPES.stream().filter(type -> type.id().equalsIgnoreCase(node.asText())).findFirst();
        return serverType.orElse(LobbyType.MAIN);
    }
}
