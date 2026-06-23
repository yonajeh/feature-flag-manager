package com.featureflag.testsupport;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class TestApiClient {

    private TestApiClient() {}

    public static String adminLogin() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"admin\",\"password\":\"admin\"}")
                .post("/api/admin/auth/login");
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Admin login failed: " + response.statusCode() + " " + response.asString());
        }
        return response.jsonPath().getString("token");
    }

    public static io.restassured.specification.RequestSpecification authorized(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

    public static Response consumerGet(String appName, String token, String path) {
        return given()
                .header("X-App-Name", appName)
                .header("X-App-Token", token)
                .get(path);
    }
}
