package net.rageevent.governor.api;

import net.rageevent.governor.api.exceptions.GovernorException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class EnvelopeAgent {

    @FunctionalInterface
    public interface MessageAgent<E extends Envelope> {
        void handle(EnvelopeConnection connection, E envelope);
    }

    @FunctionalInterface
    public interface TransactionAgent<R extends Transaction<E>, E extends Envelope> {
        E handle(EnvelopeConnection connection, R transaction) throws Exception;
    }

    @FunctionalInterface
    public interface AsyncTransactionAgent<R extends Transaction<E>, E extends Envelope> {
        CompletionStage<E> handle(EnvelopeConnection connection, R transaction) throws Exception;
    }

    private final Map<Class<?>, MessageAgent<?>> messageHandles = new ConcurrentHashMap<>();
    private final Map<Class<?>, AsyncTransactionAgent<?, ?>> transactionHandles = new ConcurrentHashMap<>();

    public EnvelopeAgent install(EnvelopeCarrier carrier) {
        carrier.register(this);
        return this;
    }

    public <E extends Envelope> EnvelopeAgent on(Class<E> type, MessageAgent<E> agent) {
        if (Transaction.class.isAssignableFrom(type))
            throw new IllegalArgumentException(type.getSimpleName() + " is a Transaction. register it with onTransaction so sender gets a response.");
        storeAgent(messageHandles, type, agent);
        return this;
    }

    public <R extends Transaction<E>, E extends Envelope> EnvelopeAgent onTransaction(Class<R> type, TransactionAgent<R, E> agent) {
        Objects.requireNonNull(agent, "agent");
        return onAsyncTransaction(type, ((connection, transaction) -> CompletableFuture.completedFuture(agent.handle(connection, transaction))));
    }

    public <R extends Transaction<E>, E extends Envelope> EnvelopeAgent onAsyncTransaction(Class<R> type, AsyncTransactionAgent<R, E> agent) {
        storeAgent(transactionHandles, type, agent);
        return this;
    }

    @SuppressWarnings("unchecked")
    public boolean handleMessage(EnvelopeConnection connection, Envelope envelope) {
        MessageAgent<Envelope> handler = (MessageAgent<Envelope>) messageHandles.get(envelope.getClass());
        if (handler == null) return false;
        handler.handle(connection, envelope);
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CompletionStage<? extends Envelope> handleTransaction(EnvelopeConnection connection, Transaction<?> transaction) throws Exception {
        AsyncTransactionAgent agent = transactionHandles.get(transaction.getClass());
        if (agent == null)
            throw new GovernorException("Agent for " + transaction.getClass().getSimpleName() + " not found.");

        CompletionStage<? extends Envelope> result = agent.handle(connection, transaction);
        if (result == null)
            throw new GovernorException("Agent for " + transaction.getClass().getSimpleName() + " returned no future.");

        return result;
    }

    private <A> void storeAgent(Map<Class<?>, A> agents, Class<?> type, A agent) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(agent, "agent");
        if (agents.putIfAbsent(type, agent) != null)
            throw new IllegalArgumentException("Agent for " + type.getSimpleName() + " is already registered.");
    }
}
