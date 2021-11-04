package scanisette.models;

import java.util.List;

public class ScanResult {
    public String date;
    public String usbkeyDeviceName;
    public String usbkeyDescription;
    public String usbkeySerialNumber;
    public String usbkeyRegistryTime1;
    public String usbkeyRegistryTime2;
    public String usbkeyVendorId;
    public String usbkeyProductId;
    public String usbkeyFirmwareRevision;
    public String operationVerb;
    public String operationOutput;
    public List<ScanVirusInfo> virusInfos;
}
