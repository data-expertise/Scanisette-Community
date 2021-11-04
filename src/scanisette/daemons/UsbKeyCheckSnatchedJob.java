/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scanisette.daemons;

import javafx.application.Platform;
import org.quartz.*;
import scanisette.tools.ResultTool;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static scanisette.Scanisette.logger;
import static scanisette.Scanisette.mainApp;


// https://www.freeformatter.com/cron-expression-generator-quartz.html
@DisallowConcurrentExecution
public class UsbKeyCheckSnatchedJob implements InterruptableJob {



    private final AtomicReference<Thread> runningThread = new AtomicReference<>();
    private long interruptMaxRetries = 3;

    public static final String MESSAGE = "msg";

    //private static Logger _log = LoggerFactory.getLogger(UsbKeyCheckInJob.class);


    public UsbKeyCheckSnatchedJob() {

        System.out.println("UsbKeyCheckSnatchedJob()");
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        this.runningThread.set(Thread.currentThread());

        JobKey jobKey = context.getJobDetail().getKey();
        String message = (String) context.getJobDetail().getJobDataMap().get(MESSAGE);
        logger.info("SimpleJob says: " + jobKey + " executing at " + new Date());


        try {
            System.out.println("on cherche la clé ici : "+mainApp.currentUsbDrive.path);
            File tmpDir = new File(mainApp.currentUsbDrive.path);
            boolean usbDriveExists = tmpDir.exists();

            if (!usbDriveExists) {
                logger.info("CLE USB ARRACHEE BY JOB");
                mainApp.usbKeySnatched=true;

                if (mainApp.appScheduler.process.isAlive())
                    mainApp.appScheduler.process.destroy();
                if (mainApp.appScheduler.executor!=null) {
                    mainApp.appScheduler.executor.shutdownNow();
                    mainApp.appScheduler.streamGobbler = null;
                }

                mainApp.appScheduler.usbKeyCheckSnatchedJobStop();
                mainApp.appScheduler.usbKeyScanJobStop();

                mainApp.scanResult.operationVerb = "Check";
                mainApp.scanResult.operationOutput = "Snatched";
                ResultTool.save(mainApp.docConfig.selectSingleNode("/config/result/file").getText(), mainApp.scanResult);
                Platform.runLater(() -> {
                    mainApp.loadScene("ResultSnatched");
                });

            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
