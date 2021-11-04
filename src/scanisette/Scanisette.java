package scanisette;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.*;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import scanisette.daemons.AppScheduler;
import scanisette.models.*;
import scanisette.tools.KeyHookTool;
import scanisette.ui.controllers.InitController;
import scanisette.ui.controllers._MetaController;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public class Scanisette extends Application {

    public static Scanisette mainApp;
    public static Logger logger;
    public static Document docConfig;

    public String executionPath;

    public static int jobCounter = 0;

    public boolean isWindowsSystem;
    public AppScheduler appScheduler = new AppScheduler();

    Screen primaryScreen;
    Screen secondaryScreen;

    public boolean haveSevenZipTool;

    Stage appStage;
    public String currentSceneName = "";
    public Scene currentScene;
    public Scale screenScale;
    public Scale imageProportionalScale;
    public _MetaController currentSceneController;

    public List<SystemDrive> initialDrives = new ArrayList<>();
    public List<SystemDrive> pluggedDrives = new ArrayList<>();
    public SystemDrive currentUsbDrive;

    public List<Antivirus> availableAntivirus;
    public Antivirus currentAntivirus;
    public int positiveFileCount = 0;
    public int positiveFileCountForVirusFound;
    public int positiveFileCountForSuspectFound;
    public ScanResult scanResult;

    final BooleanProperty key1PressedForExit = new SimpleBooleanProperty(false);
    final BooleanProperty key2PressedForExit = new SimpleBooleanProperty(false);
    final BooleanProperty key3PressedForExit = new SimpleBooleanProperty(false);
    final BooleanBinding keysPressedForExit = key1PressedForExit.and(key2PressedForExit).and(key3PressedForExit);

    public boolean usbKeySnatched = false;

    Connection databaseConnection;

    @Override
    public void start(Stage primaryStage) {

        try {
            mainApp = this;
            isWindowsSystem = System.getProperty("os.name").toLowerCase().startsWith("windows");
            executionPath = System.getProperty("user.dir");
            System.out.println(executionPath);

            loadConfigXml();

            startLogger();

            appStage = primaryStage;
            setScreen();

            // Gestion de la sortie par code secret
            KeyHookTool.blockWindowsKey();
            keysPressedForExit.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                    System.out.println("3 keys to exit");
                    currentScene.setCursor(Cursor.DEFAULT);
                    TextInputDialog td = new TextInputDialog();
                    td.initOwner(appStage);
                    td.setHeaderText("Entrez le code pour quitter le programme");
                    td.showAndWait();
                    if (td.getEditor().getText().equals("1122")) {
                        cleanAppBeforeExit();
                    } else {
                        currentScene.setCursor(Cursor.NONE);
                        td.close();
                    }

                }
            });

            loadScene("Init");

            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n###### Démarrage du système");
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n");
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Initialisation du logger : OK");

            try {
                Font.loadFont(Scanisette.class.getResource("ui/css/CoveredByYourGrace.ttf").toExternalForm(), 10);
                Font.loadFont(Scanisette.class.getResource("ui/css/Catamaran-Bold.ttf").toExternalForm(), 10);
                Font.loadFont(Scanisette.class.getResource("ui/css/Catamaran-ExtraBold.ttf").toExternalForm(), 10);
                Font.loadFont(Scanisette.class.getResource("ui/css/Catamaran-Medium.ttf").toExternalForm(), 10);
            } catch (java.lang.NullPointerException n) {
                System.out.println("police non trouvée");
            }
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Chargement des polices : OK");

            //loadConfigXml();
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Lecture du fichier de config : OK");

            ((InitController) currentSceneController).logAreaAppendTextLn("Disques listés au démarrage : ");
            getInitialDrives();


            appScheduler.init();
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Initialisation de l'ordonanceur des jobs : OK");


            instanciateAntivirus();
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Instanciation des antivirus : OK");

            mainApp.appScheduler.antivirusGetLastUpdateDateJobStart();
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Lancement de la lecture des dates de signature : OK");

            try {
                SevenZip.initSevenZipFromPlatformJAR();
                haveSevenZipTool = true;
                System.out.println("7-Zip-JBinding library was initialized");
            } catch (SevenZipNativeInitializationException ex) {
                haveSevenZipTool = false;
                ex.printStackTrace();
            }
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n- Binding 7Zip : OK");

            positiveFileCountForVirusFound = Integer.parseInt(docConfig.selectSingleNode("/config/result/positiveFileCountForVirusFound").getText());
            positiveFileCountForSuspectFound = Integer.parseInt(docConfig.selectSingleNode("/config/result/positiveFileCountForSuspectFound").getText());

            org.dom4j.Node node = docConfig.selectSingleNode("/config/scheduler/initScreenJobDelayInSeconds");
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n");
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n###### Lancement du scanner dans " + node.getText() + " secondes");
            long delay = Long.parseLong(node.getText(), 10) * 1000L;
            loadSceneAfterDelay("Home", delay);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("EXCEPTION : CA ME GAVE !!!");
            System.out.println(ex.getMessage());
        }
    }

    public void loadSceneAfterDelay(String scene, long delay) {
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on: " + new Date() + "n" +
                        "Thread's name: " + Thread.currentThread().getName());
                Platform.runLater(() -> {
                    loadScene(scene);
                });
            }
        };
        Timer timer = new Timer("Timer");
        timer.schedule(task, delay);
    }

    private void loadConfigXml() {
        try {
            SAXReader xmlReader = new SAXReader();
            xmlReader.setEncoding("UTF-8");
            File fileConfig = new File("resources/", "config.xml");
            docConfig = xmlReader.read(fileConfig);
//            Element root = docConfig.getRootElement();
//            System.out.println("---> " + root.getName());
//
//            List<Element> children = root.elements();
//
//            for (Element elem : children) {
//                System.out.println("---> " + elem.getName());
//            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception : loadConfigXml() !!!!");
        }

    }

    private void instanciateAntivirus() {
        availableAntivirus = new ArrayList<Antivirus>();
        String name;
        String folder;
        String executable;
        String parameters;
        String progressPattern;
        String filePattern;
        String commandForDate;
        String commandForDatePattern;
        String fileForDate;
        Path fileForDatePath;
        FileTime lastUpdateDate;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        List<org.dom4j.Node> list = docConfig.selectNodes("/config/scanners/antivirus[contains(enable, 'yes')]");
        try {
            int index = 0;
            for (org.dom4j.Node node : list) {
                index++;
                name = node.selectSingleNode("name").getText();
                folder = node.selectSingleNode("folder").getText();
                executable = node.selectSingleNode("executable").getText();
                parameters = node.selectSingleNode("parameters").getText();
                progressPattern = node.selectSingleNode("progressPattern").getText();
                filePattern = node.selectSingleNode("filePattern").getText();
                commandForDate = node.selectSingleNode("commandForDate").getText();
                commandForDatePattern = node.selectSingleNode("commandForDatePattern").getText();
                fileForDate = node.selectSingleNode("fileForDate").getText();
                fileForDatePath = Paths.get(fileForDate);
                lastUpdateDate = null;

                Path antivirusPath = Paths.get(folder + executable);
                if (Files.exists(antivirusPath)) {
                    availableAntivirus.add(new Antivirus(name, folder, executable, parameters, progressPattern, filePattern, commandForDate, commandForDatePattern, fileForDate, lastUpdateDate));
                }
            }
            ((InitController) currentSceneController).logAreaAppendTextLn("\r\n###### Antivirus configurés");
            for (Antivirus currentAntivirus : availableAntivirus) {

                // affichage des antivirus trouvés
                System.out.println(currentAntivirus.name);
                ((InitController) currentSceneController).logAreaAppendTextLn(currentAntivirus.name);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void startLogger() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s %2$s %5$s%6$s%n"); //%1$tF %1$tT %4$s %2$s %5$s%6$s%n
        logger = Logger.getLogger(this.getClass().getName());
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false); // supprime les messages en double car possède déjà un handler console

        // Log to console
        ConsoleHandler ConsoleHandler = new ConsoleHandler();
        ConsoleHandler.setFormatter(new SimpleFormatter());
        ConsoleHandler.setLevel(Level.ALL);
        logger.addHandler(ConsoleHandler);

        // Log to file
        try {
            org.dom4j.Node node = mainApp.docConfig.selectSingleNode("/config/logger/file");
            String file = node.getText();

            File theFile = new File(file);
            String parent = theFile.getParent();
            File theDir = new File(parent);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }

            FileHandler fileHandler = new FileHandler(file, 200, 1);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.severe("Erreur de création du fichier de log");
        }

        logger.info("setLogger : END");
    }

    private void setScreen() {
        primaryScreen = Screen.getPrimary();
        Screen.getScreens().stream()
                .filter(s -> !s.equals(primaryScreen))
                .findFirst().ifPresent(s -> secondaryScreen = s);


        Rectangle2D primaryBounds = primaryScreen.getBounds();
        double x1 = primaryBounds.getMinX();
        double y1 = primaryBounds.getMinY();
        double w1 = primaryBounds.getWidth();
        double h1 = primaryBounds.getHeight();
        double dpi1 = primaryScreen.getDpi();
        double scaleX1 = primaryScreen.getOutputScaleX();
        double scaleY1 = primaryScreen.getOutputScaleY();

        logger.info("Primary Screen / x:" + x1 + " /y:" + y1 + " /h:" + h1 + " /w:" + w1 + " /dpi:" + dpi1 + " /scaleX:" + scaleX1 + " /scaleY:" + scaleY1);

        if (secondaryScreen != null) {
            Rectangle2D secondaryBounds = secondaryScreen.getBounds();
            double x2 = secondaryBounds.getMinX();
            double y2 = secondaryBounds.getMinY();
            double w2 = secondaryBounds.getWidth();
            double h2 = secondaryBounds.getHeight();
            double dpi2 = secondaryScreen.getDpi();
            double scaleX2 = secondaryScreen.getOutputScaleX();
            double scaleY2 = secondaryScreen.getOutputScaleY();
            logger.info("Secondary Screen / x:" + x2 + " /y:" + y2 + " /h:" + h2 + " /w:" + w2 + " /dpi:" + dpi2 + " /scaleX:" + scaleX2 + " /scaleY:" + scaleY2);
        }


        appStage.setX(x1);
        appStage.setY(y1);
        appStage.setWidth(w1);
        appStage.setHeight(h1);

//        A revoir pour un ajustement automatique des résolutions et des scales
//        double scaleX = w1 / 1920.0;
//        double scaleY = h1 / 1080.0;
//        logger.info("scaleX:" + scaleX + " /scaleY:" + scaleY);
//        appStage.setRenderScaleX(scaleX);
//        appStage.setRenderScaleY(scaleY);

        screenScale = new Scale(w1 / 1920.0, h1 / 1080.0, 0, 0);
        imageProportionalScale = new Scale(w1 / 1920.0, w1 / 1920.0, 0, 0);
        logger.info("screenScale:" + screenScale);

        appStage.initStyle(StageStyle.UNDECORATED);
        appStage.setResizable(false);
        appStage.setAlwaysOnTop(true);
        appStage.toFront();


        appStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        appStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override

            public void handle(WindowEvent event) {
                //Handle
                event.consume();
            }
        });
    }

    public void loadScene(String scene) {
        currentSceneName = scene;
        String fxml = "/scanisette/ui/fxml/" + scene + "Scene.fxml";
        logger.info("loadScene : " + fxml);
        try {
            javafx.scene.Node node = replaceScene(fxml);
            if (node != null) {
                // logger.info("setMainApp sur controller");
                currentSceneController = (_MetaController) node;
                //currentSceneController.setMainApp(this);
                currentSceneController.getStylesheets().add(getClass().getResource("ui/css/styles.css").toExternalForm());
                currentSceneController.load();

            } else {
                currentSceneName = "";
                logger.severe("Le chargement du fxml a retourné un noeud NULL pour la scène : " + scene);
            }

        } catch (Exception ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            currentSceneName = "";
            logger.severe("Erreur critique loadScene : " + scene + "   exception : " + ex.toString());
            ex.printStackTrace();
        }
        try {
            appScheduler.printJobs();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Node replaceScene(String fxml) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        InputStream in = Scanisette.class
                .getResourceAsStream(fxml);
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(Scanisette.class
                .getResource(fxml));
        AnchorPane page = null;
        try {
            page = (AnchorPane) loader.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            in.close();
        }

        currentScene = new Scene(page);

        currentScene.getStylesheets().add(getClass().getResource("ui/css/styles.css").toExternalForm());
        currentScene.setCursor(Cursor.NONE);
        currentScene.getRoot().getTransforms().setAll(screenScale);
        // https://openjfx.io/javadoc/12/javafx.graphics/javafx/scene/input/KeyCombination.html
        //final KeyCombination keyCombinationForExit = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.SHORTCUT_DOWN, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

        final KeyCombination keyCombinationForExit1 = new KeyCodeCombination(KeyCode.DIVIDE, KeyCombination.SHORTCUT_ANY);
        final KeyCombination keyCombinationForExit2 = new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.SHORTCUT_ANY);
        final KeyCombination keyCombinationForExit3 = new KeyCodeCombination(KeyCode.NUMPAD0, KeyCombination.SHORTCUT_ANY);

        currentScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(final KeyEvent keyEvent) {

                if (keyCombinationForExit1.match(keyEvent)) {
                    key1PressedForExit.set(true);
                }
                if (keyCombinationForExit2.match(keyEvent)) {
                    key2PressedForExit.set(true);
                }
                if (keyCombinationForExit3.match(keyEvent)) {
                    key3PressedForExit.set(true);
                }
                //keyEvent.consume();
            }
        });

        currentScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyCombinationForExit1.match(keyEvent)) {
                    key1PressedForExit.set(false);
                }
                if (keyCombinationForExit2.match(keyEvent)) {
                    key2PressedForExit.set(false);
                }
                if (keyCombinationForExit3.match(keyEvent)) {
                    key3PressedForExit.set(false);
                }
            }
        });

        //popup.registerScene(scene);   // CLAVIER SURCOUCHE
        //popup.addFocusListener(scene);
        //popup.addDoubleClickEventFilter(stage);


        appStage.setScene(currentScene);
        appStage.show();
        appStage.requestFocus();

        //stage.setFullScreen(true);
        return (Node) loader.getController();
    }

    private void getInitialDrives() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] drives = File.listRoots();
        if (drives != null && drives.length > 0) {
            for (File aDrive : drives) {
                System.out.println(aDrive);

                String driveType = fsv.getSystemTypeDescription(aDrive);
                System.out.println(driveType);
                System.out.println();

                System.out.println("path : " + aDrive.getPath());
                System.out.println();

                long freeSpace = aDrive.getFreeSpace();
                long totalSpace = aDrive.getTotalSpace();
                // Les disques dur usb sont vus comme un disque local et pas un lecteur usb
                // donc changement de stratégie
//                if (driveType.equals("Disque local") || driveType.equals("Local Disk")) {
//                    initialDrives.add(new SystemDrive(aDrive, driveType, freeSpace, totalSpace)); // !!! attention modification pour linux
//                }
//                if (driveType.equals("Lecteur USB") || driveType.equals("USB Drive")) {
//                    usbDrives.add(new SystemDrive(aDrive, driveType, freeSpace, totalSpace)); // !!! attention modification pour linux
//                }

                initialDrives.add(new SystemDrive(aDrive.getPath(), driveType, freeSpace, totalSpace));
            }
        }
        ((InitController) currentSceneController).logAreaAppendText("Lecteurs initiaux : ");
        for (SystemDrive systemDrive : initialDrives) {
            ((InitController) currentSceneController).logAreaAppendText(systemDrive.path);
        }
        ((InitController) currentSceneController).logAreaAppendTextLn("");
    }


    public void cleanAppBeforeExit() {
        cleanSubProcesses();
        KeyHookTool.unblockWindowsKey();
        stop();
    }

    public void cleanSubProcesses() {

    }

    @Override
    public void stop() {
        System.exit(0);
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
