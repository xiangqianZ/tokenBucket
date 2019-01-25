package com.token.bucket.quartz;

import com.token.bucket.bucket.TokenBucket;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

/**
 * Created by 赵亚辉 on 2017/12/18.
 */
@Component
@EnableConfigurationProperties(TokenBucket.class)
public class SchedulerAllJob {

    @Autowired
//    @Lazy
    private SchedulerFactoryBean schedulerFactoryBean;

    /*
     * 此处可以注入数据库操作，查询出所有的任务配置
     */

    /**
     * 该方法用来启动所有的定时任务
     * @throws SchedulerException
     */
    public void scheduleJobs()  {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {

            scheduleJob1(scheduler);

            scheduler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop()  {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        try {

            scheduler.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置Job1
     * 此处的任务可以配置可以放到properties或者是放到数据库中
     * @param scheduler
     * @throws SchedulerException
     */
    private void scheduleJob1(Scheduler scheduler) {
        /*
         *  此处可以先通过任务名查询数据库，如果数据库中存在该任务，则按照ScheduleRefreshDatabase类中的方法，更新任务的配置以及触发器
         *  如果此时数据库中没有查询到该任务，则按照下面的步骤新建一个任务，并配置初始化的参数，并将配置存到数据库中
         */
        try {

            JobDetail jobDetail = JobBuilder.newJob(AddToken.class) .withIdentity("addTokenJob", "tokenGroup").build();
            // 每5s执行一次
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("*/5 * * * * ?");
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "tokenGroup") .withSchedule(scheduleBuilder).build();
            scheduler.scheduleJob(jobDetail,cronTrigger);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
