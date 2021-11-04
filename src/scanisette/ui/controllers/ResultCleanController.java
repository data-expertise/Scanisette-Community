package scanisette.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import scanisette.tools.UiTool;

import java.io.File;

import static scanisette.Scanisette.mainApp;

public class ResultCleanController extends _MetaController {

    @FXML
    ImageView imvLogo;


    @FXML
    void initialize() {
        UiTool.setLogo(imvLogo,mainApp.imageProportionalScale);
    }

    public void load() {
        mainApp.appScheduler.usbKeyCheckSnatchedJobStop();
        mainApp.appScheduler.usbKeyCheckOutJobStart();
    }
}
