/*********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                            *
 * @CreatedDate           : 2025-07-02 17:08:05                                                                      *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                            *
 * @LastEditDate          : 2025-11-07 12:31:25                                                                      *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                          *
 ********************************************************************************************************************/

package com.da.sage.notice.service;

import java.util.Date;
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.da.sage.notice.utils.PackageUtils;
import com.da.sage.notice.utils.ScheduleViewer;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SchedulerService {
  private static Scheduler scheduler = null;

  public static void init(Vertx vertx) {
    JsonObject notices = vertx.getOrCreateContext().config().getJsonObject("notices", new JsonObject());

    try {
      scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();

      var jobList = PackageUtils.getClassesInJarPackage("com.da.sage.notice.jobs");
      for (Class<?> jobClass : jobList) {
        String jobName = jobClass.getSimpleName();

        // Skip if the class is not configured in notices
        if (!notices.containsKey(jobName)) {
          log.warn("Job {} is not configured in notices, skipping.", jobName);
          continue;
        }

        log.info("Scheduling job: {}", jobName);
        JsonArray sitesConfig = notices.getJsonArray(jobName);

        for (int i = 0; i < sitesConfig.size(); i++) {
          JsonObject config = sitesConfig.getJsonObject(i);
          // Validate required fields
          String site = config.getString("site");
          String cron = config.getString("cron");
          if (site == null || site.isEmpty()) {
            log.error("Job {} is missing site configuration, skipping.", jobName);
            continue;
          }
          if (cron == null || cron.isEmpty()) {
            log.error("Job {} is missing cron configuration, skipping.", jobName);
            continue;
          }

          log.info("Job {} [{}] config: \n{}", jobName, site, config.encodePrettily());
          JobDataMap jobData = new JobDataMap();
          jobData.putAll(config.getMap());

          // String uuid = UUID.randomUUID().toString();
          @SuppressWarnings("unchecked")
          JobDetail job = JobBuilder.newJob((Class<? extends org.quartz.Job>) jobClass)
              .withIdentity(jobName + '-' + site, "SageNotice")
              .usingJobData(jobData) // Example job data, can be customized
              .build();

          Trigger trigger = TriggerBuilder.newTrigger()
              .withIdentity(jobName + '-' + site, "SageNotice")
              .withSchedule(CronScheduleBuilder.cronSchedule(cron))
              .build();
          scheduler.scheduleJob(job, trigger);
        }

        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 24 * 60 * 60 * 1000 * 7); // Next 7 days
        ScheduleViewer viewer = new ScheduleViewer(scheduler);
        viewer.displayTriggersInRange(startTime, endTime);
      }
    } catch (SchedulerException e) {
      log.error("Error starting scheduler {}", e.getMessage());
    }
  }

  public static void run(String jobName) {
    try {
      Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.anyGroup());
      for (var key : jobKeySet) {
        if (key.getName().startsWith(jobName)) {
          log.info("{}", key.getName());
          scheduler.triggerJob(key);
        }
      }
    } catch (SchedulerException e) {
      log.error(e.getMessage());
    }
  }

  public static void runAll() {
    try {
      Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.anyGroup());
      for (var key : jobKeySet) {
        log.info("{}", key.getName());
        scheduler.triggerJob(key);
      }
    } catch (SchedulerException e) {
      log.error(e.getMessage());
    }
  }
}
