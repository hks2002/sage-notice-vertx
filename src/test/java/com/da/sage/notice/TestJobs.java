/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-05-19 16:13:27                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-14 18:03:26                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.da.sage.notice.service.SchedulerService;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ExtendWith(VertxExtension.class)
public class TestJobs {
  @Test
  void testJob(Vertx vertx, VertxTestContext testContext) throws Throwable {
    VertxApp.main(new String[] {});
    SchedulerService.run("ProjectProfit");

    vertx.setTimer(1000 * 60, (t) -> {
      testContext.completeNow();
    });

  }

  @Test
  void testAllJob(Vertx vertx, VertxTestContext testContext) throws Throwable {
    VertxApp.main(new String[] {});
    SchedulerService.runAll();

    vertx.setTimer(1000 * 60 * 5, (t) -> {
      testContext.completeNow();
    });

  }
}
