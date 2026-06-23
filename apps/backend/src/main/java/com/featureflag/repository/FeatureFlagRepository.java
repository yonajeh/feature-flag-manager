package com.featureflag.repository;

import com.featureflag.domain.FeatureFlag;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FeatureFlagRepository implements PanacheRepositoryBase<FeatureFlag, UUID> {

    public List<FeatureFlag> findByApplicationId(UUID applicationId) {
        return list("applicationId", applicationId);
    }

    public Optional<FeatureFlag> findByApplicationIdAndKey(UUID applicationId, String key) {
        return find("applicationId = ?1 and key = ?2", applicationId, key).firstResultOptional();
    }
}
