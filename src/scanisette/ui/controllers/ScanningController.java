package scanisette.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import scanisette.tools.UiTool;

import java.io.File;

import static scanisette.Scanisette.mainApp;

public class ScanningController extends _MetaController {
    @FXML
    ImageView imvLogo;

    @FXML
    private Label labZoneInfo1;

    @FXML
    private Label labZoneInfo2;

    @FXML
    private ProgressBar progressBar;


    @FXML
    void initialize() {
        progressBar.setProgress(0);

        UiTool.setLogo(imvLogo, mainApp.imageProportionalScale);
    }

    public void load() {
        mainApp.appScheduler.swapScreensJobStop();
        mainApp.appScheduler.usbKeyCheckInJobStop();
        mainApp.appScheduler.usbKeyScanJobStart();
        mainApp.appScheduler.usbKeyCheckSnatchedJobStart();

    }

    public void setProgressBar(double value) {
        progressBar.setProgress(value);
    }

    public void setLabZoneInfo1(String text) {
        labZoneInfo1.setText(text);
    }

    public void setLabZoneInfo2(String text) {
        labZoneInfo2.setText(text);
    }


}
