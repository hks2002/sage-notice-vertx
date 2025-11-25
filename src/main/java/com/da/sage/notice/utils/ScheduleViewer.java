/**********************************************************************************************************************
 * @Author                : Robert Huang<56649783@qq.com>                                                             *
 * @CreatedDate           : 2025-11-07 11:18:19                                                                       *
 * @LastEditors           : Robert Huang<56649783@qq.com>                                                             *
 * @LastEditDate          : 2025-11-07 11:55:33                                                                       *
 * @CopyRight             : Dedienne Aerospace China ZhuHai                                                           *
 *********************************************************************************************************************/

package com.da.sage.notice.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.TriggerUtils;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.OperableTrigger;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ScheduleViewer {

  private final Scheduler scheduler;

  public ScheduleViewer(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  // Helper class to store trigger time information
  private static class TriggerTime implements Comparable<TriggerTime> {
    final Date time;
    final String triggerName;
    final String triggerGroup;
    final String jobName;
    final String jobGroup;

    TriggerTime(Date time, String triggerName, String triggerGroup, String jobName, String jobGroup) {
      this.time = time;
      this.triggerName = triggerName;
      this.triggerGroup = triggerGroup;
      this.jobName = jobName;
      this.jobGroup = jobGroup;
    }

    @Override
    public int compareTo(TriggerTime other) {
      return this.time.compareTo(other.time);
    }
  }

  // Show scheduled triggers in a specific time range
  public void displayTriggersInRange(Date startTime, Date endTime) throws SchedulerException {
    List<TriggerTime> triggerTimes = new ArrayList<>();

    for (String group : scheduler.getTriggerGroupNames()) {
      for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
        Trigger trigger = scheduler.getTrigger(triggerKey);
        JobDetail jobDetail = scheduler.getJobDetail(trigger.getJobKey());

        List<Date> fireTimes = TriggerUtils.computeFireTimesBetween(
            (OperableTrigger) trigger,
            null,
            startTime,
            endTime);

        for (Date fireTime : fireTimes) {
          triggerTimes.add(new TriggerTime(
              fireTime,
              triggerKey.getName(),
              triggerKey.getGroup(),
              jobDetail.getKey().getName(),
              jobDetail.getKey().getGroup()));
        }
      }
    }

    Collections.sort(triggerTimes);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    log.info("Displaying triggers from {} to {}", dateFormat.format(startTime), dateFormat.format(endTime));
    for (TriggerTime tt : triggerTimes) {
      log.info("{}\t\t\t{}", dateFormat.format(tt.time), tt.jobName);
      log.debug("{}\t\t\t{}", dateFormat.format(tt.time), tt.jobGroup);
      log.debug("{}\t\t\t{}", dateFormat.format(tt.time), tt.triggerName);
      log.debug("{}\t\t\t{}", dateFormat.format(tt.time), tt.triggerGroup);
    }

  }
}
