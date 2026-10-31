package net.rageevent.governor.api.protocol;

import lombok.RequiredArgsConstructor;
import net.rageevent.governor.api.Envelope;
import net.rageevent.governor.api.Mailbox;
import net.rageevent.governor.api.Transaction;
import net.rageevent.governor.api.exceptions.GovernorException;
import net.rageevent.governor.api.shipments.network.NetworkMailbox;
import net.rageevent.governor.api.shipments.queue.QueueMailbox;
import net.rageevent.governor.api.shipments.system.SystemMailbox;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EnvelopeRegistry {

    public record Key(Mailbox mailbox, int id) {
        @Override
        public String toString() {
            return mailbox + "#" + id;
        }
    }

    private final ConcurrentMap<Key, Class<? extends Envelope>> types = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Key> keys = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<?>, Class<? extends Envelope>> responseTypes = new ConcurrentHashMap<>();

    public static EnvelopeRegistry standard() {
        EnvelopeRegistry envelopeRegistry = new EnvelopeRegistry();
        SystemMailbox.register(envelopeRegistry);
        QueueMailbox.register(envelopeRegistry);
        NetworkMailbox.register(envelopeRegistry);

        return envelopeRegistry;
    }

    public synchronized EnvelopeRegistry register(Mailbox mailbox, int id, Class<? extends Envelope> envelope) {
        Objects.requireNonNull(mailbox, "serverType");
        Objects.requireNonNull(envelope, "type");

        String simpleName = envelope.getSimpleName();

        if (id < 0)
            throw new IllegalArgumentException("Envelope IDs must be bigger than 0. ($simpleName | $id)");

        if (envelope.isInterface() || Modifier.isAbstract(envelope.getModifiers()))
            throw new IllegalArgumentException("$simpleName must be a concrete class, usually a record.");

        Key key = new Key(mailbox, id);
        Class<? extends Envelope> taken = types.get(key);

        if (taken != null)
            throw new IllegalArgumentException("Envelope ID: $id is already taken by ${taken.getSimpleName()}.");

        Key existing = keys.get(envelope);
        if (existing != null)
            throw new IllegalArgumentException("${envelope.getSimpleName()} is already registered as $existing");

        if (Transaction.class.isAssignableFrom(envelope))
            responseTypes.put(envelope, resolveResponseType(envelope));

        types.put(key, envelope);
        keys.put(envelope, key);
        return this;
    }

    public MailboxBuilder mailbox(Mailbox mailbox) {
        return new MailboxBuilder(mailbox);
    }

    @RequiredArgsConstructor
    public final class MailboxBuilder {
        private final Mailbox mailbox;

        public MailboxBuilder add(int id, Class<? extends Envelope> envelope) {
            register(mailbox, id, envelope);
            return this;
        }
    }

    public Key keyOf(Class<?> type) {
        Key key = keys.get(type);
        if (key == null)
            throw new IllegalArgumentException("${type.getSimpleName()} is not a registered envelope, add it to its mailboxes catalog.");
        return key;
    }

    public Class<? extends Envelope> typeOf(int mailboxId, int id) {
        Mailbox mailbox = Mailbox.byId(mailboxId);
        Class<? extends Envelope> type = mailbox == null ? null : types.get(new Key(mailbox, id));
        if (type == null)
            throw new GovernorException("Unknown parcel: Mailbox: $mailbox, Id: $id");

        return type;
    }

    public Class<? extends Envelope> responseTypeOf(Class<?> envelopeType) {
        Class<? extends Envelope> transactionType = responseTypes.get(envelopeType);

        if (transactionType == null)
            throw new IllegalArgumentException("${envelopeType.getSimpleName()} is not a registered transaction.");
        return transactionType;
    }

    public void validate() {
        List<String> problems = responseTypes.entrySet().stream()
                .filter(entry -> !keys.containsKey(entry.getValue()))
                .map(entry -> "${entry.getKey().getName()} responds with ${entry.getValue().getSimpleName()}, which is not registered")
                .sorted()
                .toList();
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Parcel registry is incomplete: " + String.join("; ", problems));
        }
    }

    private Class<? extends Envelope> resolveResponseType(Class<?> requestType) {
        for (Class<?> type = requestType; type != null && type != Object.class; type = type.getSuperclass()) {
            for (Type implemented : type.getGenericInterfaces()) {
                if (implemented instanceof ParameterizedType parameterized
                        && parameterized.getRawType() == Transaction.class
                        && parameterized.getActualTypeArguments()[0] instanceof Class<?> response) {
                    return response.asSubclass(Envelope.class);
                }
            }
        }
        throw new IllegalArgumentException("${requestType.getSimpleName()} must implement Request<SomeResponse> directly, with a concrete response type");
    }
}
