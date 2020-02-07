package cn.dippers;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.Arrays;
import java.util.List;

public class MainVerticle  extends AbstractVerticle{
  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    //配置文件读取
    ConfigRetrieverOptions options = new ConfigRetrieverOptions();
    ConfigStoreOptions storeOptions = new ConfigStoreOptions();
    storeOptions.setType("file").setConfig(new JsonObject().put("path","config.json"));
    options.addStore(storeOptions);
    ConfigRetriever configRetriever = ConfigRetriever.create(Vertx.vertx(),options);
    Future<JsonObject> future = configRetriever.getConfig();
    future.onSuccess(result -> {
      vertx
        .deployVerticle(new TodoVerticle(),new DeploymentOptions().setConfig(result))
        .onSuccess(sucess -> startPromise.complete())
        .onFailure(Throwable::printStackTrace)
        .onFailure(fail -> startPromise.fail(fail));
    });
  }
}
