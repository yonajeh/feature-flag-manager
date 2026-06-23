package com.featureflag.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("feature_flags_test")
            .withUsername("test")
            .withPassword("test");

    private static boolean started;

    @Override
    public synchronized Map<String, String> start() {
        if (!started) {
            POSTGRES.start();
            started = true;
        }
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.jdbc.url", POSTGRES.getJdbcUrl());
        config.put("quarkus.datasource.username", POSTGRES.getUsername());
        config.put("quarkus.datasource.password", POSTGRES.getPassword());
        config.put("quarkus.devui.enabled", "false");
        config.put("quarkus.test.dev-mode", "false");
        config.put("ff.token.pepper", "test-pepper");
        config.put("ff.admin.username", "admin");
        config.put("ff.admin.password", "admin");
        return config;
    }

    @Override
    public synchronized void stop() {
        // Keep container alive for the full test JVM; Ryuk cleans up on exit.
    }
}
