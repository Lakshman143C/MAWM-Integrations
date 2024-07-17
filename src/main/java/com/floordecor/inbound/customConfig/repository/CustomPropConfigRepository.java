package com.floordecor.inbound.customConfig.repository;


import com.floordecor.inbound.customConfig.entity.CustomPropConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomPropConfigRepository extends JpaRepository<CustomPropConfig, String> {

    Optional<List<CustomPropConfig>> findByConfigId(String configId);

    Optional<CustomPropConfig> findByConfigIdAndPropertyKey(String configId, String key);

    void deleteByConfigIdAndPropertyKey(String configId, String key);
}
