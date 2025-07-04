/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-03-27 16:01:51                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-02 16:48:29                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice;

import java.util.Arrays;

import io.vertx.launcher.application.VertxApplication;
import io.vertx.launcher.application.VertxApplicationHooks;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VertxApp {
  public static void main(String[] args) {
    // Append extra arguments to the args array
    String[] extraArgs = new String[] { "com.da.sage.notice.NoticeServerVerticle" };
    String[] newArgs = Arrays.copyOf(args, args.length + 1);
    System.arraycopy(extraArgs, 0, newArgs, args.length, 1);
    args = newArgs;

    VertxApplicationHooks hooks = new VertxAppHooks();
    VertxApplication app = new VertxApplication(args, hooks);
    app.launch();

  }
}
