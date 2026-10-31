package net.rageevent.governor.api.shipments.network;

import lombok.experimental.UtilityClass;
import net.rageevent.governor.api.Mailbox;
import net.rageevent.governor.api.protocol.EnvelopeRegistry;
import net.rageevent.governor.api.shipments.network.player.*;
import net.rageevent.governor.api.shipments.network.server.*;

@UtilityClass
public class NetworkMailbox {

    public void register(EnvelopeRegistry registry){
        registry.mailbox(Mailbox.NETWORK)
                .add(0, FindPlayerRequest.class)
                .add(1, FindPlayerResult.class)
                .add(2, PlayerJoinedNetwork.class)
                .add(3, PlayerLeftNetwork.class)
                .add(4, PlayerSwitchedServer.class)
                .add(5, ProxyRoster.class)
                .add(6,TransferPlayerRequest.class)
                .add(7, TransferResult.class)
                .add(8, RegisterGameServer.class)
                .add(9, RemoveGameServer.class)
                .add(10, SendToGroupRequest.class)
                .add(11, SendToServerRequest.class)
                .add(12, UpdateServerState.class);
    }

}
