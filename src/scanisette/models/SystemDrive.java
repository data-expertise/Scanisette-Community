package scanisette.models;

import java.io.File;
import java.util.List;

public class SystemDrive {
    public String path;
    public String driveType;
    public long freeSpace;
    public long totalSpace;
    public String deviceName;
    public String description;
    public String serialNumber;
    public String registryTime1;
    public String registryTime2;
    public String vendorId;
    public String productId;
    public String firmwareRevision;

    public SystemDrive(String path, String driveType, long freeSpace, long totalSpace) {
        this.path = path;
        this.driveType = driveType;   //Disque local //Lecteur USB  //CD Drive    //Network Drive
        this.freeSpace = freeSpace;
        this.totalSpace = totalSpace;
    }

    public boolean isInList(List<SystemDrive> drives) {

        for (SystemDrive drive : drives) {
            if (drive.path.equals(this.path)) {
                return true;
            }
        }
        return false;
    }
}
