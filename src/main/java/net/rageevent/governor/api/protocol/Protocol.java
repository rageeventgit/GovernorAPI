package net.rageevent.governor.api.protocol;

import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public class Protocol {
    public static final int VERSION = 2;

    public static final int MAX_FRAME_BYTES = 10 * 1024 * 1024;

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration HANDSHAKE_TIMEOUT = Duration.ofSeconds(10);

    public static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(15);

    public static final Duration IDLE_TIMEOUT = Duration.ofSeconds(45);

}
