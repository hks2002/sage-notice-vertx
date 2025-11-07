/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-09 16:00:25                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 10:53:32                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.utils;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConfigUtils {
  public static JsonObject getConfig(Vertx vertx) {
    ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions();

    if (vertx.fileSystem().existsBlocking("config.json")) {
      retrieverOptions.addStore(new ConfigStoreOptions().setType("file")
          .setConfig(JsonObject.of("path", "config.json")));
    }
    if (vertx.fileSystem().existsBlocking("config-prod.json")) {
      retrieverOptions.addStore(new ConfigStoreOptions().setType("file")
          .setConfig(JsonObject.of("path", "config-prod.json")));
    }
    if (vertx.fileSystem().existsBlocking("config-test.json")) {
      retrieverOptions.addStore(new ConfigStoreOptions().setType("file")
          .setConfig(JsonObject.of("path", "config-test.json")));
    }
    ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, retrieverOptions);
    try {
      log.info("Loading config for verticle");
      return cfgRetriever.getConfig().toCompletionStage().toCompletableFuture().get();
    } catch (Exception e) {
      log.error("{}", e);
      return new JsonObject();
    }

  }
}
