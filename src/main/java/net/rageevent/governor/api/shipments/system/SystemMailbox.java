package net.rageevent.governor.api.shipments.system;

import lombok.experimental.UtilityClass;
import net.rageevent.governor.api.Mailbox;
import net.rageevent.governor.api.protocol.EnvelopeRegistry;

@UtilityClass
public class SystemMailbox {

    public static void register(EnvelopeRegistry registry) {
        registry.mailbox(Mailbox.SYSTEM)
                .add(0, HandshakeRequest.class)
                .add(1, HandshakeResponse.class)
                .add(2, PingRequest.class)
                .add(3, PingResponse.class);
    }

}
