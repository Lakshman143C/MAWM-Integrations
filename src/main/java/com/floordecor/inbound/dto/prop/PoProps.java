package com.floordecor.inbound.dto.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;

@Data
@Component
@ConfigurationProperties("props")
public class PoProps implements Serializable {

    private EntityCreateProp entityCreateEvent;

    @Data
    public static class EntityCreateProp extends HashMap<String, EntityCreate> {}


    @Data
    public static class EntityCreate {
        private Boolean enabled;
        private String queue;
    }
}
