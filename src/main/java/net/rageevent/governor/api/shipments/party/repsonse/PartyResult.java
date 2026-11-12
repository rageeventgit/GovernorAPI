package net.rageevent.governor.api.shipments.party.repsonse;

import net.rageevent.governor.api.Envelope;

import java.util.UUID;

public record PartyResult(Status status, UUID sender, UUID player, long expiresInSeconds) implements Envelope {

    public enum Status{
        NOT_ONLINE,
        ALREADY_IN_PARTY
    }

}
