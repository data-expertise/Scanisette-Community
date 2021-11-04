package scanisette.models;

import scanisette.Scanisette;

import java.nio.file.attribute.FileTime;
import java.time.DateTimeException;
import java.util.Date;

import static scanisette.Scanisette.mainApp;

public class Antivirus {
    public String name ;
    public String installationFolder ;
    public String executableFile ;
    public String parameters ;
    public String progressPattern;
    public String filePattern;
    public String commandForDate;
    public String commandForDatePattern;
    public String fileForDate;
    public FileTime lastUpdateDate;

    public Antivirus( String name, String installationFolder, String executableFile, String parameters, String progressPattern, String filePattern, String commandForDate, String commandForDatePattern, String fileForDate, FileTime lastUpdateDate) {
        this.name = name;
        this.installationFolder = installationFolder;
        this.executableFile = executableFile;
        this.parameters = parameters;
        this.progressPattern = progressPattern;
        this.filePattern = filePattern;
        this.commandForDate = commandForDate;
        this.commandForDatePattern = commandForDatePattern;
        this.fileForDate = fileForDate;
        this.lastUpdateDate = lastUpdateDate;
    }


}
