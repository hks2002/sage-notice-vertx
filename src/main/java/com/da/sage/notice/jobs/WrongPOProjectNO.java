/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-07-14 18:30:44                                                                      *
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
public class WrongPOProjectNO implements Job {
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = "WRONG_PO_PROJECT";

    // Retrieve job parameters
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    log.debug("Executing job {} with data: {}", jobName, jobDataMap);
    String site = Optional.ofNullable(jobDataMap.getString("site")).orElse("---");
    String language = Optional.ofNullable(jobDataMap.getString("language")).orElse("en_US");
    String mailTo = Optional.ofNullable(jobDataMap.getString("mailTo")).orElse("");
    String mailCc = Optional.ofNullable(jobDataMap.getString("mailCc")).orElse("");

    Locale locale = L.getLocale(language);
    ResourceBundle i18nMessage = ResourceBundle.getBundle("messages", locale);

    JsonObject params = new JsonObject();
    params.put("Site", site);
    DB.queryByFile("WrongPOProjectNO", params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No wrong PO project found for site: {}", site);
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
                .append(TH.N(i18nMessage.getString("PURCHASE_LINE")))
                .append(TH.N(i18nMessage.getString("PROJECT_NO")))
                .append(TH.N(i18nMessage.getString("PN")))
                .append(TH.N(i18nMessage.getString("DESCRIPTION")))
                .append(TH.N(i18nMessage.getString("CREATE_USER")))
                .append(TH.N(i18nMessage.getString("CREATE_DATE")))
                .append("</tr>")
                .append("</thead>");

            msg.append("<tbody>");
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              projects.add(obj.getString("ProjectNO"));

              msg.append("<tr>")
                  .append(TD.N((i + 1)))
                  .append(TD.N(obj.getInteger("PurchaseNO")))
                  .append(TD.N(obj.getString("PurchaseNO")))
                  .append(TD.N(obj.getString("ProjectNO")))
                  .append(TD.N(obj.getString("PN")))
                  .append(TD.N(obj.getString("Description")))
                  .append(TD.N(obj.getString("CreateUser")))
                  .append(TD.N(L.getDate(obj.getLong("CreateDate"), locale)))
                  .append("</tr>");

              moreMailTo += ";" + obj.getString("CreateUserEmail");
            }
            msg.append("</tbody>")
                .append("</table>");

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            msg.append("<hr />");
            msg.append(i18nMessage.getString("HOW_TO_DISABLE_DUPLICATE_PO_NOTICE"));

            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" + i18nMessage.getString(jobName) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString(),
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error wrong PO project for site {}: {}", site, err.getMessage());
        });
  }

}
