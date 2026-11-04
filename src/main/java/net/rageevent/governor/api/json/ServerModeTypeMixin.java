package net.rageevent.governor.api.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = ServerModeDeserializer.class)
public interface ServerModeTypeMixin {


}
