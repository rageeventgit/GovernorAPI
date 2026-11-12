package net.rageevent.governor.api.shipments.party.requests;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.shipments.party.repsonse.LeavePartyResponse;

import java.util.UUID;

public record LeavePartyRequest(UUID player) implements Transaction<LeavePartyResponse> {
}
