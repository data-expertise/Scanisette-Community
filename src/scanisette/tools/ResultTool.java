package scanisette.tools;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import scanisette.models.ScanResultFile;
import scanisette.models.ScanResult;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static scanisette.Scanisette.docConfig;

public class ResultTool {

    public static void save(String file, ScanResult scanResult) {

        File theFile = new File(file);
        String parent = theFile.getParent();
        File theDir = new File(parent);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }

        BufferedWriter jsonResultWriter = null;
        try {

            ScanResultFile scanResultFile = new ScanResultFile();

            Gson inputGson = new Gson();

            //Detect if there is a [DATE] in the string
            if (file.contains("[DATE]")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String dateForFile = LocalDate.now().format(formatter);
                System.out.println("dateForFile: " + dateForFile);
                file = file.replace("[DATE]", dateForFile);
            }

            System.out.println("file: " + file);

            File f = new File(file);
            if (f.exists() && f.length() > 0) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                // Type type = new TypeToken<List<Result>>() {}.getType();
                Type type = new TypeToken<ScanResultFile>() {
                }.getType();
                scanResultFile = inputGson.fromJson(br, type);
                br.close();
            }
            scanResultFile.terminalId = docConfig.selectSingleNode("/config/terminal/id").getText();
            scanResultFile.terminalName = docConfig.selectSingleNode("/config/terminal/name").getText();
            scanResultFile.applicationName = docConfig.selectSingleNode("/config/application/name").getText();
            scanResultFile.applicationVersion = docConfig.selectSingleNode("/config/application/version").getText();


            System.out.println(Arrays.toString(scanResultFile.results.toArray()));

            scanResultFile.results.add(scanResult);
            System.out.println(Arrays.toString(scanResultFile.results.toArray()));

            Writer writer = new FileWriter(file);
            Gson outputGson = new GsonBuilder().setPrettyPrinting().create();
            outputGson.toJson(scanResultFile, writer);
            writer.flush();
            writer.close();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
