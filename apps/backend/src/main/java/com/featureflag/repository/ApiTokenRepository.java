package com.featureflag.repository;

import com.featureflag.domain.ApiToken;
import com.featureflag.domain.ApiTokenStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ApiTokenRepository implements PanacheRepositoryBase<ApiToken, UUID> {

    public Optional<ApiToken> findActiveByHash(String tokenHash) {
        return find("tokenHash = ?1 and status = ?2", tokenHash, ApiTokenStatus.ACTIVE).firstResultOptional();
    }

    public List<ApiToken> findByApplicationId(UUID applicationId) {
        return list("applicationId", applicationId);
    }

    public Optional<ApiToken> findByIdAndApplicationId(UUID tokenId, UUID applicationId) {
        return find("id = ?1 and applicationId = ?2", tokenId, applicationId).firstResultOptional();
    }
}
