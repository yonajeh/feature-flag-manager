package com.featureflag.testsupport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Starts the packaged Quarkus application with an embedded SQLite database for integration tests.
 */
public final class QuarkusJarTestResource {

    private static Process appProcess;
    private static Path dbFile;
    private static int httpPort = findFreePort();
    private static boolean started;

    private QuarkusJarTestResource() {}

    public static synchronized void start() throws Exception {
        if (started) {
            return;
        }
        dbFile = Files.createTempFile("ffm-it-", ".db");

        Path jar = Path.of("target/quarkus-app/quarkus-run.jar").toAbsolutePath();
        if (!jar.toFile().exists()) {
            throw new IllegalStateException("Package the application first: mvn package -DskipTests");
        }

        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome == null || javaHome.isBlank()) {
            javaHome = System.getProperty("java.home");
        }

        List<String> command = new ArrayList<>();
        command.add(Path.of(javaHome, "bin", "java").toString());
        command.add("-Dquarkus.profile=prod");
        command.add("-Dquarkus.http.port=" + httpPort);
        command.add("-Dquarkus.datasource.jdbc.url=jdbc:sqlite:" + dbFile.toAbsolutePath());
        command.add("-Dff.admin.username=admin");
        command.add("-Dff.admin.password=admin");
        command.add("-Dff.token.pepper=test-pepper");
        command.add("-jar");
        command.add(jar.toString());

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        appProcess = builder.start();
        drainOutput(appProcess);

        waitForHealth();
        started = true;
    }

    public static synchronized void stop() {
        if (appProcess != null && appProcess.isAlive()) {
            appProcess.destroy();
            try {
                appProcess.waitFor(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                appProcess.destroyForcibly();
            }
        }
        if (dbFile != null) {
            try {
                Files.deleteIfExists(dbFile);
            } catch (IOException ignored) {
                // temp file cleanup best-effort
            }
        }
        started = false;
    }

    public static String baseUrl() {
        return "http://localhost:" + httpPort;
    }

    private static void drainOutput(Process process) {
        Thread thread = new Thread(() -> {
            try {
                process.getInputStream().transferTo(System.out);
            } catch (IOException ignored) {
                // process ended
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static void waitForHealth() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/q/health/ready"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        long deadline = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < deadline) {
            if (!appProcess.isAlive()) {
                throw new IllegalStateException("Quarkus process exited before becoming healthy");
            }
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body().contains("UP")) {
                    return;
                }
            } catch (Exception ignored) {
                // retry until ready
            }
            Thread.sleep(500);
        }
        throw new IllegalStateException("Timed out waiting for Quarkus readiness check");
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return 18080;
        }
    }
}
