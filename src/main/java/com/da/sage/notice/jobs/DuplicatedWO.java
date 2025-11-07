/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 15:18:33                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 14:12:42                                                                      *
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
public class DuplicatedWO extends BaseJob {
  @Override
  public void executeJob(JobExecutionContext context) throws JobExecutionException {
    jobName = "DUPLICATE_WORK_ORDER";

    DB.queryByFile(this.getClass().getSimpleName(), params)
        .onSuccess(list -> {
          if (list.isEmpty()) {
            log.info("No duplicated WO found for site: {}", site);
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
                .append(TH.N(i18n.getString("WO")))
                .append(TH.N(i18n.getString("CREATE_USER")))
                .append(TH.N(i18n.getString("CREATE_DATE")))
                .append("</tr>")
                .append("</thead>");

            msg.append("<tbody>");
            for (int i = 0; i < list.size(); i++) {
              JsonObject obj = list.get(i);

              projects.add(obj.getString("ProjectNO"));

              msg.append("<tr>")
                  .append(TD.N((i + 1)))
                  .append(TD.N(obj.getString("ProjectNO")))
                  .append(TD.N(obj.getString("WO")))
                  .append(TD.N(obj.getString("CreateUser")))
                  .append(TD.N(L.getDate(obj.getLong("CreateDate"), locale)))
                  .append("</tr>");

              moreMailTo += ";" + obj.getString("CreateUserEmail");
            }
            msg.append("</tbody>")
                .append("</table>");

            log.debug("{} [{}]\n{}", jobName, site, msg.toString());

            MailService.sendEmail(
                "[SageAssistant]" + "[" + site + "]" + i18n.getString(jobName) + ' ' + totalTxt,
                String.join(" ", projects) + "<hr />" + msg.toString(),
                mailTo + moreMailTo,
                mailCc);

          }
        }).onFailure(err -> {
          log.error("Error checking duplicated WO for site {}: {}", site, err.getMessage());
        });
  }

}
