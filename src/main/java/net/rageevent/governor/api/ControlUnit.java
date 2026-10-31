package net.rageevent.governor.api;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.rageevent.governor.api.exceptions.GovernorException;
import net.rageevent.governor.api.protocol.*;
import net.rageevent.governor.api.protocol.internal.EnvelopeChannelInit;
import net.rageevent.governor.api.server.ServerProfile;
import net.rageevent.governor.api.server.ServerState;

import javax.naming.ldap.Control;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class ControlUnit implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(ControlUnit.class.getName());

    public record Connection(String name, String host, int port) {
        public Connection {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be blank");
            Objects.requireNonNull(host, "host");
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Port out of range: $port");
        }
    }

    private final Connection target;
    private final ServerProfile profile;
    private final EnvelopeRegistry registry;
    private final EnvelopeAgent agent = new EnvelopeAgent();
    private final List<EnvelopeCarrier> carriers = new CopyOnWriteArrayList<>();
    private final ConnectionListeners listeners = new ConnectionListeners();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private volatile Duration reconnectionDelay = Duration.ofSeconds(5);
    private volatile EnvelopeConnection connection;
    private CompletableFuture<EnvelopeConnection> ready = new CompletableFuture<>();


    private boolean readySettled;
    private boolean closed;
    private EventLoopGroup group;
    private Bootstrap bootstrap;

    public ControlUnit(Connection target, ServerProfile profile, EnvelopeRegistry registry) {
        this.target = target;
        this.profile = profile;
        this.registry = registry;
    }

    public ControlUnit(Connection target, ServerProfile profile) {
        this(target, profile, EnvelopeRegistry.standard());
    }

    public synchronized CompletableFuture<EnvelopeConnection> connect() {
        if (closed) throw new IllegalArgumentException("This ControlUnit is closed.");
        if (bootstrap == null) {
            registry.validate();
            group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
            EnvelopeEndpoint endpoint = new EnvelopeEndpoint(EnvelopeEndpoint.Role.CLIENT, target.name(), profile, registry, agent, new ClientListener(), executor);

            bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new EnvelopeChannelInit(endpoint));
            attemptConnect();
        }
        return ready;
    }

    public ControlUnit install(EnvelopeCarrier carrier) {
        agent.install(carrier);
        carriers.add(carrier);
        return this;
    }

    public ControlUnit listen(ConnectionListener listener) {
        listeners.add(listener);
        return this;
    }

    public void updateState(ServerState state, int maxPlayers) {
    }

    public void deliver(Envelope envelope) {
        current().send(envelope);
    }

    public <E extends Envelope> CompletableFuture<E> ship(Transaction<E> request) {
        return ship(request, Protocol.DEFAULT_TIMEOUT);
    }

    public <E extends Envelope> CompletableFuture<E> ship(Transaction<E> request, Duration timeout) {
        EnvelopeConnection current = connection;
        if (current == null) return CompletableFuture.failedFuture(notConnected());
        return current.request(request, timeout);
    }

    @Override
    public void close() {
        EventLoopGroup groupToStop;
        synchronized (this) {
            if (closed) return;
            closed = true;
            readySettled = true;
            ready.completeExceptionally(new GovernorException("ControlUnit closed"));
            groupToStop = group;
        }
        EnvelopeConnection current = connection;
        if (current != null) current.close();
        if (groupToStop != null) groupToStop.shutdownGracefully(0, 2, TimeUnit.SECONDS).syncUninterruptibly();
        executor.shutdown();
        for (EnvelopeCarrier carrier : carriers) {
            try {
                carrier.close();
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "Carrier ${carrier.getClass().getSimpleName()} failed to close", e);
            }
        }
    }

    public ServerProfile profile() {
        return profile;
    }

    public ControlUnit reconnectDelay(Duration delay) {
        this.reconnectionDelay = delay;
        return this;
    }

    public EnvelopeAgent agent() {
        return agent;
    }

    public boolean connected() {
        return connection != null && connection.active();
    }

    private EnvelopeConnection current() {
        EnvelopeConnection current = connection;
        if (current == null) throw notConnected();
        return current;
    }

    private void attemptConnect() {
        bootstrap.connect(target.host(), target.port()).addListener((ChannelFuture attempt) -> {
            if (!attempt.isSuccess()) {
                LOGGER.log(System.Logger.Level.WARNING, "Could not reach Governor at ${target.host()}:${target.port()}: ${attempt.cause().getMessage()}");
                retryOrGiveUp(attempt.cause());
            }
        });
    }

    private GovernorException notConnected() {
        return new GovernorException("Not connected to Governor at ${target.host()}:${target.port()}");
    }

    private synchronized void retryOrGiveUp(Throwable cause) {
        if (closed) return;
        Duration delay = reconnectionDelay;
        if (delay == null || delay.isZero() || delay.isNegative()) {
            settleReady(null, new GovernorException("Could not connect to Governor at ${target.host()}:${target.port()}", cause));
            return;
        }
        try {
            group.schedule(this::attemptConnect, delay.toMillis(), TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException shuttingDown) {
            // close() won the race.
        }
    }

    private synchronized void settleReady(EnvelopeConnection value, Throwable error) {

        if (readySettled) return;
        readySettled = true;
        CompletableFuture<EnvelopeConnection> future = ready;
        Runnable completion = error == null ? () -> future.complete(value) : () -> future.completeExceptionally(error);
        try {
            executor.execute(completion);
        } catch (RejectedExecutionException shuttingDown) {
            completion.run();
        }
    }

    private final class ClientListener implements ConnectionListener {

        @Override
        public void ready(EnvelopeConnection freshConnection) {
            connection = freshConnection;
            LOGGER.log(System.Logger.Level.INFO, "Connected to Governor '${freshConnection.name()}' as ${target.name()}");
            listeners.ready(freshConnection);
            settleReady(freshConnection, null);
        }

        @Override
        public void rejected(EnvelopeConnection connection, String reason) {
            LOGGER.log(System.Logger.Level.WARNING, "Governor turned '${target.name()} away: $reason'");
            listeners.rejected(connection, reason);
            settleReady(null, new GovernorException("Turned away ${target.name()} for: $reason"));
        }

        @Override
        public void disconnect(EnvelopeConnection lostConnection) {
            boolean intentional;
            synchronized (ControlUnit.this) {
                intentional = closed;
                if (!closed && readySettled) {
                    ready = new CompletableFuture<>();
                    readySettled = false;
                }
            }
            if (connection == lostConnection) {
                connection = null;
                LOGGER.log(intentional ? System.Logger.Level.INFO : System.Logger.Level.WARNING,
                        (intentional ? "Disconnected from" : "Lost connection to") + " Governor '${lostConnection.name()}'");
                listeners.disconnect(lostConnection);
            }
            if (intentional) return;
            retryOrGiveUp(new GovernorException("Connection to Governor closed"));
        }
    }
}
