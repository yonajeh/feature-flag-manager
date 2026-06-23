package com.featureflag.scenario;

import com.featureflag.testsupport.QuarkusJarTestResource;
import com.featureflag.testsupport.TestApiClient;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Consolidated Gherkin scenarios E1–E7 against the packaged Quarkus application.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AllScenariosIT {

    String adminToken;

    // E1 state
    UUID e1AppId;
    String e1AppName;
    String e1Token;

    // E3 state
    UUID e3AppAId;
    UUID e3AppBId;
    String e3AppAName;
    String e3AppBName;
    String e3TokenA;
    String e3TokenB;

    // E4 state
    UUID e4AppId;
    String e4AppName;
    String e4OriginalToken;
    String e4RotatedToken;

    // E5 state
    UUID e5AppId;
    String e5AppName;
    String e5Token;
    UUID e5TokenId;

    // E6 state
    UUID e6AppId;

    @BeforeAll
    void startApp() throws Exception {
        QuarkusJarTestResource.start();
        RestAssured.baseURI = QuarkusJarTestResource.baseUrl();
        adminToken = TestApiClient.adminLogin();
    }

    @AfterAll
    void stopApp() {
        QuarkusJarTestResource.stop();
    }

    // --- E1: Consumer reads flags ---
    @Test
    @Order(10)
    void e1_setupApplicationAndFlags() {
        e1AppName = "e1-" + UUID.randomUUID().toString().substring(0, 8);
        var createResponse = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"name\":\"" + e1AppName + "\",\"displayName\":\"E1\"}")
                .post("/api/admin/applications");
        if (createResponse.statusCode() != 201) {
            throw new AssertionError("Create app failed: " + createResponse.statusCode() + " " + createResponse.asString());
        }
        e1AppId = UUID.fromString(createResponse.jsonPath().getString("id"));
        e1Token = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e1AppId + "/tokens")
                .then().statusCode(201).extract().jsonPath().getString("plaintextToken");
        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"key\":\"feature-a\",\"enabled\":true,\"description\":\"A\"}")
                .post("/api/admin/applications/" + e1AppId + "/flags").then().statusCode(201);
        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"key\":\"feature-b\",\"enabled\":false}")
                .post("/api/admin/applications/" + e1AppId + "/flags").then().statusCode(201);
    }

    @Test
    @Order(11)
    void e1_listFeatures() {
        given().header("X-App-Name", e1AppName).header("X-App-Token", e1Token)
                .get("/api/v1/features")
                .then().statusCode(200).body("size()", equalTo(2));
    }

    @Test
    @Order(12)
    void e1_getFeatureByKey() {
        given().header("X-App-Name", e1AppName).header("X-App-Token", e1Token)
                .get("/api/v1/features/feature-a")
                .then().statusCode(200).body("key", equalTo("feature-a")).body("enabled", equalTo(true));
    }

    // --- E2: Auth rejection ---
    @Test
    @Order(20)
    void e2_missingHeaders_returns401() {
        given().get("/api/v1/features").then().statusCode(401).body("code", equalTo("UNAUTHORIZED"));
    }

    @Test
    @Order(21)
    void e2_invalidToken_returns401() {
        given().header("X-App-Name", "nonexistent").header("X-App-Token", "ff_live_invalid")
                .get("/api/v1/features").then().statusCode(401);
    }

    @Test
    @Order(22)
    void e2_wrongAppName_returns401() {
        given().header("X-App-Name", "wrong").header("X-App-Token", e1Token)
                .get("/api/v1/features").then().statusCode(401);
    }

    // --- E3: Isolation ---
    @Test
    @Order(30)
    void e3_setupApps() {
        e3AppAName = "e3a-" + UUID.randomUUID().toString().substring(0, 8);
        e3AppBName = "e3b-" + UUID.randomUUID().toString().substring(0, 8);
        e3AppAId = UUID.fromString(TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"name\":\"" + e3AppAName + "\",\"displayName\":\"A\"}")
                .post("/api/admin/applications").then().statusCode(201).extract().jsonPath().getString("id"));
        e3AppBId = UUID.fromString(TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"name\":\"" + e3AppBName + "\",\"displayName\":\"B\"}")
                .post("/api/admin/applications").then().statusCode(201).extract().jsonPath().getString("id"));
        e3TokenA = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e3AppAId + "/tokens")
                .then().statusCode(201).extract().jsonPath().getString("plaintextToken");
        e3TokenB = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e3AppBId + "/tokens")
                .then().statusCode(201).extract().jsonPath().getString("plaintextToken");
        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"key\":\"only-a\",\"enabled\":true}")
                .post("/api/admin/applications/" + e3AppAId + "/flags").then().statusCode(201);
        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"key\":\"only-b\",\"enabled\":true}")
                .post("/api/admin/applications/" + e3AppBId + "/flags").then().statusCode(201);
    }

    @Test
    @Order(31)
    void e3_appASeesOnlyOwnFlags() {
        given().header("X-App-Name", e3AppAName).header("X-App-Token", e3TokenA)
                .get("/api/v1/features").then().statusCode(200).body("size()", equalTo(1));
    }

    @Test
    @Order(32)
    void e3_appBCannotSeeAppAFlag() {
        given().header("X-App-Name", e3AppBName).header("X-App-Token", e3TokenB)
                .get("/api/v1/features/only-a").then().statusCode(404);
    }

    // --- E4: Rotation ---
    @Test
    @Order(40)
    void e4_rotateToken_bothWork() {
        e4AppName = "e4-" + UUID.randomUUID().toString().substring(0, 8);
        e4AppId = UUID.fromString(TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"name\":\"" + e4AppName + "\",\"displayName\":\"E4\"}")
                .post("/api/admin/applications").then().statusCode(201).extract().jsonPath().getString("id"));
        var created = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e4AppId + "/tokens")
                .then().statusCode(201).extract().jsonPath();
        e4OriginalToken = created.getString("plaintextToken");
        UUID tokenId = UUID.fromString(created.getString("id"));
        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"key\":\"rot-flag\",\"enabled\":true}")
                .post("/api/admin/applications/" + e4AppId + "/flags").then().statusCode(201);
        e4RotatedToken = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e4AppId + "/tokens/" + tokenId + "/rotate")
                .then().statusCode(201).extract().jsonPath().getString("plaintextToken");

        given().header("X-App-Name", e4AppName).header("X-App-Token", e4OriginalToken)
                .get("/api/v1/features").then().statusCode(200);
        given().header("X-App-Name", e4AppName).header("X-App-Token", e4RotatedToken)
                .get("/api/v1/features").then().statusCode(200);
    }

    // --- E5: Revocation ---
    @Test
    @Order(50)
    void e5_revokeToken() {
        e5AppName = "e5-" + UUID.randomUUID().toString().substring(0, 8);
        e5AppId = UUID.fromString(TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"name\":\"" + e5AppName + "\",\"displayName\":\"E5\"}")
                .post("/api/admin/applications").then().statusCode(201).extract().jsonPath().getString("id"));
        var created = TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e5AppId + "/tokens")
                .then().statusCode(201).extract().jsonPath();
        e5Token = created.getString("plaintextToken");
        e5TokenId = UUID.fromString(created.getString("id"));

        given().header("X-App-Name", e5AppName).header("X-App-Token", e5Token)
                .get("/api/v1/features").then().statusCode(200);

        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e5AppId + "/tokens/" + e5TokenId + "/revoke")
                .then().statusCode(200).body("status", equalTo("REVOKED"));

        given().header("X-App-Name", e5AppName).header("X-App-Token", e5Token)
                .get("/api/v1/features").then().statusCode(401);
    }

    // --- E6: Admin CRUD ---
    @Test
    @Order(60)
    void e6_adminCrudLifecycle() {
        String name = "e6-" + UUID.randomUUID().toString().substring(0, 8);
        e6AppId = UUID.fromString(TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"name\":\"" + name + "\",\"displayName\":\"E6\"}")
                .post("/api/admin/applications").then().statusCode(201).extract().jsonPath().getString("id"));

        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"displayName\":\"Updated\"}")
                .put("/api/admin/applications/" + e6AppId)
                .then().statusCode(200).body("displayName", equalTo("Updated"));

        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"key\":\"admin-flag\",\"enabled\":true}")
                .post("/api/admin/applications/" + e6AppId + "/flags")
                .then().statusCode(201);

        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .post("/api/admin/applications/" + e6AppId + "/tokens")
                .then().statusCode(201).body("plaintextToken", notNullValue());

        TestApiClient.authorized(adminToken).get("/api/admin/applications/" + e6AppId + "/tokens")
                .then().statusCode(200).body("[0].plaintextToken", nullValue());

        TestApiClient.authorized(adminToken).contentType(ContentType.JSON)
                .body("{\"enabled\":false}")
                .put("/api/admin/applications/" + e6AppId + "/flags/admin-flag")
                .then().statusCode(200).body("enabled", equalTo(false));

        TestApiClient.authorized(adminToken)
                .delete("/api/admin/applications/" + e6AppId + "/flags/admin-flag")
                .then().statusCode(204);

        TestApiClient.authorized(adminToken)
                .delete("/api/admin/applications/" + e6AppId)
                .then().statusCode(204);
    }

    // --- E7: Admin guard ---
    @Test
    @Order(70)
    void e7_unauthenticatedAdmin_returns401() {
        given().get("/api/admin/applications").then().statusCode(401);
    }

    @Test
    @Order(71)
    void e7_invalidLogin_returns401() {
        given().contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"wrong\"}")
                .post("/api/admin/auth/login")
                .then().statusCode(401).body("code", equalTo("UNAUTHORIZED"));
    }

    @Test
    @Order(72)
    void e7_healthUp() {
        given().get("/q/health").then().statusCode(200).body("status", equalTo("UP"));
    }

    @Test
    @Order(73)
    void e7_openapiAvailable() {
        given().get("/q/openapi").then().statusCode(200)
                .body(containsString("Feature Flag Manager API"));
    }
}
