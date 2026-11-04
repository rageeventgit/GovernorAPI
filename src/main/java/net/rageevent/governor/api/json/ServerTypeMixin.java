package net.rageevent.governor.api.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ServerTypeDeserializer.class)
public interface ServerTypeMixin {



}
