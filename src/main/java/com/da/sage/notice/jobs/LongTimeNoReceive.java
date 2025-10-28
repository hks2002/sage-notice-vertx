/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-10-28 18:39:47                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.da.sage.notice.db.DB;
import com.da.sage.notice.service.MailService;
import com.da.sage.notice.utils.L;
import com.da.sage.notice.utils.TD;
import com.da.sage.notice.utils.TH;

import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LongTimeNoReceive implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "LONG_TIME_NO_RECEIVE";

    // Retrieve job parameters
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    log.debug("Executing job {} with data: {}", jobName, jobDataMap);
    String site = Optional.ofNullable(jobDataMap.getString("site")).orElse("---");
    String language = Optional.ofNullable(jobDataMap.getString("language")).orElse("en_US");
    String days = Optional.ofNullable(jobDataMap.getString("days")).orElse("30");
    String mailTo = Optional.ofNullable(jobDataMap.getString("mailTo")).orElse("");
    String mailCc = Optional.ofNullable(jobDataMap.getString("mailCc")).orElse("");

    Locale locale = L.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    params.put("Days", days);

    DB.queryByFile("LongTimeNoReceive", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No long time no receive found for site: {}", site);
          } else {
            StringBuilder msg = new StringBuilder();
            String totalTxt = MessageFormat.format(i18nMessage.getString("TOTAL_LINE"), list.size());
            Set<String> projects = new HashSet<>();
            String moreMailTo = "";

            msg.append("<table>")
                .append("<thead>")
                .append("<tr>")
                .append("<th>#</th>")
                .append(TH.N(i18nMessage.getString("PURCHASE_NO")))
                .append(TH.N(i18nMessage.getString("LINE")))
                .append(TH.N(i18nMessage.getString("PROJECT_NO")))
                .append(TH.N(i18nMessage.getString("PN")))
                .append(TH.N(i18nMessage.getString("DESCRIPTION")))
                .append(TH.N(i18nMessage.getString("QTY")))
                .append(TH.N(i18nMessage.getString("UNIT")))
                .append(TH.N(i18nMessage.getString("NET_PRICE")))
                .append(TH.N(i18nMessage.getString("CURRENCY")))
                .append(TH.N(i18nMessage.getString("VENDOR_CODE")))
                .append(TH.N(i18nMessage.getString("VENDOR_NAME")))
                .append(TH.N(i18nMessage.getString("ORDER_DATE")))
                .append(TH.N(i18nMessage.getString("ACK_DATE")))
                .append(TH.N(i18nMessage.getString("EXPECT_DATE")))
                .append(TH.N(i18nMessage.getString("PURCHASER")))
                .append(TH.N(i18nMessage.getString("DAYS_LEFT")))
                .append("</tr>")
                .append("</thead>");

            msg.append("<tbody>");
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              projects.add(obj.getString("ProjectNO"));

              msg.append("<tr>")
                  .append(TD.N((i + 1)))
                  .append(TD.N(obj.getString("PurchaseNO")))
                  .append(TD.N(obj.getString("Line")))
                  .append(TD.N(obj.getString("ProjectNO")))
                  .append(TD.N(obj.getString("PN")))
                  .append(TD.N(obj.getString("Description")))
                  .append(TD.C(obj.getInteger("QTY")))
                  .append(TD.N(obj.getString("Unit")))
                  .append(TD.R(L.getCurrency(obj.getFloat("NetPrice"), locale)))
                  .append(TD.N(obj.getString("Currency")))
                  .append(TD.N(obj.getString("VendorCode")))
                  .append(TD.N(obj.getString("VendorName")))
                  .append(TD.N(L.getDate(obj.getLong("OrderDate"), locale)))
                  .append(TD.N(L.getDate(obj.getLong("AckDate"), locale)))
                  .append(TD.N(L.getDate(obj.getLong("ExpectDate"), locale)))
                  .append(TD.N(obj.getString("Purchaser")))
                  .append(TD.C(obj.getInteger("DaysLeft")))
                  .append("</tr>");

              moreMailTo += ";" + obj.getString("PurchaserEmail");
            }
            msg.append("</tbody>")
                .append("</table>");

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());
            String msgNotes = "";
            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" +
                    MessageFormat.format(i18nMessage.getString(jobName), days) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString() + msgNotes,
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error long time no receive line for site {}: {}", site, err.getMessage());
        });
  }

}
