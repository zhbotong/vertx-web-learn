package cn.dippers;

import io.vertx.core.http.HttpServerResponse;

public class ResponseUtil {
  public static void ok(HttpServerResponse response,String result){
      response.setStatusCode(200).end(result);
  }

  public static void ok(HttpServerResponse response){
    response.setStatusCode(200).end();
  }

  public static void fail(HttpServerResponse response,String errorMsg){
      response.setStatusCode(500).end(errorMsg,"UTF-8");
  }
  public static void fail(HttpServerResponse response){
    response.setStatusCode(500).end();
  }
}
