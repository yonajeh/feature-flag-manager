package com.featureflag.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_tokens")
public class ApiToken extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "token_hash", nullable = false, length = 64)
    public String tokenHash;

    @Column(name = "token_prefix", nullable = false, length = 8)
    public String tokenPrefix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public ApiTokenStatus status;

    @Column(name = "last_used_at")
    public Instant lastUsedAt;

    @Column(name = "expires_at")
    public Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "revoked_at")
    public Instant revokedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }
}
