package scanisette.ui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import scanisette.tools.UiTool;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static scanisette.Scanisette.mainApp;

public class ResultSnatchedController extends _MetaController {

    @FXML
    ImageView imvLogo;


    @FXML
    void initialize() {
        UiTool.setLogo(imvLogo,mainApp.imageProportionalScale);
    }

    public void load() {
        mainApp.appScheduler.usbKeyCheckSnatchedJobStop();
        org.dom4j.Node node = mainApp.docConfig.selectSingleNode("/config/scheduler/resultSnatchedScreenInSeconds");
        long delay = Long.parseLong(node.getText(), 10) * 1000L;
        mainApp.launchHome(delay);

        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on: " + new Date() + "n" +
                        "Thread's name: " + Thread.currentThread().getName());
                Platform.runLater(() -> {
                    mainApp.loadScene("Home");
                });
            }
        };
        Timer timer = new Timer("Timer");



        timer.schedule(task, 10000);
    }
}
