/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 14:16:48                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.da.sage.notice.db.DB;
import com.da.sage.notice.jobs.base.BaseJob;
import com.da.sage.notice.service.MailService;
import com.da.sage.notice.utils.L;
import com.da.sage.notice.utils.TD;
import com.da.sage.notice.utils.TH;

import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProjectProfit extends BaseJob {
  @Override
  public void executeJob(JobExecutionContext context) throws JobExecutionException {
    jobName = "PROJECT_PROFIT_ANALYSIS";

    String profitRate = Optional.ofNullable(config.getString("profitRate")).orElse("2.0");

    params.put("ProfitRate", profitRate);
    DB.queryByFile(this.getClass().getSimpleName(), params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No Projects found for site: {}, profit rate bellow {}", site, profitRate);
          } else {
            StringBuilder msg = new StringBuilder();
            String totalTxt = MessageFormat.format(i18n.getString("TOTAL_LINE"), list.size());
            Set<String> projects = new HashSet<>();
            String moreMailTo = "";

            msg.append("<table>")
                .append("<thead>")
                .append("<tr>")
                .append("<th>#</th>")
                .append(TH.N(i18n.getString("PROJECT_NO")))
                .append(TH.N(i18n.getString("ORDER_NO")))
                .append(TH.N(i18n.getString("PRODUCT_FAMILY")))
                .append(TH.N(i18n.getString("PN")))
                .append(TH.N(i18n.getString("DESCRIPTION")))
                .append(TH.N(i18n.getString("QTY")))
                .append(TH.N(i18n.getString("ORDER_DATE")))
                .append(TH.N(i18n.getString("NET_PRICE")))
                .append(TH.N(i18n.getString("NET_PRICE_WITH_TAX")))
                .append(TH.N(i18n.getString("SALES_AMOUNT")))
                .append(TH.N(i18n.getString("SALES_AMOUNT_WITH_TAX")))
                .append(TH.N(i18n.getString("SALES_LOCAL_AMOUNT")))
                .append(TH.N(i18n.getString("SALES_LOCAL_AMOUNT_WITH_TAX")))
                .append(TH.N(i18n.getString("CURRENCY")))
                .append(TH.N(i18n.getString("LOCAL_CURRENCY")))
                .append(TH.N(i18n.getString("CURRENCY_RATE")))
                .append(TH.N(i18n.getString("PURCHASE_LOCAL_AMOUNT_WITH_TAX")))
                .append(TH.N(i18n.getString("PROJECT_PROFIT")))
                .append(TH.N(i18n.getString("PROJECT_PROFIT_RATE")))
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
                  .append(TD.R(L.getCurrency(obj.getFloat("NetPrice"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("NetPriceWithTax"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("SalesAmount"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("SalesAmountWithTax"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("SalesLocalAmount"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("SalesLocalAmountWithTax"), locale)))
                  .append(TD.N(obj.getString("Currency")))
                  .append(TD.N(obj.getString("LocalCurrency")))
                  .append(TD.R(L.getNumber(obj.getFloat("Rate"), locale)))
                  .append(TD.R(L.getCurrency(obj.getFloat("PurchaseLocalAmountWithTax"), locale)));

              if (obj.getDouble("Profit") < 0) {
                msg
                    .append(TD.R(L.getCurrency(obj.getFloat("Profit"), locale), "red"))
                    .append(TD.R(L.getNumber(obj.getFloat("ProfitRate"), locale), "red"));
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
            msg.append(MessageFormat.format(i18n.getString("NOTE_FOR_PROFIT_RATE"), profitRate));

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" + i18n.getString(jobName) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString(),
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error Projects found for site: {}, profit rate bellow {}: {}", site, profitRate, err.getMessage());
        });
  }

}
