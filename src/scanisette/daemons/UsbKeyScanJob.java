/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scanisette.daemons;

import javafx.application.Platform;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.quartz.*;
import scanisette.models.Antivirus;
import scanisette.models.ScanResult;
import scanisette.models.StreamGobbler;
import scanisette.tools.ResultTool;
import scanisette.tools.SevenZipTool;
import scanisette.tools.UiTool;
import scanisette.ui.controllers.ScanningController;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static scanisette.Scanisette.*;


// https://www.freeformatter.com/cron-expression-generator-quartz.html
@DisallowConcurrentExecution
public class UsbKeyScanJob implements InterruptableJob {

    private final AtomicReference<Thread> runningThread = new AtomicReference<>();
    private long interruptMaxRetries = 3;

    public static final String MESSAGE = "msg";


    // private static Logger _log = LoggerFactory.getLogger(ScanUsbKeyJob.class);

    public UsbKeyScanJob() {
        System.out.println("UsbKeyScanJob()");


    }

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        this.runningThread.set(Thread.currentThread());

        mainApp.jobCounter++;
        System.out.println("");
        System.out.println("------------------------------------------------------");
        System.out.println("UsbKeyScanJob : execute() : " + new Date());
        System.out.println(mainApp.jobCounter);
        System.out.println(Thread.currentThread().getId());
        System.out.println(Thread.currentThread().getName());
        System.out.println("------------------------------------------------------");
        System.out.println("");

        mainApp.usbKeySnatched = false;

//        System.out.println("execute()");
        // This job simply prints out its job name and the
        // date and time that it is running
        JobKey jobKey = context.getJobDetail().getKey();
        String message = (String) context.getJobDetail().getJobDataMap().get(MESSAGE);

//        logger.info("SimpleJob says: " + jobKey + " executing at " + new Date());
//        logger.info("SimpleJob: msg: " + message);
//            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
//            System.out.println(now);


        // Récupération des informations avec USBDeview.exe
        Node node = docConfig.selectSingleNode("/config/tools/tool[contains(name, 'USBDeview')]");

