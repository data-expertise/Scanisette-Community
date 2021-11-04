/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scanisette.daemons;


import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.DateBuilder.IntervalUnit.HOUR;
import static org.quartz.DateBuilder.IntervalUnit.SECOND;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;

import org.quartz.*;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static scanisette.Scanisette.*;

import org.quartz.impl.StdSchedulerFactory;
import scanisette.Scanisette;
import scanisette.models.StreamGobbler;

import java.util.List;
import java.util.concurrent.ExecutorService;


public class AppScheduler {

    final SchedulerFactory factory = new StdSchedulerFactory();
    Scheduler scheduler = null;

    JobDetail swapScreensJob;
    Trigger swapScreensTrigger;

    JobDetail usbKeyCheckInJob;
    Trigger usbKeyCheckInTrigger;

    JobDetail usbKeyScanJob;
    Trigger usbKeyScanTrigger;

    JobDetail usbKeyCheckSnatchedJob;
    Trigger usbKeyCheckSnatchedTrigger;

    JobDetail usbKeyCheckOutJob;
    Trigger usbKeyCheckOutTrigger;

    JobDetail antivirusGetLastUpdateDateJob;
    Trigger antivirusGetLastUpdateDateTrigger;

    Process process;
    StreamGobbler streamGobbler;
    ExecutorService executor;

    int delay;

