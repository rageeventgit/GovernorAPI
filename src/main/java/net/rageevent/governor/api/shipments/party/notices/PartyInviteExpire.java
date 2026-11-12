package net.rageevent.governor.api.shipments.party.notices;

import net.rageevent.governor.api.UserNotice;

import java.util.UUID;

public record PartyInviteExpire(UUID player, UUID target, String targetName) implements UserNotice {
}
