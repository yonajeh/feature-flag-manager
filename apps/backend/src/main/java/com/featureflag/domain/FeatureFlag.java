package com.featureflag.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "feature_flags")
public class FeatureFlag extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(name = "application_id", nullable = false)
    public UUID applicationId;

    @Column(name = "flag_key", nullable = false, length = 128, updatable = false)
    public String key;

    @Column(nullable = false)
    public boolean enabled;

    @Column
    public String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "TEXT")
    public Map<String, Object> metadata;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
