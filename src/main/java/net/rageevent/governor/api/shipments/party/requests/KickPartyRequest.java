package net.rageevent.governor.api.shipments.party.requests;

import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.shipments.party.repsonse.KickPartyResponse;

import java.util.UUID;

public record KickPartyRequest(UUID sender, UUID receiver) implements Transaction<KickPartyResponse> {
}
