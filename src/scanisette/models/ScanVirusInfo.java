package scanisette.models;

public class ScanVirusInfo {
    public String antivirusName;
    public String antivirusLastUpdateDate;
    public String fileName;
    public String virusName;

    public ScanVirusInfo(String antivirusName, String antivirusLastUpdateDate, String fileName, String virusName) {
        this.antivirusName = antivirusName;
        this.antivirusLastUpdateDate = antivirusLastUpdateDate;
        this.fileName = fileName;
        this.virusName = virusName;
    }
}
