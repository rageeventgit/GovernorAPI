package net.rageevent.governor.api.shipments.party.requests;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.shipments.party.repsonse.ListPartyMembersResponse;

import java.util.UUID;

public record ListPartyMembersRequest(UUID sender) implements Transaction<ListPartyMembersResponse> {
}
