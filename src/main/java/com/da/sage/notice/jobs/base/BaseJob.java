/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-11-07 12:43:58                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 13:38:14                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs.base;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.da.sage.notice.utils.L;

import io.vertx.core.json.JsonObject;

public abstract class BaseJob implements Job {

  protected String jobName;
  protected JobDataMap config;
  protected ResourceBundle i18n;

  protected String site;
  protected String language;
  protected String mailTo;
  protected String mailCc;
  protected Locale locale;
  protected JsonObject params;

  @Override
  public final void execute(JobExecutionContext context) throws JobExecutionException {
    config = context.getJobDetail().getJobDataMap();
    System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXx");

    // setup common parameters
    jobName = "BASE_JOB";
    site = Optional.ofNullable(config.getString("site")).orElse("---");
    language = Optional.ofNullable(config.getString("language")).orElse("en_US");
    mailTo = Optional.ofNullable(config.getString("mailTo")).orElse("");
    mailCc = Optional.ofNullable(config.getString("mailCc")).orElse("");

    // i18n resource bundle
    locale = L.getLocale(language);
    i18n = ResourceBundle.getBundle("i18n", locale);

    // execution params
    params = new JsonObject();
    params.put("Site", site);

    // subclass execute
    executeJob(context);
  }

  /**
   * subclass must implement this method to execute job logic
   * 
   * @param context JobExecutionContext
   * @throws JobExecutionException if job execution fails
   */
  protected abstract void executeJob(JobExecutionContext context) throws JobExecutionException;
}
