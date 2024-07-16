package com.floordecor.inbound.repository;

import com.floordecor.inbound.entity.prop.CustomPropConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomPropConfigRepository extends JpaRepository<CustomPropConfig, String> {

    Optional<List<CustomPropConfig>> findByConfigId(String configId);

    Optional<CustomPropConfig> findByConfigIdAndPropertyKey(String configId, String key);

    void deleteByConfigIdAndPropertyKey(String configId, String key);
}
