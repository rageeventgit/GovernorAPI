package net.rageevent.governor.api;

@FunctionalInterface
public interface EnvelopeCarrier {

    void register(EnvelopeAgent agent);

    default void close(){

    }

}
