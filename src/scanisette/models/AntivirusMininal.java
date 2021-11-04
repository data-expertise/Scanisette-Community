package scanisette.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.attribute.FileTime;

public class AntivirusMininal {

    private  StringProperty name;
    private  StringProperty lastUpdateDate;

    public AntivirusMininal(String fName, String fLastUpdateDate) {
        this.name = new SimpleStringProperty(this, "name", fName);
        this.lastUpdateDate = new SimpleStringProperty(this, "lastUpdateDate", fLastUpdateDate);
    }

    public String getname() {
        return name.get();
    }

    public  void setname(String fName) {
        name.set(fName);
    }

    public StringProperty nameProperty() {
        return name ;
    }

    public  String getlastUpdateDate() {
        return lastUpdateDate.get();
    }

    public  void setlastUpdateDate(String fLastUpdateDate) {
        lastUpdateDate.set(fLastUpdateDate);
    }

    public  StringProperty lastUpdateDateProperty() {
        return lastUpdateDate ;
    }

}
