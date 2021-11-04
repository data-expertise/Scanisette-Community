package scanisette.models;

import com.google.gson.JsonObject;
import scanisette.tools.StringTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static scanisette.Scanisette.mainApp;

public class ClamavAntivirus {

    static double progressValue;
    static Matcher matcher;

    static String filename = "";
    static String virusname = "";

    public static JsonObject processLine(String line, Pattern progressPattern, Pattern filePattern, String memory) {

        JsonObject output = new JsonObject();

        // traitement de la progression
        matcher = progressPattern.matcher(line);
        if (matcher.find()) {
            progressValue = Double.parseDouble(matcher.group(1)) / 100.0;
            output.addProperty("Action","Progress");
            output.addProperty("Progress",progressValue);
            return output;
        }

        // traitement d'une ligne de fichier
        matcher = filePattern.matcher(line);  //2021-05-20 22:23:30	D:\eicar_com.zip//eicar.com	detected	EICAR-Test-File
        if (matcher.find()) {
            System.out.println("GRP1 : " + matcher.group(1).trim()); // filename
            System.out.println("GRP2 : " + matcher.group(2).trim()); // virusname
            System.out.println("GRP3 : " + matcher.group(3).trim()); // FOUND
            filename=matcher.group(1).trim();
            virusname=matcher.group(2).trim();

            if (matcher.group(3).trim().equals("FOUND")) {
                //virus dans fichier
                output.addProperty("Action", "File");
                output.addProperty("Status", "Virus");
                output.addProperty("FileName", filename);
                output.addProperty("VirusName", virusname);
                return output;
            }
            output.addProperty("Action", "File");
            output.addProperty("Status", "Unknown");
            return output;
        }
        output.addProperty("Action", "Nop");
        return output;
    }

    public static FileTime getLastUpdateDate(String installationFolder, String commandForDate, String commandForDatePattern, String fileForDate) {
        String cmd;
        File dir;
        FileTime lastUpdateDate = null;

        try {
            if (StringTool.isSomething(commandForDate)) {
                dir = new File(installationFolder);
                cmd = installationFolder + commandForDate;
                System.out.println(cmd);
                Process process = Runtime.getRuntime().exec(cmd, null, dir);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String s = null;
                String found;
                if (StringTool.isSomething(commandForDatePattern)) {
                    Pattern datePattern = Pattern.compile(commandForDatePattern);
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                        matcher = datePattern.matcher(s);
                        if (matcher.find()) {
                            found = matcher.group(1).trim();
                            //found = found.substring(4);
                            System.out.println(found);
                            Date date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH).parse(found); // Ne pas virer le Locale.ENGLISH sinon ça marche plus
                            lastUpdateDate = FileTime.fromMillis(date.getTime());
                            break;
                        }
                    }
                }
                else {
                    lastUpdateDate = null;
                }
            }
            else {
                Path fileForDatePath = Paths.get(fileForDate);
                if (Files.exists(fileForDatePath)) {
                    BasicFileAttributes attr = Files.readAttributes(fileForDatePath, BasicFileAttributes.class);
                    lastUpdateDate = attr.lastModifiedTime();
                } else {
                    lastUpdateDate = null;
                }
            }
            return lastUpdateDate;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
