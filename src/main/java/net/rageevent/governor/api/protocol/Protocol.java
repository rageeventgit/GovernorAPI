package net.rageevent.governor.api.protocol;

import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public class Protocol {

    public final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

}
