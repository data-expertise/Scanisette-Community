/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scanisette.daemons;

import java.io.File;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import org.quartz.*;
import scanisette.models.SystemDrive;

import javax.swing.filechooser.FileSystemView;

import static scanisette.Scanisette.logger;
import static scanisette.Scanisette.mainApp;


// https://www.freeformatter.com/cron-expression-generator-quartz.html
@DisallowConcurrentExecution
public class UsbKeyCheckInJob implements InterruptableJob {

    private final AtomicReference<Thread> runningThread = new AtomicReference<>();
    private long interruptMaxRetries = 3;

    public static final String MESSAGE = "msg";


    //private static Logger _log = LoggerFactory.getLogger(UsbKeyCheckInJob.class);


    public UsbKeyCheckInJob() {
        System.out.println("UsbKeyCheckInJob()");
    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        this.runningThread.set(Thread.currentThread());

//        mainApp.jobCounter++;
//        System.out.println("");
//        System.out.println("------------------------------------------------------");
//        System.out.println("UsbKeyCheckInJob : execute() : "+ new Date());
//        System.out.println(mainApp.jobCounter);
//        System.out.println(Thread.currentThread().getId());
//        System.out.println(Thread.currentThread().getName());
//        System.out.println("------------------------------------------------------");
//        System.out.println("");



        JobKey jobKey = context.getJobDetail().getKey();
        String message = (String) context.getJobDetail().getJobDataMap().get(MESSAGE);
        logger.info("SimpleJob says: " + jobKey + " executing at " + new Date());

        try {

            mainApp.pluggedDrives.clear();
            FileSystemView fsv = FileSystemView.getFileSystemView();
            File[] drives = File.listRoots();
            SystemDrive systemDrive;
            if (drives != null && drives.length > 0) {
                for (File aDrive : drives) {
                    //System.out.println(aDrive);

                    String driveType = fsv.getSystemTypeDescription(aDrive);
                    //System.out.println(driveType);

                    long freeSpace = aDrive.getFreeSpace();
                    long totalSpace = aDrive.getTotalSpace();

                    systemDrive = new SystemDrive(aDrive.getPath(), driveType, freeSpace, totalSpace);

                    if (!systemDrive.isInList(mainApp.initialDrives)) {
                        mainApp.pluggedDrives.add(new SystemDrive(aDrive.getPath(), driveType, freeSpace, totalSpace)); // !!! attention modification pour linux
                    }

                }
            }

            if (mainApp.pluggedDrives.size() > 0) {
                logger.info("DRIVE/CLE USB TROUVE");
                logger.info(mainApp.pluggedDrives.get(0).path);
                mainApp.currentUsbDrive = (SystemDrive) mainApp.pluggedDrives.get(0);
                Platform.runLater(() -> mainApp.loadScene("Scanning"));
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
