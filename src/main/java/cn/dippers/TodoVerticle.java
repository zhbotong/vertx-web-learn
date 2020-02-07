package cn.dippers;

import cn.dippers.entity.Todo;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import java.time.LocalDateTime;
import java.util.*;

public class TodoVerticle extends AbstractVerticle {
  private MySQLPool client;


  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    JsonObject config = config();
    client = initDBPool(vertx,config.getJsonObject("db"));
    Router router = Router.router(vertx);
    enableCors(router);
    router.route().handler(BodyHandler.create());
    router.route().handler(LoggerHandler.create());
    router.get("/todo").handler(this::todoList);

    router.post("/todo").handler(this::addTodo);
    router.patch("/todo").handler(this::updateTodo);
    router.delete("/todo/:id").handler(this::deleteTodo);
    router.route().failureHandler(fail -> {
      fail.failure().printStackTrace();
      HttpServerResponse response = fail.response();
      response.setStatusCode(500);
      response.end(fail.failure().getLocalizedMessage());
    });
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config.getJsonObject("server").getInteger("port"))
      .onSuccess(success -> startPromise.complete())
      .onFailure(Throwable::printStackTrace)
      .onFailure(fail -> startPromise.fail(fail));
  }

  //删除待办事项
  private void deleteTodo(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    String id = routingContext.pathParam("id");
     client.preparedQuery("DELETE FROM todo WHERE id=?",Tuple.of(id))
       .onSuccess(res ->  ResponseUtil.ok(response))
       .onFailure(Throwable::printStackTrace)
       .onFailure(res -> ResponseUtil.fail(response));
  }

  //修改待办事项
  private void updateTodo(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    Todo todo = routingContext.getBodyAsJson().mapTo(Todo.class);
    SqlTemplate sqlTemplate = SqlTemplate.create(client,"UPDATE todo SET content=:content WHERE id=:id");
    sqlTemplate
      .query(Todo.class,todo)
      .onSuccess(res ->  ResponseUtil.ok(response))
      .onFailure(Throwable::printStackTrace)
      .onFailure(res -> ResponseUtil.fail(response));
  }

  //添加待办事项
  private void addTodo(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    Todo todo = routingContext.getBodyAsJson().mapTo(Todo.class);
    todo.setCreateTime(LocalDateTime.now());
    SqlTemplate sqlTemplate = SqlTemplate
      .create(client, "INSERT INTO todo(content,time,createTime) VALUES (:content,:time,:createTime)");
    Future<List<Todo>> listFuture = sqlTemplate.query(Todo.class, todo);
    SqlTemplate template = SqlTemplate.create(client, "SELECT LAST_INSERT_ID() AS id");
    listFuture
      .compose(res -> template.query(Todo.class, null))
      .onSuccess(res ->  ResponseUtil.ok(response,Json.encode(res.get(0))))
      .onFailure(Throwable::printStackTrace)
      .onFailure(res -> ResponseUtil.fail(response));

  }

  // 待办列表
  private void todoList(RoutingContext routingContext) {
    SqlTemplate sqlTemplate = SqlTemplate.create(client, "SELECT * FROM todo");
    Future<List<Todo>> resultFuture = sqlTemplate.query(Todo.class, null);
    HttpServerResponse response = routingContext.response();
    resultFuture.onSuccess(result -> ResponseUtil.ok(response,Json.encode(result)));
    resultFuture.onFailure(Throwable::printStackTrace);
    resultFuture.onFailure(result -> ResponseUtil.fail(response));
  }

  /**
   * 数据库配置
   */
  private MySQLPool initDBPool(Vertx vertx,JsonObject config){
    MySQLConnectOptions connectOptions = new MySQLConnectOptions(config.getJsonObject("connect"));
    PoolOptions poolOptions = new PoolOptions(config.getJsonObject("pool"));
    return MySQLPool.pool(vertx,connectOptions, poolOptions);
  }

  /**
   * 跨域处理
   * @param router
   */
  private void enableCors(Router router){
    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");
    allowedHeaders.add("X-PINGARUNER");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);

    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);

    router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
  }
}
