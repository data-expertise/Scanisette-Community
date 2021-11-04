package scanisette.models;

import java.util.ArrayList;
import java.util.List;

public class ScanResultFile {

    public String terminalId;
    public String terminalName;
    public String applicationName;
    public String applicationVersion;

    public List<ScanResult> results = new ArrayList<ScanResult>();
}
