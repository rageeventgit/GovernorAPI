package net.rageevent.governor.api.shipments.party.repsonse;

import net.rageevent.governor.api.Envelope;

import java.util.UUID;

public record KickPartyResponse(LeavePartyResponse.Status status, UUID sender, UUID player) implements Envelope {


}
