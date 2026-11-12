package net.rageevent.governor.api.shipments.party.repsonse;

import net.rageevent.governor.api.Envelope;
import net.rageevent.myriad.api.player.UserRank;

import java.util.List;
import java.util.UUID;

public record ListPartyMembersResponse(UUID player, List<PartyMember> members) implements Envelope {

    public record PartyMember(UUID player, String name, UserRank rank){
    }
}
