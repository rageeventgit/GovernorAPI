package net.rageevent.governor.api.shipments.party.repsonse;

import net.rageevent.governor.api.UserNotice;

import java.util.UUID;

public record LeavePartyResponse(Status status, UUID player) implements UserNotice {

    public enum Status{
        LEFT_SUCCESSFULLY,
        NOT_IN_PARTY
    }

}
