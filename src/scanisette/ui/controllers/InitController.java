package scanisette.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import static scanisette.Scanisette.mainApp;


public class InitController extends _MetaController {

    @FXML
    public TextArea logArea;

    @FXML
    void initialize() {
        logArea.setEditable(false);
        logArea.appendText("\r\n");
    }


    public void load() {
        
    }

    public void logAreaAppendTextLn(String text) {
        logArea.appendText(text);
        logArea.appendText("\r\n");
    }

    public void logAreaAppendText(String text) {
        logArea.appendText(text);
    }


}
