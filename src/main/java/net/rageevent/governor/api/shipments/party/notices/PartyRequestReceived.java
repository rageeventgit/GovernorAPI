package net.rageevent.governor.api.shipments.party.notices;

import net.rageevent.governor.api.UserNotice;

import java.util.UUID;

public record PartyRequestReceived(UUID player, UUID sender) implements UserNotice {
}
