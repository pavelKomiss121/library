package ru.mentee.library;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

public class Specs {

    /**
     * Создает RequestSpecification для аутентифицированных запросов с JWT токеном
     * Использует глобальные настройки RestAssured для baseURI и port
     */
    public static RequestSpecification authenticatedRequestSpec(String token) {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    /**
     * Создает RequestSpecification для неаутентифицированных запросов
     * Использует глобальные настройки RestAssured для baseURI и port
     */
    public static RequestSpecification unauthenticatedRequestSpec() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }

    /**
     * ResponseSpecification для успешных ответов (200 OK)
     */
    public static ResponseSpecification successResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    /**
     * ResponseSpecification для успешного создания ресурса (201 Created)
     */
    public static ResponseSpecification createdResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(201)
                .expectContentType(ContentType.JSON)
                .build();
    }

    /**
     * ResponseSpecification для ответов с ошибкой валидации (400 Bad Request)
     */
    public static ResponseSpecification badRequestResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(400)
                .build();
    }

    /**
     * ResponseSpecification для ответов с отсутствием ресурса (404 Not Found)
     */
    public static ResponseSpecification notFoundResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .build();
    }

    /**
     * ResponseSpecification для ответов с отсутствием авторизации (401 Unauthorized)
     */
    public static ResponseSpecification unauthorizedResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(401)
                .build();
    }

    /**
     * ResponseSpecification для ответов с отсутствием доступа (403 Forbidden)
     */
    public static ResponseSpecification forbiddenResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(403)
                .build();
    }

    /**
     * ResponseSpecification для успешного удаления (204 No Content)
     */
    public static ResponseSpecification noContentResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(204)
                .build();
    }
}

