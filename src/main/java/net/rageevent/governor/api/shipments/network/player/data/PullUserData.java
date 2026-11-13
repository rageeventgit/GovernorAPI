package net.rageevent.governor.api.shipments.network.player.data;

import net.rageevent.governor.api.Transaction;
import net.rageevent.myriad.api.player.store.DataContainer;

import java.util.UUID;

public record PullUserData(UUID player, String section, boolean override) implements Transaction<UserDataResponse> {

    public PullUserData(UUID player, DataContainer specific, boolean override) {
        this(player, specific.dotNotation(), override);
    }

}
