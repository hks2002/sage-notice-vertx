/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 14:09:55                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.jobs;

import java.text.MessageFormat;
import java.util.HashSet;
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
public class DuplicatedRA extends BaseJob {
  @Override
  public void executeJob(JobExecutionContext context) throws JobExecutionException {
    jobName = "DUPLICATE_PURCHASE_RECEIPT";

    DB.queryByFile(this.getClass().getSimpleName(), params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No duplicated RAs found for site: {}", site);
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
                .append(TH.N(i18n.getString("N_TIMES")))
                .append(TH.N(i18n.getString("PN")))
                .append(TH.N(i18n.getString("DESCRIPTION")))
                .append(TH.N(i18n.getString("RECEIPT_NO")))
                .append(TH.N(i18n.getString("RECEIPT_LINE")))
                .append(TH.N(i18n.getString("RECEIPT_DATE")))
                .append(TH.N(i18n.getString("RECEIPT_RECEIPTOR")))
                .append(TH.N(i18n.getString("RECEIPT_QTY")))
                .append(TH.N(i18n.getString("RECEIPT_AMOUNT")))
                .append(TH.N(i18n.getString("CURRENCY")))
                .append(TH.N(i18n.getString("PURCHASE_NO")))
                .append(TH.N(i18n.getString("PURCHASE_LINE")))
                .append(TH.N(i18n.getString("PURCHASE_DATE")))
                .append(TH.N(i18n.getString("PURCHASE_PURCHASER")))
                .append(TH.N(i18n.getString("TOTAL_RECEIPT_QTY_BY_PROJECT")))
                .append(TH.N(i18n.getString("TOTAL_PURCHASE_QTY_BY_PROJECT")))
                .append(TH.N(i18n.getString("TOTAL_SALES_QTY_BY_PROJECT")))
                .append("</tr>")
                .append("</thead>");

            msg.append("<tbody>");
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              projects.add(obj.getString("ProjectNO"));

              msg.append("<tr>")
                  .append(TD.N((i + 1)))
                  .append(TD.N(obj.getString("ProjectNO")))
                  .append(TD.N(obj.getString("Seq")))
                  .append(TD.N(obj.getString("PN")))
                  .append(TD.N(obj.getString("Description")))
                  .append(TD.N(obj.getString("ReceiptNO")))
                  .append(TD.N(obj.getString("ReceiptLine")))
                  .append(TD.N(L.getDate(obj.getLong("ReceiptDate"), locale)))
                  .append(TD.N(obj.getString("Receiptor")))
                  .append(TD.N(obj.getInteger("ReceiptQty")))
                  .append(TD.R(L.getCurrency(obj.getFloat("ReceiptAmount"), locale)))
                  .append(TD.N(obj.getString("Currency")))
                  .append(TD.N(obj.getString("PurchaseNO")))
                  .append(TD.N(obj.getString("PurchaseLine")))
                  .append(TD.N(L.getDate(obj.getLong("PurchaseDate"), locale)))
                  .append(TD.N(obj.getString("Purchaser")))
                  .append(TD.C(obj.getInteger("TotalReceiptQty")))
                  .append(TD.C(obj.getInteger("TotalPurchaseQty")))
                  .append(TD.C(obj.getInteger("TotalSalesQty")))
                  .append("</tr>");

              moreMailTo += ";" + obj.getString("CreateUserEmail");
            }
            msg.append("</tbody>")
                .append("</table>");

            msg.append("<hr />");
            msg.append(i18n.getString("HOW_TO_DISABLE_DUPLICATE_RECEIPT_NOTICE"));

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" + i18n.getString(jobName) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString(),
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error checking duplicated POs for site {}: {}", site, err.getMessage());
        });
  }

}
