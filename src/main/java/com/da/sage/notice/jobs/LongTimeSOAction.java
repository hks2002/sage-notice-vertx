/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-10 12:34:14                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs;

import java.text.MessageFormat;
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
public class LongTimeSOAction implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "LONG_TIME_SO_ACTION";

    // Retrieve job parameters
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    log.debug("Executing job {} with data: {}", jobName, jobDataMap);
    String site = Optional.ofNullable(jobDataMap.getString("site")).orElse("---");
    String language = Optional.ofNullable(jobDataMap.getString("language")).orElse("en_US");
    String days = Optional.ofNullable(jobDataMap.getString("days")).orElse("30");
    String mailTo = Optional.ofNullable(jobDataMap.getString("mailTo")).orElse("");
    String mailCc = Optional.ofNullable(jobDataMap.getString("mailCc")).orElse("");

    Locale locale = LocaleUtils.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    params.put("Days", days);

    DB.queryByFile("LongTimeSOAction", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No long time SO action found for site: {}", site);
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
                  .append(i18nMessage.getString("SALES_ORDER_NO"))
                  .append("</td><td>")
                  .append(obj.getString("SalesOrderNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_TYPE"))
                  .append("</td><td>")
                  .append(obj.getString("OrderType"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PN"))
                  .append("</td><td>")
                  .append(obj.getString("PN"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("DESCRIPTION"))
                  .append("</td><td>")
                  .append(obj.getString("Description"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("QTY"))
                  .append("</td><td>")
                  .append(obj.getInteger("QTY"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("UNIT"))
                  .append("</td><td>")
                  .append(obj.getString("Unit"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_PRICE"))
                  .append("</td><td>")
                  .append(obj.getFloat("OrderPrice"))
                  .append(LocaleUtils.getCurrency(obj.getFloat("OrderPrice"), locale))
                  .append(" ")
                  .append(obj.getString("OrderCurrency"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("MARK"))
                  .append("</td><td>")
                  .append(obj.getString("Mark"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PAINT"))
                  .append("</td><td>")
                  .append(obj.getString("Paint"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("CUSTOMER_CODE"))
                  .append("</td><td>")
                  .append(obj.getString("CustomerCode"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("CUSTOMER_NAME"))
                  .append("</td><td>")
                  .append(obj.getString("CustomerName"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_DATE"))
                  .append("</td><td>")
                  .append(LocaleUtils.getDate(obj.getLong("OrderDate"), locale))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("DEMAND_DATE"))
                  .append("</td><td>")
                  .append(LocaleUtils.getDate(obj.getLong("DemandDate"), locale))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_STATUS"))
                  .append("</td><td>")
                  .append(obj.getString("OrderStatus"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_STATUS"))
                  .append("</td><td>")
                  .append(obj.getString("ProjectStatus"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_BLOCK_REASON"))
                  .append("</td><td>")
                  .append(obj.getString("ProjectBlockReason"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_COMMENT"))
                  .append("</td><td>")
                  .append(obj.getString("ProjectComment"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("DAYS_COUNT"))
                  .append("</td><td>")
                  .append(obj.getInteger("DaysCount"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("DAYS_LEFT"))
                  .append("</td><td>")
                  .append(obj.getInteger("DaysLeft"))
                  .append("</td></tr>");
              msg.append("</tbody></table>");
              msg.append(MessageFormat.format(i18nMessage.getString("LINE_OF_TOTAL"), i + 1, list.size()));
              msg.append("<hr />");

            }

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            if (msg.length() > 0) {
              String msgNotes = """
                  <hr />
                  Result only include: 'In progress'
                  <hr />
                  ProjectStatus: 'In hold','In progress','PO Cancelled','Delivered','Customer StandBy';
                  ProjectBlockReason: 'AMENDMENT', 'PAYMENT', 'TECHNICAL DATA', 'DEVIATION REQUEST', 'QUALIFICATION','WAITING TOOL';
                  """;
              MailService.sendEmail(
                  "[SageAssistant]" + "[" + site + "]" +
                      MessageFormat.format(i18nMessage.getString(jobName), days) + ' ' +
                      MessageFormat.format(i18nMessage.getString("TOTAL_LINE"), list.size()),
                  msg.toString() + msgNotes,
                  mailTo + newMailTo,
                  mailCc);
            }
          }
        }).onFailure(err -> {
          log.error("Error long time SO actions line for site {}: {}", site, err.getMessage());
        });
  }

}
