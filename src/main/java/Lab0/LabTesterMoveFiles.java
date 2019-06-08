package Lab0;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.TesterBaseClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

public class LabTesterMoveFiles extends TesterBaseClass {

    private String unzipDir;

    public LabTesterMoveFiles(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
                              long timeOut, int threads) {
        super(workingDir, logDir, pythonPath, idsFilePath, cleanBuild, timeOut, threads);


        if (!workingDir.endsWith("/")) {
            this.setWorkingDir(workingDir + "/");
        }

        this.unzipDir = this.getWorkingDir() + "build/";

        if (cleanBuild) {
            try {
                FileUtils.deleteDirectory(new File(unzipDir));
            } catch (IOException e) {
                this.logEntry("ERROR CANNOT CLEAN BUILDING DIRECTORY");
            }
        }
    }

    @Override
    public void test(String file) {

        Matcher match = TesterBaseClass.idRegex.matcher(file);
        String id = "";
        if (match.find()) {
            id = match.group(1) + "_" + match.group(2);
            HashMap<String, Double> subTasksGrade = new HashMap<>();
            String path = this.unzipDir + id + "/";
            String pathDest = this.unzipDir + "/";
            this.unzipAndMoveFiles(file, path, pathDest, id);

        }


    }

    private void unzipAndMoveFiles(String file, String path, String pathdest, String id) {
        try {
            if (!Files.exists(Paths.get(path))) {
                Files.createDirectories(Paths.get(path));
            }
            ArchiveExtractor.extractArchiveByExtension(this.getWorkingDir() + file, path);
            this.moveFiles(path, pathdest, "py", id);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.logEntry(id + " ERROR CANNOT CREATE FOLDER");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}





