package net.rageevent.governor.api.protocol;

import io.netty.bootstrap.ServerBootstrap;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class SerialExecutor implements Executor {

    private static final System.Logger LOG = System.getLogger(SerialExecutor.class.getName());

    private final Executor delegate;
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean draining = new AtomicBoolean();

    public SerialExecutor(Executor delegate) {
        this.delegate = delegate;
    }

    @Override public void execute(Runnable command) {
        tasks.add(command);
    }

    private void scheduleDrain() {
        if (!draining.compareAndSet(false, true)) return;

        delegate.execute(this::drain);
    }

    private void drain() {
        try {
            Runnable task;
            while ((task = tasks.poll()) != null) {
                try {
                    task.run();
                } catch (Throwable error) {
                    LOG.log(System.Logger.Level.WARNING, "Task failed", error);
                }
            }

        } finally {
            draining.set(false);
            if (!tasks.isEmpty()) scheduleDrain();
        }
    }
}
