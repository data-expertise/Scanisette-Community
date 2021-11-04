package scanisette.tools;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Scale;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static scanisette.Scanisette.mainApp;

public class UiTool {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void setLogo(ImageView imageView, Scale imageProportionalScale) {
        org.dom4j.Node node = mainApp.docConfig.selectSingleNode("/config/logo/enable");
        if (node.getText().equals("yes")) {
            File file = new File(mainApp.docConfig.selectSingleNode("/config/logo/file").getText());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                imageView.getTransforms().setAll(imageProportionalScale);
            }
        } else {
            imageView.setVisible(false);
        }
    }

    public static String formatDateTime(FileTime fileTime) {

        if (fileTime != null) {
            LocalDateTime localDateTime = fileTime
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            return localDateTime.format(DATE_FORMATTER);
        } else {
            return "";
        }
    }

    public static <T extends Control> void removeScrollBar(T table) {
        ScrollBar scrollBar = (ScrollBar) table.queryAccessibleAttribute(AccessibleAttribute.HORIZONTAL_SCROLLBAR);
        /*
         *This null-check is for safety reasons if you are using when the table's skin isn't yet initialized.
         * If you use this method in a custom skin you wrote, where you @Override the layoutChildren method,
         * use it there, and it should be always initialized, so null-check would be unnecessary.
         *
         */
        if (scrollBar != null) {
            scrollBar.setPrefHeight(0);
            scrollBar.setMaxHeight(0);
            scrollBar.setOpacity(1);
            scrollBar.setVisible(false); // If you want to keep the scrolling functionality then delete this row.
        }
    }

}
