/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scanisette.daemons;

import javafx.application.Platform;
import org.quartz.*;
import scanisette.models.Antivirus;
import scanisette.ui.controllers.InitController;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static scanisette.Scanisette.logger;
import static scanisette.Scanisette.mainApp;


// https://www.freeformatter.com/cron-expression-generator-quartz.html
@DisallowConcurrentExecution
public class AntivirusGetLastUpdateDateJob implements InterruptableJob {

    private final AtomicReference<Thread> runningThread = new AtomicReference<>();
    private long interruptMaxRetries = 3;

    public static final String MESSAGE = "msg";


    //private static Logger _log = LoggerFactory.getLogger(UsbKeyCheckInJob.class);


    public AntivirusGetLastUpdateDateJob() {
        System.out.println("AntivirusGetLastUpdateDateJob()");
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        this.runningThread.set(Thread.currentThread());


//        System.out.println("AntivirusGetLastUpdateDateJob : execute()");

        JobKey jobKey = context.getJobDetail().getKey();
        String message = (String) context.getJobDetail().getJobDataMap().get(MESSAGE);

//        logger.info("SimpleJob says: " + jobKey + " executing at " + new Date());
//        logger.info("SimpleJob: msg: " + message);

        Path fileForDatePath;
        FileTime lastUpdateDate;

        try {
            for (Antivirus currentAntivirus : mainApp.availableAntivirus) {
                System.out.println(currentAntivirus.name);

                Class<?> customAntivirusClass = Class.forName("scanisette.models."+currentAntivirus.name+"Antivirus");
                Method getLastUpdateDate = customAntivirusClass.getMethod("getLastUpdateDate", String.class, String.class, String.class, String.class);

                lastUpdateDate = (FileTime)getLastUpdateDate.invoke(null, currentAntivirus.installationFolder, currentAntivirus.commandForDate, currentAntivirus.commandForDatePattern, currentAntivirus.fileForDate);

                currentAntivirus.lastUpdateDate = lastUpdateDate;
            }



        } catch (Exception ex) {

        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        Thread thread = this.runningThread.getAndSet(null);
        if (thread == null) {
            logger.info("Unable to retrieve the thread of the current job execution");
        } else {
            logger.info("Interrupting job from thread" + thread.getId());

            if (interruptMaxRetries < 1) {
                interruptMaxRetries = 1;
            }
            for (int i = 0; i < interruptMaxRetries && thread.isAlive(); i++) {
                thread.interrupt();
            }
            // if the thread is still alive, it should be available in the next stop
            if (thread.isAlive()) {
                this.runningThread.set(thread);
            }
        }
    }

}
