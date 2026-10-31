package net.rageevent.governor.api;

import net.rageevent.governor.api.protocol.Protocol;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface EnvelopeConnection {

    String name();

    SocketAddress remote();

    boolean active();

    void send(Envelope envelope);

    default <E extends Envelope> CompletableFuture<E> request(Transaction<E> transaction) {
        return request(transaction, Protocol.DEFAULT_TIMEOUT);
    }

    <E extends Envelope> CompletableFuture<E> request(Transaction<E> transaction, Duration timeout);

    void close();
}
