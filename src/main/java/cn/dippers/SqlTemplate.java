package cn.dippers;


import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@VertxGen
public interface SqlTemplate {

  static SqlTemplate create(SqlClient client, String template) {
    return new SqlTemplateImpl(client, template);
  }

  default <T> Future<List<T>> query(Function<Row, T> mapper, Object args) {
    Promise<List<T>> promise = Promise.promise();
    query(mapper, args, promise);
    return promise.future();
  }

  default <T> Future<List<T>> query(Function<Row, T> mapper, Map<String, Object> args) {
    Promise<List<T>> promise = Promise.promise();
    query(mapper, args, promise);
    return promise.future();
  }

  default <T> void query(Function<Row, T> mapper, Object args, Handler<AsyncResult<List<T>>> resultHandler) {
    query(mapper, JsonObject.mapFrom(args).getMap(), resultHandler);
  }

  <T> void query(Function<Row, T> mapper, Map<String, Object> args, Handler<AsyncResult<List<T>>> resultHandler);

  default <T> Future<List<T>> query(Class<T> type, Object args) {
    Promise<List<T>> promise = Promise.promise();
    query(type, args, promise);
    return promise.future();
  }

  default <T> Future<List<T>> query(Class<T> type, Map<String, Object> args) {
    Promise<List<T>> promise = Promise.promise();
    query(type, args, promise);
    return promise.future();
  }

  default <T> void query(Class<T> type, Object args, Handler<AsyncResult<List<T>>> resultHandler) {
    query(type, JsonObject.mapFrom(args).getMap(), resultHandler);
  }

  <T> void query(Class<T> type, Map<String, Object> args, Handler<AsyncResult<List<T>>> resultHandler);

  default Future<Void> batch(List<Map<String, Object>> list) {
    Promise<Void> promise = Promise.promise();
    batch(list, promise);
    return promise.future();
  }

  void batch(List<Map<String, Object>> list, Handler<AsyncResult<Void>> result);

  default <T> Future<Void> batch(Function<T, Tuple> mapper, List<T> list) {
    Promise<Void> promise = Promise.promise();
    batch(mapper, list, promise);
    return promise.future();
  }

  <T> void batch(Function<T, Tuple> mapper, List<T> list, Handler<AsyncResult<Void>> result);

  default <T> Future<Void> batch(Class<T> type, List<T> list) {
    Promise<Void> promise = Promise.promise();
    batch(type, list, promise);
    return promise.future();
  }

  <T> void batch(Class<T> type, List<T> list, Handler<AsyncResult<Void>> result);

}
