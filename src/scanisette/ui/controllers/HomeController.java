package scanisette.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import scanisette.models.Antivirus;
import scanisette.tools.UiTool;

import java.io.File;

import static scanisette.Scanisette.docConfig;
import static scanisette.Scanisette.mainApp;
import static scanisette.tools.UiTool.formatDateTime;

public class HomeController  extends _MetaController {

    @FXML
    ImageView imvLogo;


    @FXML
    Label labZone1;

    @FXML
    Label labZone2;

    @FXML
    Label labNumber1;

    @FXML
    Label labText1;

    @FXML
    Label labNumber2;

    @FXML
    Label labText2;

    @FXML
    Label labNumber3;

    @FXML
    Label labText3;

    @FXML
    Label labText4;

    @FXML
    void initialize() {
        UiTool.setLogo(imvLogo,mainApp.imageProportionalScale);

        labZone1.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/zone1").getText());
        labZone2.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/zone2").getText());
        labNumber1.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/number1").getText());
        labText1.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/text1").getText());
        labNumber2.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/number2").getText());
        labText2.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/text2").getText());
        labNumber3.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/number3").getText());
        labText3.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/text3").getText());
        labText4.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Home\"]/text4").getText());
    }


    public void load() {

        mainApp.appScheduler.swapScreensJobStart();
        mainApp.appScheduler.usbKeyCheckInJobStart();

        for (Antivirus currentAntivirus : mainApp.availableAntivirus) {
            System.out.println(currentAntivirus.name + " -> " + formatDateTime(currentAntivirus.lastUpdateDate));
        }
    }
}