    public AppScheduler() {
        super();
        try {
            scheduler = factory.getScheduler();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void init() {
        try {



            // scheduler = factory.getScheduler();


            scheduler.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void swapScreensJobStart() {
        try {
            logger.info("swapScreensJobStart()");
            if (!scheduler.checkExists(JobKey.jobKey("SwapScreens.SSJ"))) {
                logger.info(" -> Creation");
                swapScreensJob = newJob(SwapScreensJob.class)
                        .withIdentity("SSJ", "SwapScreens")
                        .build();

                delay = Integer.parseInt(docConfig.selectSingleNode("/config/scheduler/swapScreensJobDelayInSeconds").getText());
                swapScreensTrigger = newTrigger()
                        .withIdentity("SST", "SwapScreens")
                        .withSchedule(simpleSchedule().withIntervalInSeconds(delay).repeatForever())
                        .startAt(futureDate(delay, SECOND))
                        .build();

                // https://www.geek-share.com/detail/2769504237.html
                scheduler.scheduleJob(swapScreensJob, swapScreensTrigger);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void swapScreensJobStop() {
        try {
            logger.info("swapScreensJobStop()");
            scheduler.unscheduleJob(swapScreensTrigger.getKey());
            scheduler.interrupt(swapScreensJob.getKey());
            scheduler.deleteJob(swapScreensJob.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyCheckInJobStart() {
        try {
            // Test si présence de clé usb toutes les x secondes
            logger.info("usbKeyCheckInJobStart()");
            if (!scheduler.checkExists(JobKey.jobKey("CheckIn.UKCIJ"))) {
                logger.info(" -> Creation");
                usbKeyCheckInJob = newJob(UsbKeyCheckInJob.class)
                        .withIdentity("UKCIJ", "CheckIn")
                        .build();

                delay = Integer.parseInt(docConfig.selectSingleNode("/config/scheduler/usbKeyCheckInJobDelayInSeconds").getText());
                usbKeyCheckInTrigger = newTrigger()
                        .withIdentity("UKCIT", "CheckIn")
                        .withSchedule(simpleSchedule().withIntervalInSeconds(delay).repeatForever())
                        .startNow()
                        //.startAt(futureDate(1, SECOND))
                        .build();

                scheduler.scheduleJob(usbKeyCheckInJob, usbKeyCheckInTrigger);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyCheckInJobStop() {
        try {
            logger.info("usbKeyCheckInJobStop()");
            scheduler.unscheduleJob(usbKeyCheckInTrigger.getKey());
            scheduler.interrupt(usbKeyCheckInJob.getKey());
            scheduler.deleteJob(usbKeyCheckInJob.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyScanJobStart() {
        try {
            logger.info("usbKeyScanJobStart()");
            if (!scheduler.checkExists(JobKey.jobKey("Scan.UKSJ"))) {
                logger.info(" -> Creation");
                // lancement de l'antivirus après détection de clé
                usbKeyScanJob = newJob(UsbKeyScanJob.class)
                        .withIdentity("UKSJ", "Scan")
                        .build();

                usbKeyScanTrigger = newTrigger()
                        .withIdentity("UKST", "Scan")
                        // .withSchedule(cronSchedule(Properties.dataCollectCronExpression))
                        .build();
                scheduler.scheduleJob(usbKeyScanJob, usbKeyScanTrigger);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyScanJobStop() {
        try {
            logger.info("usbKeyScanJobStop()");

            scheduler.unscheduleJob(usbKeyScanTrigger.getKey());
            scheduler.interrupt(usbKeyScanJob.getKey());
            scheduler.deleteJob(usbKeyScanJob.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyCheckSnatchedJobStart() {
        try {
            logger.info("usbKeyCheckSnatchedJobStart()");
            //SNATCHED
            if (!scheduler.checkExists(JobKey.jobKey("CheckSnatched.UKCSJ"))) {
                logger.info(" -> Creation");
                usbKeyCheckSnatchedJob = newJob(UsbKeyCheckSnatchedJob.class)
                        .withIdentity("UKCSJ", "CheckSnatched")
                        .build();

                delay = Integer.parseInt(docConfig.selectSingleNode("/config/scheduler/usbKeyCheckSnatchedJobDelayInSeconds").getText());
                usbKeyCheckSnatchedTrigger = newTrigger()
                        .withIdentity("UKCST", "CheckSnatched")
                        .withSchedule(simpleSchedule().withIntervalInSeconds(delay).repeatForever())
                        .startNow()
                        //.startAt(futureDate(1, SECOND))
                        .build();
                scheduler.scheduleJob(usbKeyCheckSnatchedJob, usbKeyCheckSnatchedTrigger);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyCheckSnatchedJobStop() {
        try {
            logger.info("usbKeyCheckSnatchedJobStop()");
            scheduler.unscheduleJob(usbKeyCheckSnatchedTrigger.getKey());
            scheduler.interrupt(usbKeyCheckSnatchedJob.getKey());
            scheduler.deleteJob(usbKeyCheckSnatchedJob.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void usbKeyCheckOutJobStart() {
        try {
            logger.info("usbKeyCheckOutJobStart()");
            //USB OUT
            if (!scheduler.checkExists(JobKey.jobKey("CheckOut.UKCOJ"))) {
                logger.info(" -> Creation");
                usbKeyCheckOutJob = newJob(UsbKeyCheckOutJob.class)
                        .withIdentity("UKCOJ", "CheckOut")
                        .build();

                delay = Integer.parseInt(docConfig.selectSingleNode("/config/scheduler/usbKeyCheckOutJobDelayInSeconds").getText());
                usbKeyCheckOutTrigger = newTrigger()
                        .withIdentity("UKCOT", "CheckOut")
                        .withSchedule(simpleSchedule().withIntervalInSeconds(delay).repeatForever())
                        .startNow()
                        //.startAt(futureDate(1, SECOND))
                        .build();
                scheduler.scheduleJob(usbKeyCheckOutJob, usbKeyCheckOutTrigger);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void usbKeyCheckOutJobStop() {
        try {
            logger.info("usbKeyCheckOutJobStop()");
            scheduler.unscheduleJob(usbKeyCheckOutTrigger.getKey());
            scheduler.interrupt(usbKeyCheckOutJob.getKey());
            scheduler.deleteJob(usbKeyCheckOutJob.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void antivirusGetLastUpdateDateJobStart() {
        try {
            logger.info("antivirusGetLastUpdateDateJobStart()");
            if (!scheduler.checkExists(JobKey.jobKey("Antivirus.GLUDJ"))) {
                logger.info(" -> Creation");
                antivirusGetLastUpdateDateJob = newJob(AntivirusGetLastUpdateDateJob.class)
                        .withIdentity("GLUDJ", "Antivirus")
                        .build();

                delay = Integer.parseInt(docConfig.selectSingleNode("/config/scheduler/antivirusGetLastUpdateDateInHours").getText());
                antivirusGetLastUpdateDateTrigger = newTrigger()
                        .withIdentity("GLUDT", "Antivirus")
                        .withSchedule(simpleSchedule().withIntervalInHours(delay).repeatForever())
                        .startNow()
                        //.startAt(futureDate(delay, HOUR))
                        .build();

                // https://www.geek-share.com/detail/2769504237.html
                scheduler.scheduleJob(antivirusGetLastUpdateDateJob, antivirusGetLastUpdateDateTrigger);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void antivirusGetLastUpdateDateJobStop() {
        try {
            logger.info("antivirusGetLastUpdateDateJobStop()");
            scheduler.unscheduleJob(antivirusGetLastUpdateDateTrigger.getKey());
            scheduler.interrupt(antivirusGetLastUpdateDateJob.getKey());
            scheduler.deleteJob(antivirusGetLastUpdateDateJob.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void printJobs() throws SchedulerException {
        // List<JobExecutionContext> currentlyExecuting = schedulerFactoryBean.getCurrentlyExecutingJobs();
        logger.info("");
        logger.info("");
        logger.info("JOB ################################################");
        List<JobExecutionContext> currentJobs = factory.getScheduler().getCurrentlyExecutingJobs();
        String jobName;
        String groupName;

        for (JobExecutionContext jobCtx : currentJobs) {
            jobName = jobCtx.getJobDetail().getKey().getName();
            groupName = jobCtx.getJobDetail().getKey().getGroup();
            logger.info("JOB " + jobName + groupName);
//        if (jobName.equalsIgnoreCase("job_I_am_looking_for_name") && groupName.equalsIgnoreCase("job_group_I_am_looking_for_name")) {
//            //found it!
//            logger.info("the job is already running - do nothing");
//            return;
//        }
        }
        logger.info("");
        logger.info("");
    }
}