package cn.dippers;


import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SqlTemplateImpl implements SqlTemplate {

  private static Pattern P = Pattern.compile(":(\\p{Alnum}+)");

  private final SqlClientBase<?> client;
  private final String sql;
  private final List<String> mapping;

  public SqlTemplateImpl(SqlClient client, String template) {

    mapping = new ArrayList<>();
    StringBuilder actual = new StringBuilder();
    Matcher matcher = P.matcher(template);
    int pos = 0;
    while (matcher.find()) {
      mapping.add(matcher.group(1));
      actual.append(template, pos, matcher.start());
      actual.append('?');
      pos = matcher.end();
    }
    actual.append(template, pos, template.length());
    sql = actual.toString();


    this.client = (SqlClientBase) client;
  }

  @Override
  public <T> void query(Class<T> type, Map<String, Object> args, Handler<AsyncResult<List<T>>> asyncResultHandler) {
    query(row -> {
      JsonObject json = new JsonObject();
      for (int i = 0;i < row.size();i++) {
        json.getMap().put(row.getColumnName(i), row.getValue(i));
      }
      return json.mapTo(type);
    }, args, asyncResultHandler);
  }

  private Tuple mapTuple(Map<String, Object> args) {
    return Tuple.wrap(mapping.stream().map(k -> args.get(k)).collect(Collectors.toList()));
  }

  @Override
  public <T> void query(Function<Row, T> f, Map<String, Object> args, Handler<AsyncResult<List<T>>> asyncResultHandler) {
    Tuple tuple = mapTuple(args);
    System.out.println(sql);
    client.preparedQuery(sql, tuple, ar -> {
      asyncResultHandler.handle(ar.map(abc -> {
        List<T> list = new ArrayList<>();
        abc.forEach(r -> list.add(f.apply(r)));
        return list;
      }));
    });
  }

  @Override
  public void batch(List<Map<String, Object>> list, Handler<AsyncResult<Void>> result) {
    batch(this::mapTuple, list, result);
  }

  @Override
  public <T> void batch(Function<T, Tuple> mapper, List<T> list, Handler<AsyncResult<Void>> result) {
    client.preparedBatch(sql, list.stream().map(mapper).collect(Collectors.toList()), ar -> result.handle(ar.mapEmpty()));
  }

  @Override
  public <T> void batch(Class<T> type, List<T> list, Handler<AsyncResult<Void>> result) {
    batch(t -> mapTuple(JsonObject.mapFrom(t).getMap()), list, result);
  }
}
