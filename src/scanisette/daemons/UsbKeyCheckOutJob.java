/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scanisette.daemons;

import javafx.application.Platform;
import org.quartz.*;
import scanisette.Scanisette;
import scanisette.models.SystemDrive;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static scanisette.Scanisette.logger;
import static scanisette.Scanisette.mainApp;


// https://www.freeformatter.com/cron-expression-generator-quartz.html
@DisallowConcurrentExecution
public class UsbKeyCheckOutJob implements InterruptableJob {

    private final AtomicReference<Thread> runningThread = new AtomicReference<>();
    private long interruptMaxRetries = 3;

    public static final String MESSAGE = "msg";


    //private static Logger _log = LoggerFactory.getLogger(UsbKeyCheckInJob.class);


    public UsbKeyCheckOutJob() {
        System.out.println("UsbKeyCheckOutJob()");
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        this.runningThread.set(Thread.currentThread());


//        System.out.println("UsbKeyCheckOutJob : execute()");
        // This job simply prints out its job name and the
        // date and time that it is running
        JobKey jobKey = context.getJobDetail().getKey();
        String message = (String) context.getJobDetail().getJobDataMap().get(MESSAGE);

//        logger.info("SimpleJob says: " + jobKey + " executing at " + new Date());
//        logger.info("SimpleJob: msg: " + message);

        try {

           // System.out.println(mainApp.name); //!!! remonter dans le constructeur du job (je sasi pas comment car dépend du contexte)
//            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
//            System.out.println(now);

            File tmpDir = new File(mainApp.currentUsbDrive.path);
            boolean usbDriveExists = tmpDir.exists();

            if (!usbDriveExists) {
                logger.info("CLE USB ENLEVEE");
                mainApp.appScheduler.usbKeyCheckOutJobStop();
                Platform.runLater(() -> {
                    mainApp.loadScene("Home");
                });
                //
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
