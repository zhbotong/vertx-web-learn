package cn.dippers;

import cn.dippers.entity.Todo;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@ExtendWith(VertxExtension.class)
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class TestTodoVerticle {
  private HttpClient httpClient;

  private static Long id;
  private static final String url = "http://localhost:8080/todo";
  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {

    vertx.deployVerticle(new MainVerticle(),testContext.succeeding(id -> testContext.completeNow()));
    ObjectMapper mapper = DatabindCodec.mapper();
    ObjectMapper objectMapper = DatabindCodec.prettyMapper();
    List<Module> modules = Arrays.asList(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
    mapper.registerModules(modules);
    objectMapper.registerModules(modules);
    httpClient = HttpClient.newBuilder().build();
  }

  @Test
  void test_3todoList() throws Throwable {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
    HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Assertions.assertEquals(httpResponse.statusCode(),200);
  }

  @Test
  void test_1addTodo() throws Throwable {
    Todo to = new Todo();
    to.setTime(LocalDateTime.now());
    to.setContent("hello word");
    HttpRequest request = HttpRequest
      .newBuilder(URI.create(url))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(Json.encode(to)))
      .build();
    HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Assertions.assertEquals(httpResponse.statusCode(),200);
    Assertions.assertNotNull(httpResponse.body());
    String body = httpResponse.body();
    Todo todo = Json.decodeValue(body, Todo.class);
    id = todo.getId();

    Assertions.assertNotNull(todo.getId());
  }

  @Test
  void test_2updateTodo()throws Throwable {
    Todo to = new Todo();
    to.setId(id);
    to.setTime(LocalDateTime.now());
    to.setContent("hello word2");
    HttpRequest request = HttpRequest
      .newBuilder(URI.create(url))
      .header("Content-Type", "application/json")
      .method("PATCH",HttpRequest.BodyPublishers.ofString(Json.encode(to)))
      .build();
    HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Assertions.assertEquals(httpResponse.statusCode(),200);
  }
  @Test
  void test_4deleteTodo()throws Throwable {
    HttpRequest request = HttpRequest
      .newBuilder(URI.create(url+"/"+id))
      .DELETE()
      .build();
    HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    Assertions.assertEquals(httpResponse.statusCode(),200);
  }


}
