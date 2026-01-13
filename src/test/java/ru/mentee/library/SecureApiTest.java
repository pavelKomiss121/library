package ru.mentee.library;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SecureApiTest {

  @Value("${local.server.port}")
  private int port;

  private static RequestSpecification userSpec;
  private static RequestSpecification librarianSpec;
  private static RequestSpecification adminSpec;

  private static ResponseSpecification successSpec;
  private static ResponseSpecification createdSpec;
  private static ResponseSpecification notFoundSpec;
  private static ResponseSpecification unauthorizedSpec;
  private static ResponseSpecification forbiddenSpec;

  private static String userToken;
  private static String librarianToken;
  private static String adminToken;
  private static Integer createdBookId;

  @BeforeAll
  static void setupSpecs() {
    // Инициализация будет выполнена в setupPort()
  }

  @BeforeEach
  void setupPort() {
    // Проверяем, что порт валидный
    assertTrue(port > 0, "Port must be greater than 0");

    // Устанавливаем порт и baseURI для RestAssured
    RestAssured.port = port;
    RestAssured.baseURI = "http://localhost";
    RestAssured.basePath = "";

    // Получаем токены для разных ролей (только один раз)
    if (userToken == null) {
      userToken = loginAndExtractToken("user@example.com", "password");
      librarianToken = loginAndExtractToken("librarian@example.com", "password");
      adminToken = loginAndExtractToken("admin@example.com", "password");
    }

    // Создаем спецификации для аутентифицированных запросов
    userSpec = Specs.authenticatedRequestSpec(userToken);
    librarianSpec = Specs.authenticatedRequestSpec(librarianToken);
    adminSpec = Specs.authenticatedRequestSpec(adminToken);

    // Создаем спецификации для ответов
    successSpec = Specs.successResponseSpec();
    createdSpec = Specs.createdResponseSpec();
    notFoundSpec = Specs.notFoundResponseSpec();
    unauthorizedSpec = Specs.unauthorizedResponseSpec();
    forbiddenSpec = Specs.forbiddenResponseSpec();
  }

  /** Вспомогательный метод для логина и извлечения токена */
  private String loginAndExtractToken(String username, String password) {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("username", username);
    loginRequest.put("password", password);

    return given()
        .contentType(ContentType.JSON)
        .body(loginRequest)
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .path("accessToken");
  }

  @Test
  @Order(1)
  @DisplayName("Should успешно залогиниться и получить JWT токен")
  void shouldLoginAndGetToken() {
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("username", "user@example.com");
    loginRequest.put("password", "password");

    Response response =
        given()
            .spec(Specs.unauthenticatedRequestSpec())
            .body(loginRequest)
            .when()
            .post("/auth/login")
            .then()
            .spec(successSpec)
            .body("accessToken", notNullValue())
            .body("refreshToken", notNullValue())
            .body("accessToken", not(emptyString()))
            .body("refreshToken", not(emptyString()))
            .extract()
            .response();

    String accessToken = response.path("accessToken");
    String refreshToken = response.path("refreshToken");

    assertNotNull(accessToken);
    assertNotNull(refreshToken);
    assertFalse(accessToken.isEmpty());
    assertFalse(refreshToken.isEmpty());
  }

  @Test
  @Order(2)
  @DisplayName("Should получить список всех книг (публичный эндпоинт)")
  void shouldGetAllBooks() {
    given()
        .spec(Specs.unauthenticatedRequestSpec())
        .when()
        .get("/api/books")
        .then()
        .spec(successSpec)
        .body("$", isA(List.class))
        .body("size()", greaterThanOrEqualTo(0));
  }

  @Test
  @Order(3)
  @DisplayName("Should создать книгу с использованием JWT токена (LIBRARIAN)")
  void shouldCreateBookWithJwtToken() {
    Map<String, Object> bookRequest = new HashMap<>();
    bookRequest.put("title", "Test Book");
    bookRequest.put("author", "Test Author");
    bookRequest.put("publicationYear", 2024);

    Response response =
        given()
            .spec(librarianSpec)
            .body(bookRequest)
            .when()
            .post("/api/books")
            .then()
            .spec(createdSpec)
            .body("id", notNullValue())
            .body("title", equalTo("Test Book"))
            .body("author", equalTo("Test Author"))
            .body("publicationYear", equalTo(2024))
            .body("available", notNullValue())
            .extract()
            .response();

    // JsonPath возвращает Integer, преобразуем в Long если нужно
    Integer id = response.path("id");
    createdBookId = id;
    assertNotNull(createdBookId);
  }

  @Test
  @Order(4)
  @DisplayName("Should получить созданную книгу по ID")
  void shouldGetCreatedBookById() {
    assumeTrue(createdBookId != null, "Book should be created first");

    // GET /api/books/{id} требует аутентификацию согласно SecurityConfig
    given()
        .spec(userSpec)
        .pathParam("id", createdBookId)
        .when()
        .get("/api/books/{id}")
        .then()
        .spec(successSpec)
        .body("id", equalTo(createdBookId))
        .body("title", equalTo("Test Book"))
        .body("author", equalTo("Test Author"))
        .body("publicationYear", equalTo(2024));
  }

  @Test
  @Order(5)
  @DisplayName(
      "Should обновить книгу с использованием JWT токена (цепочка: логин -> получение -> изменение)")
  void shouldUpdateBookWithJwtToken() {
    assumeTrue(createdBookId != null, "Book should be created first");

    Map<String, Object> updateRequest = new HashMap<>();
    updateRequest.put("title", "Updated Test Book");
    updateRequest.put("author", "Updated Test Author");
    updateRequest.put("publicationYear", 2025);

    given()
        .spec(librarianSpec)
        .pathParam("id", createdBookId)
        .body(updateRequest)
        .when()
        .put("/api/books/{id}")
        .then()
        .spec(successSpec)
        .body("id", equalTo(createdBookId))
        .body("title", equalTo("Updated Test Book"))
        .body("author", equalTo("Updated Test Author"))
        .body("publicationYear", equalTo(2025));
  }

  @Test
  @Order(6)
  @DisplayName("Should использовать JsonPath для детальной валидации сложного JSON-ответа")
  void shouldValidateComplexJsonResponse() {
    // Создаем несколько книг для сложной валидации
    Map<String, Object> book1 = new HashMap<>();
    book1.put("title", "Book 1");
    book1.put("author", "Author A");
    book1.put("publicationYear", 2020);

    Map<String, Object> book2 = new HashMap<>();
    book2.put("title", "Book 2");
    book2.put("author", "Author B");
    book2.put("publicationYear", 2021);

    given().spec(librarianSpec).body(book1).when().post("/api/books");
    given().spec(librarianSpec).body(book2).when().post("/api/books");

    // Получаем все книги и валидируем сложными JsonPath выражениями
    given()
        .spec(Specs.unauthenticatedRequestSpec())
        .when()
        .get("/api/books")
        .then()
        .spec(successSpec)
        // Проверяем, что список не пустой
        .body("$", hasSize(greaterThan(0)))
        // Проверяем, что у всех книг есть title
        .body("title", everyItem(notNullValue()))
        // Проверяем, что у всех книг есть author
        .body("author", everyItem(notNullValue()))
        // Проверяем, что есть книга с определенным названием
        .body("find { it.title == 'Book 1' }", notNullValue())
        .body("find { it.title == 'Book 1' }.author", equalTo("Author A"))
        // Проверяем, что все книги имеют id
        .body("id", everyItem(notNullValue()))
        // Проверяем, что все id больше 0
        .body("id", everyItem(greaterThan(0)));
  }

  @Test
  @Order(7)
  @DisplayName("Should получить 403 Forbidden при попытке создать книгу без прав LIBRARIAN/ADMIN")
  void shouldGetForbiddenWhenCreatingBookWithoutPermissions() {
    Map<String, Object> bookRequest = new HashMap<>();
    bookRequest.put("title", "Unauthorized Book");
    bookRequest.put("author", "Unauthorized Author");
    bookRequest.put("publicationYear", 2024);

    given()
        .spec(userSpec) // USER не имеет прав на создание книг
        .body(bookRequest)
        .when()
        .post("/api/books")
        .then()
        .spec(forbiddenSpec);
  }

  @Test
  @Order(8)
  @DisplayName("Should получить 401 Unauthorized при запросе без токена")
  void shouldGetUnauthorizedWithoutToken() {
    Map<String, Object> bookRequest = new HashMap<>();
    bookRequest.put("title", "Unauthorized Book");
    bookRequest.put("author", "Unauthorized Author");

    given()
        .spec(Specs.unauthenticatedRequestSpec())
        .body(bookRequest)
        .when()
        .post("/api/books")
        .then()
        .spec(unauthorizedSpec);
  }

  @Test
  @Order(9)
  @DisplayName("Should получить 404 Not Found для несуществующей книги")
  void shouldGetNotFoundForNonExistentBook() {
    // GET /api/books/{id} требует аутентификацию согласно SecurityConfig
    given()
        .spec(userSpec)
        .pathParam("id", 99999)
        .when()
        .get("/api/books/{id}")
        .then()
        .spec(notFoundSpec);
  }

  @Test
  @Order(10)
  @DisplayName("Should использовать Groovy-like синтаксис JsonPath для фильтрации")
  void shouldUseGroovyLikeJsonPathSyntax() {
    // Создаем книги с разными годами публикации
    Map<String, Object> oldBook = new HashMap<>();
    oldBook.put("title", "Old Book");
    oldBook.put("author", "Old Author");
    oldBook.put("publicationYear", 1990);

    Map<String, Object> newBook = new HashMap<>();
    newBook.put("title", "New Book");
    newBook.put("author", "New Author");
    newBook.put("publicationYear", 2024);

    given().spec(librarianSpec).body(oldBook).when().post("/api/books");
    given().spec(librarianSpec).body(newBook).when().post("/api/books");

    // Используем Groovy-like синтаксис для фильтрации
    Response response =
        given()
            .spec(Specs.unauthenticatedRequestSpec())
            .when()
            .get("/api/books")
            .then()
            .spec(successSpec)
            .extract()
            .response();

    // Проверяем, что есть книги с publicationYear >= 2020
    List<Integer> recentYears =
        response.path("findAll { it.publicationYear >= 2020 }.publicationYear");
    assertNotNull(recentYears);
    assertTrue(recentYears.size() > 0);
  }

  @Test
  @Order(11)
  @DisplayName("Should удалить книгу с использованием JWT токена")
  void shouldDeleteBook() {
    assumeTrue(createdBookId != null, "Book should be created first");

    given()
        .spec(librarianSpec)
        .pathParam("id", createdBookId)
        .when()
        .delete("/api/books/{id}")
        .then()
        .spec(Specs.noContentResponseSpec());

    // Проверяем, что книга действительно удалена (GET /api/books/{id} требует аутентификацию)
    given()
        .spec(userSpec)
        .pathParam("id", createdBookId)
        .when()
        .get("/api/books/{id}")
        .then()
        .spec(notFoundSpec);
  }

  @Nested
  @DisplayName("Параметризованные тесты")
  class ParameterizedTests {

    @Test
    @DisplayName("Should валидировать различные невалидные данные для создания книги")
    void shouldValidateInvalidBookData() {
      // Тест 1: Отсутствие title
      Map<String, Object> invalidBook1 = new HashMap<>();
      invalidBook1.put("author", "Author");
      invalidBook1.put("publicationYear", 2024);

      given()
          .spec(librarianSpec)
          .body(invalidBook1)
          .when()
          .post("/api/books")
          .then()
          .spec(Specs.badRequestResponseSpec()); // Валидация должна вернуть 400

      // Тест 2: Отсутствие author
      Map<String, Object> invalidBook2 = new HashMap<>();
      invalidBook2.put("title", "Title");
      invalidBook2.put("publicationYear", 2024);

      given()
          .spec(librarianSpec)
          .body(invalidBook2)
          .when()
          .post("/api/books")
          .then()
          .spec(Specs.badRequestResponseSpec()); // Валидация должна вернуть 400

      // Тест 3: Отрицательный год
      Map<String, Object> invalidBook3 = new HashMap<>();
      invalidBook3.put("title", "Title");
      invalidBook3.put("author", "Author");
      invalidBook3.put("publicationYear", -100);

      given()
          .spec(librarianSpec)
          .body(invalidBook3)
          .when()
          .post("/api/books")
          .then()
          .spec(Specs.badRequestResponseSpec()); // Валидация должна вернуть 400
    }

    @Test
    @DisplayName("Should проверить различные роли пользователей")
    void shouldTestDifferentUserRoles() {
      // USER может читать книги
      given().spec(userSpec).when().get("/api/books").then().spec(successSpec);

      // USER не может создавать книги
      Map<String, Object> bookRequest = new HashMap<>();
      bookRequest.put("title", "Test");
      bookRequest.put("author", "Author");
      bookRequest.put("publicationYear", 2024);

      given().spec(userSpec).body(bookRequest).when().post("/api/books").then().spec(forbiddenSpec);

      // LIBRARIAN может создавать книги
      given()
          .spec(librarianSpec)
          .body(bookRequest)
          .when()
          .post("/api/books")
          .then()
          .spec(createdSpec);

      // ADMIN может создавать книги
      given().spec(adminSpec).body(bookRequest).when().post("/api/books").then().spec(createdSpec);
    }
  }
}
