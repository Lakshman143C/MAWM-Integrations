package com.floordecor.inbound.service;

import com.floordecor.inbound.dto.prop.CustomPropConfigDto;
import com.floordecor.inbound.entity.prop.CustomPropConfig;
import com.floordecor.inbound.repository.CustomPropConfigRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomPropConfigService {

    @Autowired
    CustomPropConfigRepository customPropConfigRepository;

    public CustomPropConfig save(CustomPropConfig customPropConfig) {
        return customPropConfigRepository.save(customPropConfig);
    }

    public List<CustomPropConfigDto> updateOrSave(List<CustomPropConfigDto> customPropConfigDtos) {
        List<CustomPropConfig> updatedConfigs =
                customPropConfigDtos.stream()
                        .flatMap(
                                dto ->
                                        dto.getProperties().entrySet().stream()
                                                .map(
                                                        entry -> {
                                                            String key = entry.getKey();
                                                            String value = entry.getValue();
                                                            return customPropConfigRepository
                                                                    .findByConfigIdAndPropertyKey(dto.getConfigId(), key)
                                                                    .map(
                                                                            existingConfig -> {
                                                                                existingConfig.setPropertyValue(value);
                                                                                return existingConfig;
                                                                            })
                                                                    .orElseGet(
                                                                            () -> {
                                                                                CustomPropConfig newConfig = new CustomPropConfig();
                                                                                newConfig.setConfigId(dto.getConfigId());
                                                                                newConfig.setPropertyKey(key);
                                                                                newConfig.setPropertyValue(value);
                                                                                return newConfig;
                                                                            });
                                                        }))
                        .collect(Collectors.toList());
        customPropConfigRepository.saveAll(updatedConfigs);
        return customPropConfigDtos;
    }

    public List<CustomPropConfigDto> getAllConfigProperties() {
        List<CustomPropConfig> allCustomPropConfigs = customPropConfigRepository.findAll();
        // Group CustomPropConfig entities by ConfigId and aggregate properties
        Map<String, CustomPropConfigDto> aggregatedConfigs = new HashMap<>();
        allCustomPropConfigs.forEach(
                config -> {
                    String configId = config.getConfigId();
                    Map<String, String> entityProperties =
                            Map.of(config.getPropertyKey(), config.getPropertyValue());
                    CustomPropConfigDto dto =
                            aggregatedConfigs.computeIfAbsent(
                                    configId,
                                    k -> {
                                        CustomPropConfigDto newDto = new CustomPropConfigDto();
                                        newDto.setConfigId(configId);
                                        newDto.setProperties(new HashMap<>());
                                        return newDto;
                                    });
                    entityProperties.forEach(
                            (key, value) -> {
                                dto.getProperties().merge(key, value, (v1, v2) -> v1 + "," + v2);
                            });
                });

        return new ArrayList<>(aggregatedConfigs.values());
    }

    public CustomPropConfigDto getConfigPropertiesByConfigId(String configId) {
        Optional<List<CustomPropConfig>> customPropConfig =
                customPropConfigRepository.findByConfigId(configId);
        // Create a new CustomPropConfigDto to aggregate properties
        CustomPropConfigDto aggregatedDto = new CustomPropConfigDto();
        aggregatedDto.setConfigId(configId);
        aggregatedDto.setProperties(new HashMap<>());

        // If the list is empty, return an empty list or handle as needed.
        if (!customPropConfig.isPresent() || customPropConfig.get().isEmpty()) {
            return aggregatedDto;
        }
        // Aggregate properties from all CustomPropConfig entities
        customPropConfig
                .get()
                .forEach(
                        config -> {
                            Map<String, String> entityProperties =
                                    Map.of(config.getPropertyKey(), config.getPropertyValue());
                            entityProperties.forEach(
                                    (key, value) -> {
                                        aggregatedDto.getProperties().merge(key, value, (v1, v2) -> v1 + "," + v2);
                                    });
                        });
        return aggregatedDto;
    }

    public void deletePropertyByConfigIdAndPropertyKey(String configId, String key) {
        customPropConfigRepository.deleteByConfigIdAndPropertyKey(configId, key);
    }

    public CustomPropConfigDto entityToDto(CustomPropConfig customPropConfig) {
        CustomPropConfigDto customPropConfigDto =
                CustomPropConfigDto.builder()
                        .configId(customPropConfig.getConfigId())
                        .properties(
                                Map.ofEntries(
                                        Map.entry(
                                                customPropConfig.getPropertyKey(), customPropConfig.getPropertyValue())))
                        .build();
        return customPropConfigDto;
    }
}