        String file = node.selectSingleNode("file").getText();
        File theFile = new File(file);
        String parent = theFile.getParent();
        File theDir = new File(parent);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }


        File dir = new File(node.selectSingleNode("folder").getText());
        String cmd = node.selectSingleNode("folder").getText() + node.selectSingleNode("executable").getText() + " " + node.selectSingleNode("parameters").getText();
        cmd = cmd.replace("[FILE]", node.selectSingleNode("file").getText());
        System.out.println("CMD : " + cmd);

        try {
            mainApp.appScheduler.process = Runtime.getRuntime().exec(cmd, null, dir);
            int exitCode = mainApp.appScheduler.process.waitFor();
            System.out.println("Génération par USBDeview Sortie : " + exitCode);

            if (exitCode == 0) {
                // on parse le fichier xml

                SAXReader xmlReader = new SAXReader();
                File fileConfig = new File(node.selectSingleNode("file").getText());
                Document docUSBDeview = xmlReader.read(fileConfig);
                String searchUsbKey = "/usb_devices_list/item[contains(drive_letter, '" + mainApp.currentUsbDrive.path.replace("\\", "") + "')]";
                System.out.println(searchUsbKey);

                Node nodeUsbItem = docUSBDeview.selectSingleNode(searchUsbKey);

                if (nodeUsbItem != null) {

                    mainApp.currentUsbDrive.deviceName = nodeUsbItem.selectSingleNode("device_name").getText();
                    mainApp.currentUsbDrive.description = nodeUsbItem.selectSingleNode("description").getText();
                    mainApp.currentUsbDrive.serialNumber = nodeUsbItem.selectSingleNode("serial_number").getText();
                    mainApp.currentUsbDrive.registryTime1 = nodeUsbItem.selectSingleNode("registry_time_1").getText();
                    mainApp.currentUsbDrive.registryTime2 = nodeUsbItem.selectSingleNode("registry_time_2").getText();
                    mainApp.currentUsbDrive.vendorId = nodeUsbItem.selectSingleNode("vendorid").getText();
                    mainApp.currentUsbDrive.productId = nodeUsbItem.selectSingleNode("productid").getText();
                    mainApp.currentUsbDrive.firmwareRevision = nodeUsbItem.selectSingleNode("firmware_revision").getText();
                }
            }

        } catch (IOException | InterruptedException | DocumentException ex) {
            ex.printStackTrace();
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String datetimenow = dtf.format(now);

        mainApp.scanResult = new ScanResult();
        mainApp.scanResult.date = datetimenow;
        mainApp.scanResult.usbkeyDeviceName = mainApp.currentUsbDrive.deviceName;
        mainApp.scanResult.usbkeyDescription = mainApp.currentUsbDrive.description;
        mainApp.scanResult.usbkeySerialNumber = mainApp.currentUsbDrive.serialNumber;
        mainApp.scanResult.usbkeyRegistryTime1 = mainApp.currentUsbDrive.registryTime1;
        mainApp.scanResult.usbkeyRegistryTime2 = mainApp.currentUsbDrive.registryTime2;
        mainApp.scanResult.usbkeyVendorId = mainApp.currentUsbDrive.vendorId;
        mainApp.scanResult.usbkeyProductId = mainApp.currentUsbDrive.productId;
        mainApp.scanResult.usbkeyFirmwareRevision = mainApp.currentUsbDrive.firmwareRevision;


        boolean hasOneEncryptedFile = false;
        boolean suspectFound = false;
        boolean virusFound = false;

        System.out.println("Encrypted start");

        try {
            List<File> archiveFiles = SevenZipTool.getCompressedFiles(mainApp.currentUsbDrive.path);
            System.out.println(Arrays.toString(archiveFiles.toArray()));

            Platform.runLater(() -> {
                ((ScanningController) mainApp.currentSceneController).setProgressBar(0.0);
                ((ScanningController) mainApp.currentSceneController).setLabZoneInfo1("Scan des archives : " + archiveFiles.size() + " fichiers");
                ((ScanningController) mainApp.currentSceneController).setLabZoneInfo2("Archive en cours : 0 / " + archiveFiles.size());
            });

            RandomAccessFile randomAccessFile = null;
            IInArchive inArchive = null;
            int currentFileIndex = 0;
            for (File archiveFile : archiveFiles) {
                currentFileIndex++;
                System.out.println("file: " + archiveFile.getCanonicalPath());
                int finalCurrentFileIndex = currentFileIndex;
                Platform.runLater(() -> {
                    double progress = 1.0 * finalCurrentFileIndex / archiveFiles.size();
                    ((ScanningController) mainApp.currentSceneController).setProgressBar(progress);
                    ((ScanningController) mainApp.currentSceneController).setLabZoneInfo2("Archive en cours : " + finalCurrentFileIndex + " / " + archiveFiles.size());
                });
                randomAccessFile = new RandomAccessFile(archiveFile.getCanonicalPath(), "r");
                inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
                if (SevenZipTool.isEncrypted(inArchive)) {
                    hasOneEncryptedFile = true;
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Encrypted end");


        if (hasOneEncryptedFile) {
            System.out.println("Scanning aborted because encrypted file");
        } else {
            System.out.println("Scanning start");

            mainApp.positiveFileCount = 0;
            int antivirusIndex = 0;
            suspectFound = false;
            virusFound = false;

            mainApp.scanResult.virusInfos = new ArrayList<>();


            for (Antivirus currentAntivirus : mainApp.availableAntivirus) {
                antivirusIndex++;

                System.out.println(currentAntivirus.name);
                mainApp.currentAntivirus = currentAntivirus;

                int finalAntivirusIndex = antivirusIndex;
                Platform.runLater(() -> {
                    ((ScanningController) mainApp.currentSceneController).setProgressBar(0.0);
                    String antivirusInfo = "Antivirus " + finalAntivirusIndex + "/" + mainApp.availableAntivirus.size() + " : " + currentAntivirus.name;
                    ((ScanningController) mainApp.currentSceneController).setLabZoneInfo1(antivirusInfo);
                    ((ScanningController) mainApp.currentSceneController).setLabZoneInfo2("Version : " + UiTool.formatDateTime(currentAntivirus.lastUpdateDate));
                });

                try {
                    dir = new File(currentAntivirus.installationFolder);
                    cmd = currentAntivirus.installationFolder + currentAntivirus.executableFile + " " + currentAntivirus.parameters;
                    cmd = cmd.replace("[USB]", mainApp.currentUsbDrive.path);
                    System.out.println(cmd);
                    mainApp.appScheduler.process = Runtime.getRuntime().exec(cmd, null, dir);

                    mainApp.appScheduler.streamGobbler = new StreamGobbler(mainApp.appScheduler.process, currentAntivirus);
                    mainApp.appScheduler.executor = Executors.newSingleThreadExecutor();
                    Future<Integer> result = (Future<Integer>) mainApp.appScheduler.executor.submit(mainApp.appScheduler.streamGobbler);

                    int exitCode = mainApp.appScheduler.process.waitFor();
                    System.out.println(currentAntivirus.name + " Sortie : " + exitCode);
                    if (mainApp.appScheduler.process != null)
                        mainApp.appScheduler.process.destroy();

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }


                if (mainApp.positiveFileCountForVirusFound > 0 && mainApp.positiveFileCount == mainApp.positiveFileCountForVirusFound) {
                    virusFound = true;
                    suspectFound = false;
                    break; // boucle antivirus
                } else if (mainApp.positiveFileCountForSuspectFound > 0 && mainApp.positiveFileCount == mainApp.positiveFileCountForSuspectFound) {
                    virusFound = false;
                    suspectFound = true;
                } else {
                    virusFound = false;
                    suspectFound = false;
                }

            }
            System.out.println("Scanning end");
        }

        if (!mainApp.usbKeySnatched) {
            if (hasOneEncryptedFile) {
                mainApp.scanResult.operationVerb = "ScanForEncrypted";
                mainApp.scanResult.operationOutput = "Found";
                ResultTool.save(mainApp.docConfig.selectSingleNode("/config/result/file").getText(), mainApp.scanResult);
                Platform.runLater(() -> {
                    mainApp.loadScene("ResultSuspect");
                });
                return;
            }


            if (virusFound) {
                logger.info("VIRUS !!!");
                mainApp.scanResult.operationVerb = "ScanForVirus";
                mainApp.scanResult.operationOutput = "Virus";
                cleanSubProcesses();
                ResultTool.save(mainApp.docConfig.selectSingleNode("/config/result/file").getText(), mainApp.scanResult);
                cleanSubProcesses();
                mainApp.appScheduler.usbKeyCheckSnatchedJobStop();
                mainApp.appScheduler.usbKeyScanJobStop();
                Platform.runLater(() -> {
                    mainApp.loadScene("ResultVirus");
                });


                return;

            }
            if (!virusFound && suspectFound) {
                logger.info("SUSPECT !!!");
                mainApp.scanResult.operationVerb = "ScanForVirus";
                mainApp.scanResult.operationOutput = "Suspect";
                cleanSubProcesses();
                ResultTool.save(mainApp.docConfig.selectSingleNode("/config/result/file").getText(), mainApp.scanResult);

                mainApp.appScheduler.usbKeyScanJobStop();
                mainApp.appScheduler.usbKeyCheckSnatchedJobStop();

                Platform.runLater(() -> {
                    mainApp.loadScene("ResultSuspect");
                });

                return;
            }
            if (!virusFound && !suspectFound) {
                logger.info("CLEAN !!!");
                mainApp.scanResult.operationVerb = "ScanForVirus";
                mainApp.scanResult.operationOutput = "Clean";
                cleanSubProcesses();
                ResultTool.save(mainApp.docConfig.selectSingleNode("/config/result/file").getText(), mainApp.scanResult);

                Platform.runLater(() -> {
                    mainApp.loadScene("ResultClean");
                });
                mainApp.appScheduler.usbKeyScanJobStop();
                mainApp.appScheduler.usbKeyCheckSnatchedJobStop();
                return;
            }
        }

    }


    @Override
    public void interrupt() throws UnableToInterruptJobException {
        // destruction du sous process de scan
//        if (process != null)
//            process.destroy();
//
//        OutputLine outputLineSnatched = mainApp.outputScannedFiles.get("SNATCHED");
//        if (outputLineSnatched.code.equals("SNATCHED")) {
//            logger.info("SNATCHED !!!");
//            Platform.runLater(() -> {
//                mainApp.loadScene("ResultSnatched");
//            });
//        }

        Thread thread = this.runningThread.getAndSet(null);
        if (thread == null) {
            logger.info("Unable to retrieve the thread of the current job execution");
        } else {
            logger.info("Interrupting job from thread" + thread.getId());
            cleanSubProcesses();

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

    private void cleanSubProcesses() {
        if (mainApp.appScheduler.process.isAlive())
            mainApp.appScheduler.process.destroy();
        mainApp.appScheduler.executor.shutdownNow();
        mainApp.appScheduler.streamGobbler = null;
    }

}
