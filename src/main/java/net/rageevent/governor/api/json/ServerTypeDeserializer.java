package net.rageevent.governor.api.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.rageevent.myriad.server.GameType;
import net.rageevent.myriad.server.LobbyType;
import net.rageevent.myriad.server.ServerType;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
