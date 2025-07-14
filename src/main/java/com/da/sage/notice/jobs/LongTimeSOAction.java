/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-14 18:28:39                                                                      *
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

    Locale locale = L.getLocale(language);
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
            String totalTxt = MessageFormat.format(i18nMessage.getString("TOTAL_LINE"), list.size());
            Set<String> projects = new HashSet<>();
            String moreMailTo = "";

            msg.append("<table>")
                .append("<thead>")
                .append("<tr>")
                .append("<th>#</th>")
                .append(TH.N(i18nMessage.getString("PROJECT_NO")))
                .append(TH.N(i18nMessage.getString("SALES_ORDER_NO")))
                .append(TH.N(i18nMessage.getString("ORDER_TYPE")))
                .append(TH.N(i18nMessage.getString("PN")))
                .append(TH.N(i18nMessage.getString("DESCRIPTION")))
                .append(TH.N(i18nMessage.getString("QTY")))
                .append(TH.N(i18nMessage.getString("UNIT")))
                .append(TH.N(i18nMessage.getString("ORDER_PRICE")))
                .append(TH.N(i18nMessage.getString("CURRENCY")))
                .append(TH.N(i18nMessage.getString("MARK")))
                .append(TH.N(i18nMessage.getString("PAINT")))
                .append(TH.N(i18nMessage.getString("CUSTOMER_CODE")))
                .append(TH.N(i18nMessage.getString("CUSTOMER_NAME")))
                .append(TH.N(i18nMessage.getString("ORDER_DATE")))
                .append(TH.N(i18nMessage.getString("DEMAND_DATE")))
                .append(TH.N(i18nMessage.getString("ORDER_STATUS")))
                .append(TH.N(i18nMessage.getString("PROJECT_STATUS")))
                .append(TH.N(i18nMessage.getString("PROJECT_BLOCK_REASON")))
                .append(TH.N(i18nMessage.getString("PROJECT_COMMENT")))
                .append(TH.N(i18nMessage.getString("DAYS_COUNT")))
                .append(TH.N(i18nMessage.getString("DAYS_LEFT")))
                .append("</tr>")
                .append("</thead>");

            msg.append("<tbody>");
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              projects.add(obj.getString("ProjectNO"));

              msg.append("<tr>")
                  .append(TD.N((i + 1)))
                  .append(TD.N(obj.getString("ProjectNO")))
                  .append(TD.N(obj.getString("SalesOrderNO")))
                  .append(TD.N(obj.getString("OrderType")))
                  .append(TD.N(obj.getString("PN")))
                  .append(TD.N(obj.getString("Description")))
                  .append(TD.C(obj.getInteger("QTY")))
                  .append(TD.N(obj.getString("Unit")))
                  .append(TD.R(L.getCurrency(obj.getFloat("OrderPrice"), locale)))
                  .append(TD.N(obj.getString("OrderCurrency")))
                  .append(TD.N(obj.getString("Mark")))
                  .append(TD.N(obj.getString("Paint")))
                  .append(TD.N(obj.getString("CustomerCode")))
                  .append(TD.N(obj.getString("CustomerName")))
                  .append(TD.N(L.getDate(obj.getLong("OrderDate"), locale)))
                  .append(TD.N(L.getDate(obj.getLong("DemandDate"), locale)))
                  .append(TD.N(obj.getString("OrderStatus")))
                  .append(TD.N(obj.getString("ProjectStatus")))
                  .append(TD.N(obj.getString("ProjectBlockReason")))
                  .append(TD.N(obj.getString("ProjectComment")))
                  .append(TD.C(obj.getInteger("DaysCount")))
                  .append(TD.C(obj.getInteger("DaysLeft")))
                  .append("</tr>");

            }
            msg.append("</tbody>")
                .append("</table>");

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());
            String msgNotes = """
                <hr />
                Result only include: 'In progress'
                <hr />
                ProjectStatus: 'In hold','In progress','PO Cancelled','Delivered','Customer StandBy';
                ProjectBlockReason: 'AMENDMENT', 'PAYMENT', 'TECHNICAL DATA', 'DEVIATION REQUEST', 'QUALIFICATION','WAITING TOOL';
                """;
            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" + i18nMessage.getString(jobName) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString() + msgNotes,
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error long time SO actions line for site {}: {}", site, err.getMessage());
        });
  }

}
