package com.featureflag.repository;

import com.featureflag.domain.Application;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ApplicationRepository implements PanacheRepositoryBase<Application, UUID> {

    public Optional<Application> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}
