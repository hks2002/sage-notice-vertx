/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-16 14:49:13                                                                      *
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

    Locale locale = L.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    params.put("ProfitRate", profitRate);
    DB.queryByFile("ProjectProfit", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No Projects found for site: {}, profit rate bellow {}", site, profitRate);
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
                .append(TH.N(i18nMessage.getString("ORDER_NO")))
                .append(TH.N(i18nMessage.getString("PRODUCT_FAMILY")))
                .append(TH.N(i18nMessage.getString("PN")))
                .append(TH.N(i18nMessage.getString("DESCRIPTION")))
                .append(TH.N(i18nMessage.getString("QTY")))
                .append(TH.N(i18nMessage.getString("ORDER_DATE")))
                .append(TH.N(i18nMessage.getString("PROJECT_SALES_PRICE")))
                .append(TH.N(i18nMessage.getString("CURRENCY")))
                .append(TH.N(i18nMessage.getString("PROJECT_SALES_LOCAL_PRICE")))
                .append(TH.N(i18nMessage.getString("CURRENCY")))
                .append(TH.N(i18nMessage.getString("CURRENCY_RATE")))
                .append(TH.N(i18nMessage.getString("PROJECT_LOCAL_COST")))
                .append(TH.N(i18nMessage.getString("PROJECT_PROFIT")))
                .append(TH.N(i18nMessage.getString("PROJECT_PROFIT_RATE")))
                .append("</tr>")
                .append("</thead>");

            msg.append("<tbody>");
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              projects.add(obj.getString("ProjectNO"));
              msg.append("<tr>")
                  .append(TD.N((i + 1)))
                  .append(TD.N(obj.getString("ProjectNO")))
                  .append(TD.N(obj.getString("OrderNO")))
                  .append(TD.N(obj.getString("ProductFamily")))
                  .append(TD.N(obj.getString("PN")))
                  .append(TD.N(obj.getString("Description")))
                  .append(TD.C(obj.getInteger("QTY")))
                  .append(TD.N(L.getDate(obj.getLong("OrderDate"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("ProjectSalesPrice"), locale)))
                  .append(TD.N(obj.getString("SalesCurrency")))
                  .append(TD.R(L.getCurrency(obj.getFloat("ProjectSalesLocalPrice"), locale)))
                  .append(TD.N(obj.getString("LocalCurrency")))
                  .append(TD.R(L.getNumber(obj.getFloat("Rate"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("ProjectLocalCost"), locale)));

              if (obj.getDouble("Profit") < 0) {
                msg
                    .append("<td style=\"color: red;\">")
                    .append(TD.R(L.getCurrency(obj.getFloat("Profit"), locale)))
                    .append("</td><td style=\"color: red;\">")
                    .append(TD.R(L.getNumber(obj.getFloat("ProfitRate"), locale)))
                    .append("</td>");
              } else {
                msg
                    .append(TD.R(L.getCurrency(obj.getFloat("Profit"), locale)))
                    .append(TD.R(L.getNumber(obj.getFloat("ProfitRate"), locale)));
              }

              msg.append("</tr>");

            }
            msg.append("</tbody>")
                .append("</table>");

            msg.append("<hr />");
            msg.append(MessageFormat.format(i18nMessage.getString("NOTE_FOR_PROFIT_RATE"), profitRate));

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" + i18nMessage.getString(jobName) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString(),
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error Projects found for site: {}, profit rate bellow {}: {}", site, profitRate, err.getMessage());
        });
  }

}
