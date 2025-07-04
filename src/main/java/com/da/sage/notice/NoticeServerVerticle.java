/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-03-08 19:11:51                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-02 17:14:08                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice;

import com.da.sage.notice.db.DB;
import com.da.sage.notice.service.MailService;
import com.da.sage.notice.service.SchedulerService;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;

public class NoticeServerVerticle extends VerticleBase {
  @Override
  public Future<?> start() {
    DB.initDB(vertx);
    MailService.init(vertx);
    SchedulerService.init(vertx);

    return Future.succeededFuture();
  }

  @Override
  public Future<?> stop() {
    DB.closeAll();
    return Future.succeededFuture();
  }
}
