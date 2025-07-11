/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-10 12:04:12                                                                      *
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
public class DeadPurchaseLine implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "DEAD_PURCHASE_LINE";

    // Retrieve job parameters
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    log.debug("Executing job {} with data: {}", jobName, jobDataMap);
    String site = Optional.ofNullable(jobDataMap.getString("site")).orElse("---");
    String language = Optional.ofNullable(jobDataMap.getString("language")).orElse("en_US");
    String mailTo = Optional.ofNullable(jobDataMap.getString("mailTo")).orElse("");
    String mailCc = Optional.ofNullable(jobDataMap.getString("mailCc")).orElse("");

    Locale locale = LocaleUtils.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    DB.queryByFile("DeadPurchaseLine", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No dead purchase line found for site: {}", site);
          } else {
            StringBuilder msg = new StringBuilder();
            String newMailTo = "";
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              msg.append("<table><tbody>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_NO"))
                  .append("</td><td>")
                  .append(obj.getString("PurchaseNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_LINE"))
                  .append("</td><td>")
                  .append(obj.getString("PurchaseLine"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_NO"))
                  .append("</td><td>")
                  .append(obj.getString("ProjectNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_PN"))
                  .append("</td><td>")
                  .append(obj.getString("PurchasePN"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("DESCRIPTION"))
                  .append("</td><td>")
                  .append(obj.getString("Description"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_QTY"))
                  .append("</td><td>")
                  .append(obj.getInteger("PurchaseQty"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_AMOUNT"))
                  .append("</td><td>")
                  .append(LocaleUtils.getCurrency(obj.getFloat("PurchaseAmount"), locale))
                  .append(" ")
                  .append(obj.getString("PurchaseCurrency"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_DATE"))
                  .append("</td><td>")
                  .append(LocaleUtils.getDate(obj.getLong("PurchaseDate"), locale))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASER"))
                  .append("</td><td>")
                  .append(obj.getString("Purchaser"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_NO"))
                  .append("</td><td>")
                  .append(obj.getString("OrderNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("SALES_PN"))
                  .append("</td><td>")
                  .append(obj.getString("SalesPN"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("SALES_QTY"))
                  .append("</td><td>")
                  .append(obj.getInteger("SalesQty"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_DATE"))
                  .append("</td><td>")
                  .append(LocaleUtils.getDate(obj.getLong("OrderDate"), locale))
                  .append("</td></tr>");
              msg.append("</tbody></table>");
              msg.append(MessageFormat.format(i18nMessage.getString("LINE_OF_TOTAL"), i + 1, list.size()));
              msg.append("<hr />");

              newMailTo += ";" + obj.getString("PurchaserEmail");
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
          log.error("Error dead purchase line for site {}: {}", site, err.getMessage());
        });
  }

}
