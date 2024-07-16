package com.floordecor.inbound.customConfig.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

import com.floordecor.inbound.entity.base.BaseEntity;
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
