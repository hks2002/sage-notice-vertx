/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-10 12:12:39                                                                      *
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
public class DuplicatedRA implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "DUPLICATE_PURCHASE_RECEIPT";

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
    DB.queryByFile("DuplicatedRA", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No duplicated RAs found for site: {}", site);
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
                  .append(MessageFormat.format(i18nMessage.getString("N_RECEIPT_NO"), obj.getString("Seq")))
                  .append("</td><td>")
                  .append(obj.getString("ReceiptNO"))
                  .append("-")
                  .append(obj.getString("ReceiptLine"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(MessageFormat.format(i18nMessage.getString("N_RECEIPT_DATE"), obj.getString("Seq")))
                  .append("</td><td>")
                  .append(LocaleUtils.getDate(obj.getLong("ReceiptDate"), locale))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(MessageFormat.format(i18nMessage.getString("N_RECEIPT_RECEIPTOR"), obj.getString("Seq")))
                  .append("</td><td>")
                  .append(obj.getString("Receiptor"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(MessageFormat.format(i18nMessage.getString("N_RECEIPT_QTY"), obj.getString("Seq")))
                  .append("</td><td>")
                  .append((obj.getInteger("ReceiptQty")))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(MessageFormat.format(i18nMessage.getString("N_RECEIPT_AMOUNT"), obj.getString("Seq")))
                  .append("</td><td>")
                  .append(LocaleUtils.getCurrency(obj.getFloat("ReceiptAmount"), locale))
                  .append(" ")
                  .append(obj.getString("Currency"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_NO"))
                  .append("</td><td>")
                  .append(obj.getString("PurchaseNO"))
                  .append("-")
                  .append(obj.getString("PurchaseLine"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_DATE"))
                  .append("</td><td>")
                  .append(LocaleUtils.getDate(obj.getLong("PurchaseDate"), locale))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("PURCHASE_PURCHASER"))
                  .append("</td><td>")
                  .append(obj.getString("Purchaser"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("TOTAL_RECEIPT_QTY_BY_PROJECT"))
                  .append("</td><td>")
                  .append(obj.getInteger("TotalReceiptQty"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("TOTAL_PURCHASE_QTY_BY_PROJECT"))
                  .append("</td><td>")
                  .append(obj.getInteger("TotalPurchaseQty"))
                  .append("</td></tr>");
              msg.append("<tr><td>")
                  .append(i18nMessage.getString("TOTAL_SALES_QTY_BY_PROJECT"))
                  .append("</td><td>")
                  .append(obj.getInteger("TotalSalesQty"))
                  .append("</td></tr>");
              msg.append("</tbody></table>");
              msg.append(MessageFormat.format(i18nMessage.getString("LINE_OF_TOTAL"), i + 1, list.size()));
              msg.append("<hr />");

              newMailTo += ";" + obj.getString("PurchaserMail");
            }

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            if (msg.length() > 0) {
              msg.append("<hr />");
              msg.append(i18nMessage.getString("HOW_TO_DISABLE_DUPLICATE_RECEIPT_NOTICE"));

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
          log.error("Error checking duplicated POs for site {}: {}", site, err.getMessage());
        });
  }

}
