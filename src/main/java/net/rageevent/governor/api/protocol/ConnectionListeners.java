package net.rageevent.governor.api.protocol;

import net.rageevent.governor.api.EnvelopeConnection;
import net.rageevent.governor.api.shipments.system.HandshakeRequest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ConnectionListeners implements ConnectionListener {

    private static final System.Logger LOGGER = System.getLogger(ConnectionListener.class.getName());

    private final List<ConnectionListener> listeners = new CopyOnWriteArrayList<>();

    public void add(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public String handshake(EnvelopeConnection connection, HandshakeRequest handshake) {
        for (ConnectionListener listener : listeners) {
            try {
                String reason = listener.handshake(connection, handshake);
                if (reason != null)
                    return reason;
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Connection listener failed during handshake of ${handshake.name()}", e);
                return "Hub error while checking the handshake";
            }
        }
        return null;
    }

    @Override
    public void ready(EnvelopeConnection connection) {
        each(listener -> listener.ready(connection));
    }

    @Override
    public void disconnect(EnvelopeConnection connection) {
        each(listener -> listener.disconnect(connection));
    }

    @Override
    public void rejected(EnvelopeConnection connection, String reason) {
        each(listener -> listener.rejected(connection,reason));
    }

    private void each(Consumer<ConnectionListener> listenerConsumer) {
        for (ConnectionListener listener : listeners) {
            try {
                listenerConsumer.accept(listener);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Connection listener failed", e);
            }
        }
    }
}
