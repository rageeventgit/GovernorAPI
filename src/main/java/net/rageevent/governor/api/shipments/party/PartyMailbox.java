package net.rageevent.governor.api.shipments.party;

import net.rageevent.governor.api.Mailbox;
import net.rageevent.governor.api.protocol.EnvelopeRegistry;
import net.rageevent.governor.api.shipments.party.notices.PartyRequestAnswered;
import net.rageevent.governor.api.shipments.party.repsonse.KickPartyResponse;
import net.rageevent.governor.api.shipments.party.repsonse.LeavePartyResponse;
import net.rageevent.governor.api.shipments.party.repsonse.ListPartyMembersResponse;
import net.rageevent.governor.api.shipments.party.repsonse.PartyResult;
import net.rageevent.governor.api.shipments.party.requests.KickPartyRequest;
import net.rageevent.governor.api.shipments.party.requests.LeavePartyRequest;
import net.rageevent.governor.api.shipments.party.requests.ListPartyMembersRequest;
import net.rageevent.governor.api.shipments.party.requests.SendPartyRequest;

public class PartyMailbox {

    public static void register(EnvelopeRegistry registry) {
        registry.mailbox(Mailbox.PARTY)
                .add(0, SendPartyRequest.class)
                .add(1, PartyResult.class)
                .add(2, LeavePartyRequest.class)
                .add(3, LeavePartyResponse.class)
                .add(4, KickPartyRequest.class)
                .add(5, KickPartyResponse.class)
                .add(6, ListPartyMembersRequest.class)
                .add(7, ListPartyMembersResponse.class)
                .add(8, PartyRequestAnswered.class)
        ;
    }

}
