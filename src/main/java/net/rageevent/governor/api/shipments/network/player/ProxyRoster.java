package net.rageevent.governor.api.shipments.network.player;

import net.rageevent.governor.api.Envelope;

import java.util.List;
import java.util.UUID;

public record ProxyRoster(List<UserEntry> players) implements Envelope {

    public record UserEntry(UUID player, String name, String server){
    }
    public ProxyRoster{
        players = players == null ? List.of() : List.copyOf(players);
    }

}
