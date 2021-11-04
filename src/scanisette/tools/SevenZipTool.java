package scanisette.tools;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.List;

public class SevenZipTool {

    public static List<File> getCompressedFiles(String drive) throws IOException {
        System.out.println(drive);
        File dir = new File(drive);
        String[] extensions = new String[] { "7z", "zip", "rar", "tar", "gzip", "tar.gz"};
        //System.out.println("Getting all .txt and .jsp files in " + dir.getCanonicalPath() + " including those in subdirectories");
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
        return files;
    }

    public static boolean isEncrypted(IInArchive archive) throws SevenZipException {
        if (Boolean.TRUE.equals(archive.getArchiveProperty(PropID.ENCRYPTED))) return true;
        for (int i=0;i<archive.getNumberOfItems();i++) {
            if (Boolean.TRUE.equals(archive.getProperty(i,PropID.ENCRYPTED))) return true;
        }
        return false;
    }

    public static boolean checkIfOneEncryptedFile(List<File> files) throws IOException,SevenZipException {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        for (File file : files) {
            System.out.println("file: " + file.getCanonicalPath());
            randomAccessFile = new RandomAccessFile(file.getCanonicalPath(), "r");
            inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
            if (isEncrypted(inArchive)) {
                return true;
            }
        }
        return false;
    }

}
