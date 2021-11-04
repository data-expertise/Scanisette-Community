package scanisette.models;


import com.google.gson.JsonObject;
import scanisette.ui.controllers.ScanningController;

import java.io.*;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static scanisette.Scanisette.logger;
import static scanisette.Scanisette.mainApp;

public class StreamGobbler implements Runnable {
    protected Process process;
    protected InputStream inputStream;
    protected Antivirus antivirus;

    public StreamGobbler( Process process, Antivirus antivirus ) {
        this.process = process;
        this.inputStream = process.getInputStream();
        //this.consumer = consumer;
        this.antivirus = antivirus;
    }

    

    @Override
    public void run() {
        try
        {
            System.out.println("STREAMGOBBLER RUN");


            Class<?> customAntivirusClass = Class.forName("scanisette.models."+antivirus.name+"Antivirus");
            Method processLine = customAntivirusClass.getMethod("processLine", String.class, Pattern.class, Pattern.class, String.class);

            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String line;
            JsonObject resultFromLine;

            // https://regex101.com/r/fVTQjy/4/
            System.out.println(">>>>>> "+ antivirus.progressPattern);

            String filePatternTemp = antivirus.filePattern;
            String usbEscape = mainApp.currentUsbDrive.path.replace("\\","\\\\");
            filePatternTemp = filePatternTemp.replace("[USB]",usbEscape);
            System.out.println(">>>>>> "+ filePatternTemp);
            Pattern progressPattern = Pattern.compile(antivirus.progressPattern);
            Pattern filePattern = Pattern.compile(filePatternTemp);

            boolean virusFound = false;
            String memory = "";

            while ( (line = br.readLine()) != null) {

                System.out.println(antivirus.name + " : " + line);
                resultFromLine = (JsonObject) processLine.invoke(null, line, progressPattern, filePattern, memory);
                System.out.println(resultFromLine.toString());

                if (resultFromLine.has("memory")) {
                    memory = resultFromLine.get("memory").getAsString();
                }

                // on traite la progression
                if (resultFromLine.get("Action").getAsString().equals("Progress") || resultFromLine.get("Action").getAsString().equals("ProgressAndFile")) {
                    double progressValue = Double.parseDouble(resultFromLine.get("Progress").getAsString());
                    ((ScanningController) mainApp.currentSceneController).setProgressBar(progressValue);
                }

                // on traite un retour fichier
                if (resultFromLine.get("Action").getAsString().equals("File") || resultFromLine.get("Action").getAsString().equals("ProgressAndFile")) {

                    // on traite le retour de l'analyse viral
                    switch (resultFromLine.get("Status").getAsString()) {
                        case "Ok":
                            break;
                        case "Virus":
                            virusFound = true;
                            mainApp.positiveFileCount++;
                            mainApp.scanResult.virusInfos.add(new ScanVirusInfo(mainApp.currentAntivirus.name,mainApp.currentAntivirus.lastUpdateDate.toString(),resultFromLine.get("FileName").getAsString(),resultFromLine.get("VirusName").getAsString() ));

                            if (process.isAlive())
                                process.destroy();
                            logger.info(mainApp.currentAntivirus.name + " : Process destroyed because VIRUS !!!");
                            break;
                    }
                }

                if (virusFound) {
                    break;  // boucle du streamgobbler
                }

            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }



}

