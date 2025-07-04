/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-04 11:30:38                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs;

import java.sql.Date;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
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
public class ProjectProfit implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "PROJECT_PROFIT_ANALYSIS";

    // Retrieve job parameters
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    log.debug("Executing job {} with data: {}", jobName, jobDataMap);
    String site = Optional.ofNullable(jobDataMap.getString("site")).orElse("---");
    String profitRate = Optional.ofNullable(jobDataMap.getString("profitRate")).orElse("2.0");
    String language = Optional.ofNullable(jobDataMap.getString("language")).orElse("en_US");
    String mailTo = Optional.ofNullable(jobDataMap.getString("mailTo")).orElse("");
    String mailCc = Optional.ofNullable(jobDataMap.getString("mailCc")).orElse("");

    Locale locale = LocaleUtils.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
    NumberFormat numberFormat = NumberFormat.getInstance(locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    params.put("ProfitRate", profitRate);
    DB.queryByFile("ProjectProfit", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No Projects found for site: {}, profit rate bellow {}", site, profitRate);
          } else {
            StringBuilder msg = new StringBuilder();
            String newMailTo = "";
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              msg.append("<hr />");
              msg.append(MessageFormat.format("LINE_OF_TOTAL", i + 1, list.size()));
              msg.append("<table><tbody>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_NO"))
                  .append("</td><td>")
                  .append(obj.getString("ProjectNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_NO"))
                  .append("</td><td>")
                  .append(obj.getString("OrderNO"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PRODUCT_FAMILY"))
                  .append("</td><td>")
                  .append(obj.getString("ProductFamily"))
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
                  .append(numberFormat.format(obj.getDouble("QTY")))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("ORDER_DATE"))
                  .append("</td><td>")
                  .append(dateFormat.format(new Date(obj.getLong("OrderDate"))))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_SALES_PRICE"))
                  .append("</td><td>")
                  .append(numberFormat.format(obj.getDouble("ProjectSalesPrice")))
                  .append(" ")
                  .append(obj.getString("SalesCurrency"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_SALES_LOCAL_PRICE"))
                  .append("</td><td>")
                  .append(numberFormat.format(obj.getDouble("ProjectSalesLocalPrice")))
                  .append(" ")
                  .append(obj.getString("LocalCurrency"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("CURRENCY_RATE"))
                  .append("</td><td>")
                  .append(numberFormat.format(obj.getDouble("Rate")))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PROJECT_LOCAL_COST"))
                  .append("</td><td>")
                  .append(numberFormat.format(obj.getDouble("ProjectLocalCost")))
                  .append(" ")
                  .append(obj.getString("LocalCurrency"))
                  .append("</td></tr>");
              if (obj.getDouble("Profit") < 0) {
                msg.append("<tr><td style=\"color: red;\">")
                    .append(i18nMessage.getString("PROJECT_PROFIT"))
                    .append("</td><td style=\"color: red;\">")
                    .append(numberFormat.format(obj.getDouble("Profit")))
                    .append("</td></tr>");
                msg.append("<tr><td style=\"color: red;\">")
                    .append(i18nMessage.getString("PROJECT_PROFIT_RATE"))
                    .append("</td><td style=\"color: red;\">")
                    .append(numberFormat.format(obj.getDouble("ProfitRate")))
                    .append("</td></tr>");
              } else {
                msg.append("<tr><td>")
                    .append(i18nMessage.getString("PROJECT_PROFIT"))
                    .append("</td><td>")
                    .append(numberFormat.format(obj.getDouble("Profit")))
                    .append("</td></tr>");
                msg.append("<tr><td>")
                    .append(i18nMessage.getString("PROJECT_PROFIT_RATE"))
                    .append("</td><td>")
                    .append(numberFormat.format(obj.getDouble("ProfitRate")))
                    .append("</td></tr>");
              }
              msg.append("</tbody></table>");

              newMailTo += ";" + obj.getString("PurchaserMail");
            }

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            if (msg.length() > 0) {
              msg.append("<hr />");
              msg.append(i18nMessage.getString("NOTE_FOR_PROFIT_RATE"));

              MailService.sendEmail(
                  "[SageAssistant]" + "[" + site + "]" +
                      i18nMessage.getString(jobName) + ' ' +
                      MessageFormat.format("TOTAL_LINE", list.size()),
                  msg.toString(),
                  mailTo + newMailTo,
                  mailCc);
            }
          }
        }).onFailure(err -> {
          log.error("Error Projects found for site: {}, profit rate bellow {}: {}", site, profitRate, err.getMessage());
        });
  }

}
