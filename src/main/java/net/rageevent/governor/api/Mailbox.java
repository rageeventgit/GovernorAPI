package net.rageevent.governor.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum Mailbox {
    SYSTEM(0),
    NETWORK(1),
    QUEUE(2),

    ;
    private static final Map<Integer, Mailbox> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(Mailbox::id, Function.identity()));

    private final int id;

    public static Mailbox byId(int id) {
        return BY_ID.get(id);
    }
}
