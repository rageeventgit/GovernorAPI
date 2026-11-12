package net.rageevent.governor.api.shipments.party.notices;

import net.rageevent.governor.api.UserNotice;

import java.util.UUID;

public record PartyRequestAnswered(UUID player, UUID sender,boolean accepted) implements UserNotice {
}
