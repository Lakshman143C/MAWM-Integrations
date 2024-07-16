package com.floordecor.inbound.customConfig.controller;


import com.floordecor.inbound.customConfig.dto.CustomPropConfigDto;
import com.floordecor.inbound.customConfig.service.CustomPropConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/custom/config")
@Tag(
        name = "CustomPropConfigController Controller",
        description = "CustomPropConfigController related APIs")
public class CustomPropConfigController {
    @Autowired
    CustomPropConfigService customPropConfigService;

    @GetMapping("/get")
    public ResponseEntity<List<CustomPropConfigDto>> getAllConfigProperties() {
        return ResponseEntity.ok(customPropConfigService.getAllConfigProperties());
    }

    @GetMapping("/get/{configId}")
    public ResponseEntity<CustomPropConfigDto> getConfigPropertiesByConfigId(
            @PathVariable("configId") String configId) {
        return ResponseEntity.ok(customPropConfigService.getConfigPropertiesByConfigId(configId));
    }

    @PostMapping("/save")
    public ResponseEntity<List<CustomPropConfigDto>> saveConfigProperties(
            @RequestBody List<CustomPropConfigDto> customPropConfig) {
        return ResponseEntity.ok(customPropConfigService.updateOrSave(customPropConfig));
    }

    @DeleteMapping("/delete/{configId}/{propertyKey}")
    public ResponseEntity<String> deletePropertyByConfigIdAndPropertyKey(
            @PathVariable("configId") String configId, @PathVariable("propertyKey") String propertyKey) {
        customPropConfigService.deletePropertyByConfigIdAndPropertyKey(configId, propertyKey);
        return ResponseEntity.ok(
                String.format("Property %s deleted successfully from configId %s", propertyKey, configId));
    }
}
