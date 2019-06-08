package AbstractClasses;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.tar.TarXZUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;

import java.io.*;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;

public class ArchiveExtractor {

    public static void extractArchiveByExtension(String inputPath, String destinationDirectory) {

        if (inputPath.endsWith("zip")) {
            extractZip(inputPath, destinationDirectory);
        } else if (inputPath.endsWith("rar")) {
            extractRAR(inputPath, destinationDirectory);
        } else if (inputPath.endsWith("xz")) {
            extractXZ(inputPath, destinationDirectory);
        } else if (inputPath.endsWith("gz")) {
            extractGZ(inputPath, destinationDirectory);
        } else {
            System.err.println("Unknown File Format " + inputPath);
        }
    }

    public static void extractArchiveByExtension(String inputPath, String destinationDirectory, BlockingQueue loggingQueue) {
        if (inputPath.endsWith("zip")) {
            extractZip(inputPath, destinationDirectory);
        } else if (inputPath.endsWith("rar")) {
            extractRAR(inputPath, destinationDirectory);
        } else if (inputPath.endsWith("xz")) {
            extractXZ(inputPath, destinationDirectory);
        } else if (inputPath.endsWith("gz")) {
            extractGZ(inputPath, destinationDirectory);
        } else {
            System.err.println("Unknown File Format " + inputPath);

            try {
                loggingQueue.put(new Timestamp(System.currentTimeMillis()) + " ERROR UNKNOWN FILE EXTENSION " + inputPath);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void extractGZ(String inputPath, String destinationDirectory) {
        File inputFile = new File(inputPath);
        File outputFile = new File(destinationDirectory);
        TarGZipUnArchiver tarGZipUnArchiver = new TarGZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
        ConsoleLoggerManager manager = new ConsoleLoggerManager();
        manager.initialize();
        tarGZipUnArchiver.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
        tarGZipUnArchiver.setSourceFile(inputFile);
        outputFile.mkdirs();
        tarGZipUnArchiver.setDestDirectory(outputFile);
        tarGZipUnArchiver.extract();
    }

    public static void extractXZ(String inputPath, String destinationDirectory) {
        File inputFile = new File(inputPath);
        File outputFile = new File(destinationDirectory);
        TarXZUnArchiver tarXZUnArchiver = new TarXZUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
        ConsoleLoggerManager manager = new ConsoleLoggerManager();
        manager.initialize();
        tarXZUnArchiver.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
        tarXZUnArchiver.setSourceFile(inputFile);
        outputFile.mkdirs();
        tarXZUnArchiver.setDestDirectory(outputFile);
        tarXZUnArchiver.extract();
    }


    public static void extractRAR(String inputPath, String destinationDirectory) {
        File f = new File(inputPath);
        Archive a = null;
        try {
            a = new Archive(new FileVolumeManager(f));
        } catch (RarException e) {
            e.printStackTrace();
            System.err.println("CORRUPT RAR " + inputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (a != null) {
            FileHeader fh = a.nextFileHeader();
            while (fh != null) {
                try {
                    File out = new File(destinationDirectory
                            + fh.getFileNameString().trim());

                    FileOutputStream os = new FileOutputStream(out);
                    a.extractFile(fh, os);
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (RarException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fh = a.nextFileHeader();
            }
        }
    }


    public static void extractZip(String inputPath, String destinationDirectory) {
        File inputFile = new File(inputPath);
        File outputFile = new File(destinationDirectory);
        ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
// Logging - as @Akom noted, logging is mandatory in newer versions, so you can use a code like this to configure it:
        ConsoleLoggerManager manager = new ConsoleLoggerManager();
        manager.initialize();
        zipUnArchiver.enableLogging(manager.getLoggerForComponent("bla"));
// -- end of logging part
        zipUnArchiver.setSourceFile(inputFile);

        outputFile.mkdirs();
        zipUnArchiver.setDestDirectory(outputFile);
        zipUnArchiver.extract();

    }

}
