/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-05-19 16:54:08                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-09 16:02:03                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice;

import com.da.sage.notice.utils.ConfigUtils;

import io.vertx.core.Vertx;
import io.vertx.core.VertxBuilder;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.launcher.application.HookContext;
import io.vertx.launcher.application.VertxApplicationHooks;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VertxAppHooks implements VertxApplicationHooks {
  @Override
  public VertxBuilder createVertxBuilder(VertxOptions options) {
    log.info("VertxOptions:\n{}", options.toJson().encodePrettily());
    options.setMaxEventLoopExecuteTime(60000000000L); // 60 seconds
    return Vertx.builder().with(options);
  }

  @Override
  public void beforeDeployingVerticle(HookContext context) {
    Vertx vertx = context.vertx();
    JsonObject defaultConfig = ConfigUtils.getConfig(vertx);
    // this config could passing by command line with args
    // -config=#{absolutePath.Config}
    JsonObject deploymentConfig = context.deploymentOptions().getConfig();

    defaultConfig.getMap().forEach((k, v) -> {
      deploymentConfig.put(k, v);
    });
    log.info("Final Config: \n{}",
        deploymentConfig.encodePrettily()
            // .replaceAll("(\\\"user\\\" : \\\").*(\\\",)", "$1******$2")
            .replaceAll("(\\\"password\\\" : \\\").*(\\\",)", "$1******$2"));

  }

  @Override
  public void afterVerticleDeployed(HookContext context) {
  }

  @Override
  public void afterFailureToStartVertx(HookContext context, Throwable t) {
    log.error("{}", t.getMessage());
  }

  @Override
  public void afterFailureToDeployVerticle(HookContext context, Throwable t) {
    log.error("{}", t.getMessage());
    context.vertx().close();
  }

}
