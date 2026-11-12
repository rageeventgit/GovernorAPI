package net.rageevent.governor.api.shipments.party.requests;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.shipments.party.repsonse.PartyResult;

import java.util.UUID;

public record SendPartyRequest(UUID sender, UUID receiver) implements Transaction<PartyResult> {
}
