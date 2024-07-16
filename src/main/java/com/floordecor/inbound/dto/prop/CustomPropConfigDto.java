package com.floordecor.inbound.dto.prop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.floordecor.inbound.dto.mms.BaseEntity;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomPropConfigDto extends BaseEntity {
    @JsonIgnore private Long id;

    @JsonProperty("ConfigId")
    private String configId;

    private Map<String, String> properties;
}
