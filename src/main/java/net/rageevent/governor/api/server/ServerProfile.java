package net.rageevent.governor.api.server;

import net.rageevent.myriad.server.ServerType;

import java.util.Objects;

public record ServerProfile(ServerRole role, ServerType serverType, String host, int port, int maxPlayers) {

    public ServerProfile {
        Objects.requireNonNull(role, "role");
        if (serverType == null && role != ServerRole.PROXY)
            throw new IllegalArgumentException("serverType must not be blank");
        if (maxPlayers < 0) throw new IllegalArgumentException("maxPlayers must be >= 0");
    }

    public static ServerProfile proxy() {
        return new ServerProfile(ServerRole.PROXY, null, null, 0, 0);
    }

    public static ServerProfile backend(ServerType serverType, String host, int port, int maxPlayers) {
        return new ServerProfile(ServerRole.GAME_SERVER, serverType, host, port, maxPlayers);
    }

    /*
        A game server that is already registered through its own config.
     */
    public static ServerProfile backend(ServerType serverType, int maxPlayers) {
        return new ServerProfile(ServerRole.GAME_SERVER, serverType, null, 0, maxPlayers);
    }
}
