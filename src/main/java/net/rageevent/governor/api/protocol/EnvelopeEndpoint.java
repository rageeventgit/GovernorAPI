package net.rageevent.governor.api.protocol;

import net.rageevent.governor.api.EnvelopeAgent;
import net.rageevent.governor.api.server.ServerProfile;

import java.util.concurrent.Executor;

public record EnvelopeEndpoint(Role role, String localName, ServerProfile profile, EnvelopeRegistry registry,
                               EnvelopeAgent agent,
                               ConnectionListener listener, Executor executor) {

    public enum Role {
        CLIENT,
        SERVER
    }

}
