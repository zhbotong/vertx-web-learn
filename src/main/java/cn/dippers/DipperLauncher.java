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
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.Arrays;
import java.util.List;

import static io.vertx.core.spi.resolver.ResolverProvider.DISABLE_DNS_RESOLVER_PROP_NAME;

public class DipperLauncher extends Launcher {

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {

    //配置文件读取
    ConfigRetrieverOptions options = new ConfigRetrieverOptions();
    ConfigStoreOptions storeOptions = new ConfigStoreOptions();
    storeOptions.setType("file").setConfig(new JsonObject().put("path","config.json"));
    options.addStore(storeOptions);
    ConfigRetriever configRetriever = ConfigRetriever.create(Vertx.vertx(),options);
    Future<JsonObject> config = configRetriever.getConfig();
    config.onSuccess(result -> {
      deploymentOptions.setConfig(result);
      super.beforeDeployingVerticle(deploymentOptions);
    });
  }

  @Override
  public void beforeStartingVertx(VertxOptions options) {
    options.setWarningExceptionTime(10L * 1000 * 1000000);  //block时间超过此值，打印代码堆栈
    options.setBlockedThreadCheckInterval(20000); // 每隔x，检查下是否block
    options.setMaxEventLoopExecuteTime(2L * 1000 * 1000000); //允许eventloop block 的最长时间
    super.beforeStartingVertx(options);
  }

  public static void main(String[] args) {
    /**
     * webclient dns_resolver error问题
     * 详见　https://zhuanlan.zhihu.com/p/30913753
     */
    System.getProperties().setProperty(DISABLE_DNS_RESOLVER_PROP_NAME, "true");

    //Jackson对于java8的支持
    ObjectMapper mapper = DatabindCodec.mapper();
    ObjectMapper objectMapper = DatabindCodec.prettyMapper();
    List<Module> modules = Arrays.asList(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule());
    mapper.registerModules(modules);
    objectMapper.registerModules(modules);

    new DipperLauncher().dispatch(args);
  }
}
