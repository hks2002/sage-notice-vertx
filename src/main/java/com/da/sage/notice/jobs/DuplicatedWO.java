/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-07 13:31:43                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs;

import java.sql.Date;
import java.text.DateFormat;
import java.text.MessageFormat;
//import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.da.sage.notice.db.DB;
import com.da.sage.notice.service.MailService;
import com.da.sage.notice.utils.LocaleUtils;

import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DuplicatedWO implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "DUPLICATE_WORK_ORDER";

    // Retrieve job parameters
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    log.debug("Executing job {} with data: {}", jobName, jobDataMap);
    String site = Optional.ofNullable(jobDataMap.getString("site")).orElse("---");
    String language = Optional.ofNullable(jobDataMap.getString("language")).orElse("en_US");
    String mailTo = Optional.ofNullable(jobDataMap.getString("mailTo")).orElse("");
    String mailCc = Optional.ofNullable(jobDataMap.getString("mailCc")).orElse("");

    Locale locale = LocaleUtils.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
    // NumberFormat numberFormat = NumberFormat.getInstance(locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    DB.queryByFile("DuplicatedWO", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No duplicated WO found for site: {}", site);
          } else {
            StringBuilder msg = new StringBuilder();
            String newMailTo = "";
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              msg.append("<table><tbody>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_NO"))
                  .append("</td><td>")
                  .append(obj.getString("ProjectNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("WO"))
                  .append("</td><td>")
                  .append(obj.getString("WO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("CREATE_USER"))
                  .append("</td><td>")
                  .append(obj.getString("CreateUser"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("CREATE_DATE"))
                  .append("</td><td>")
                  .append(dateFormat.format(new Date(obj.getLong("CreateDate"))))
                  .append("</td></tr>");
              msg.append("</tbody></table>");
              msg.append("<hr />");
              msg.append(MessageFormat.format(i18nMessage.getString("LINE_OF_TOTAL"), i + 1, list.size()));

              newMailTo += ";" + obj.getString("CreateUserEmail");
            }

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            if (msg.length() > 0) {
              MailService.sendEmail(
                  "[SageAssistant]" + "[" + site + "]" +
                      i18nMessage.getString(jobName) + ' ' +
                      MessageFormat.format(i18nMessage.getString("TOTAL_LINE"), list.size()),
                  msg.toString(),
                  mailTo + newMailTo,
                  mailCc);
            }
          }
        }).onFailure(err -> {
          log.error("Error checking duplicated WO for site {}: {}", site, err.getMessage());
        });
  }

}
