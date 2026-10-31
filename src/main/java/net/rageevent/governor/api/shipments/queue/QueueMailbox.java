package net.rageevent.governor.api.shipments.queue;

import lombok.experimental.UtilityClass;
import net.rageevent.governor.api.Mailbox;
import net.rageevent.governor.api.protocol.EnvelopeRegistry;
import net.rageevent.governor.api.shipments.system.HandshakeRequest;
import net.rageevent.governor.api.shipments.system.HandshakeResponse;
import net.rageevent.governor.api.shipments.system.PingRequest;
import net.rageevent.governor.api.shipments.system.PingResponse;

@UtilityClass
public class QueueMailbox {

    public static void register(EnvelopeRegistry registry) {
        registry.mailbox(Mailbox.QUEUE)
                .add(0, JoinQueueRequest.class)
                .add(1, LeaveQueueRequest.class)
                .add(2, QueueMatched.class)
                .add(3, QueueResult.class)
                .add(4, QueueUpdate.class)
        ;
    }

}
