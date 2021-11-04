package scanisette.ui.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.HTMLEditor;
import javafx.util.Callback;
import scanisette.models.Antivirus;
import scanisette.models.AntivirusMininal;
import scanisette.tools.StringTool;
import scanisette.tools.UiTool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static scanisette.Scanisette.docConfig;
import static scanisette.Scanisette.mainApp;

public class InvitationController extends _MetaController {
    @FXML
    ImageView imvLogo;

    @FXML
    Label labZone1;

    @FXML
    Label labZone2;

    @FXML
    TableView<AntivirusMininal> tbvAntivirus;

    @FXML
    void initialize() {
        UiTool.setLogo(imvLogo, mainApp.imageProportionalScale);

        labZone1.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Invitation\"]/zone1").getText());
        labZone2.setText(docConfig.selectSingleNode("/config/screens/screen[@name=\"Invitation\"]/zone2").getText());


        tbvAntivirus.setPlaceholder(new Label("Aucun antivirus configuré"));
        UiTool.removeScrollBar(tbvAntivirus);

        TableColumn<AntivirusMininal, String> nameCol = new TableColumn<>("Nom de l'antivirus");
        nameCol.prefWidthProperty().bind(tbvAntivirus.widthProperty().multiply(0.5));
        nameCol.setResizable(false);
        nameCol.setCellValueFactory(new PropertyValueFactory<AntivirusMininal, String>("name"));

        TableColumn<AntivirusMininal, String> dateCol = new TableColumn<>("Date");
        dateCol.prefWidthProperty().bind(tbvAntivirus.widthProperty().multiply(0.5));
        dateCol.setResizable(false);
        dateCol.setCellValueFactory(new PropertyValueFactory<AntivirusMininal, String>("lastUpdateDate"));
        tbvAntivirus.getColumns().addAll(Arrays.asList(nameCol, dateCol));


        ObservableList<AntivirusMininal> data = FXCollections.<AntivirusMininal>observableArrayList();

        for (Antivirus currentAntivirus : mainApp.availableAntivirus) {
            data.add(new AntivirusMininal(currentAntivirus.name, UiTool.formatDateTime(currentAntivirus.lastUpdateDate)));
        }
        tbvAntivirus.setItems(data);

        tbvAntivirus.managedProperty().bind(tbvAntivirus.visibleProperty());
        tbvAntivirus.visibleProperty().bind(Bindings.isEmpty(tbvAntivirus.getItems()).not());

        tbvAntivirus.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                // Get the table header
                Pane header = (Pane) tbvAntivirus.lookup("TableHeaderRow");
                if (header != null && header.isVisible()) {
                    header.setMaxHeight(0);
                    header.setMinHeight(0);
                    header.setPrefHeight(0);
                    header.setVisible(false);
                    header.setManaged(false);
                }
            }
        });

        final PseudoClass colorLessThan12 = PseudoClass.getPseudoClass("green");
        final PseudoClass colorLessThan24 = PseudoClass.getPseudoClass("orange");
        final PseudoClass colorMoreThan24 = PseudoClass.getPseudoClass("red");

        tbvAntivirus.setRowFactory(new Callback<TableView<AntivirusMininal>, TableRow<AntivirusMininal>>() {
            @Override
            public TableRow<AntivirusMininal> call(TableView<AntivirusMininal> tableView) {
                final TableRow<AntivirusMininal> row = new TableRow<AntivirusMininal>() {
                    @Override
                    protected void updateItem(AntivirusMininal antivirusMinimal, boolean empty) {
                        super.updateItem(antivirusMinimal, empty);


                        if (antivirusMinimal != null) {
                            String lastUpdateDate = antivirusMinimal.getlastUpdateDate();
                            if (StringTool.isSomething(lastUpdateDate)) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                Date d1 = new Date();
                                Date d2;
                                try {
                                    d2 = sdf.parse(lastUpdateDate);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    d2 = null;
                                }

                                long difference_In_Time = d1.getTime() - d2.getTime();
                                long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60));

                                if (difference_In_Hours <= 12)
                                    setStyle("-fx-background-color: #92edc5; -fx-control-inner-background-alt: #92edc5;");
                                else if (difference_In_Hours <= 24)
                                    setStyle("-fx-background-color: #f6c271; -fx-control-inner-background-alt: #f6c271;");
                                else
                                    setStyle("-fx-background-color: #e78490; -fx-control-inner-background-alt: #e78490;");
                            }

                        } else {
                            setStyle("");
                        }
                    }
                };
                return row;
            }
        });
    }

    public void load() {
        // C'est le Home qui lance les jobs, donc vide
    }


}



