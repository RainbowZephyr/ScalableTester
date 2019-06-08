package Lab3;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.ProcessHandler;
import AbstractClasses.TesterBaseClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class LabTester3 extends TesterBaseClass {

    private String unzipDir;

    public LabTester3(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
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

            TreeMap<String, Double> subTasksGrade = new TreeMap<>();
            String path = this.unzipDir + id + "/";
            double taskResult = 0;
            this.unzipAndMoveFiles(file, path, id);

            double resultGrade = 0;

            for (int i = 1; i <= 3; i++) {
                taskResult = task1(id, 1, i);

                if (taskResult == 0) {
                    resultGrade = 0;
                } else {
                    switch (i) {
                        case 1:
                            resultGrade = 2;
                            break;
                        case 2:
                            resultGrade = 2.5;
                            break;
                        case 3:
                            resultGrade = 2.5;
                            break;
                    }
                }

                subTasksGrade.put("task_3_1_" + i, resultGrade);
            }

            for (int i = 1; i <= 7; i++) {
                taskResult = task1(id, 2, i);
                if (taskResult == 0) {
                    resultGrade = 0;
                } else {
                    switch (i) {
                        case 1:
                            resultGrade = 0.5;
                            break;
                        case 2:
                            resultGrade = 1;
                            break;
                        case 3:
                            resultGrade = 1;
                            break;
                        case 4:
                            resultGrade = 0.5;
                            break;
                        case 5:
                            resultGrade = 0.5;
                            break;
                        case 6:
                            resultGrade = 1;
                            break;
                        case 7:
                            resultGrade = 0.5;
                            break;
                    }
                }

                subTasksGrade.put("task_3_2_" + i, resultGrade);
            }

            for (int i = 15; i <= 20; i++) {
                taskResult = task1(id, 3, i);
                if (taskResult == 0) {
                    resultGrade = 0;
                } else {
                    switch (i) {
                        case 1:
                            resultGrade = 0.5;
                            break;
                        case 2:
                            resultGrade = 0.5;
                            break;
                        case 3:
                            resultGrade = 0.5;
                            break;
                        case 4:
                            resultGrade = 0.5;
                            break;
                        case 5:
                            resultGrade = 0.5;
                            break;
                        case 6:
                            resultGrade = 0.5;
                            break;
                        case 7:
                            resultGrade = 0.5;
                            break;
                        case 8:
                            resultGrade = 0.5;
                            break;
                        case 9:
                            resultGrade = 0.5;
                            break;
                        case 10:
                            resultGrade = 0.5;
                            break;
                        case 11:
                            resultGrade = 0.5;
                            break;
                        case 12:
                            resultGrade = 0.5;
                            break;
                        case 13:
                            resultGrade = 0.5;
                            break;
                        case 14:
                            resultGrade = 0.5;
                            break;
                        case 15:
                            resultGrade = 0.5;
                            break;
                        case 16:
                            resultGrade = 0.5;
                            break;
                        case 17:
                            resultGrade = 0.5;
                            break;
                        case 18:
                            resultGrade = 0.5;
                            break;
                        case 19:
                            resultGrade = 0.5;
                            break;
                        case 20:
                            resultGrade = 0.5;
                            break;
                    }
                }

                subTasksGrade.put("task_3_3_" + i, resultGrade);
            }

            this.getIdsToGradeMap().put(id, subTasksGrade);
        }

    }

    private void unzipAndMoveFiles(String file, String path, String id) {
        try {
            if (!Files.exists(Paths.get(path))) {
                Files.createDirectories(Paths.get(path));
            }
            ArchiveExtractor.extractArchiveByExtension(this.getWorkingDir() + file, path);
            this.moveFiles(path, path, "py");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.logEntry(id + " ERROR CANNOT CREATE FOLDER");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double task1(String id, int subtask, int subtaskinput) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileNameDfa = "task_3_" + subtask + "_0" + ".txt";
        String testFileNameInput = "task_3_" + subtask + "_" + subtaskinput + ".txt";
        String testResultFileName = "task_3_" + subtask + "_" + subtaskinput + "_result.txt";

        try {
            String path = this.unzipDir + id + "/";
            List<String> taskPublicDfa = this.readTestFile("lab_3/" + testFileNameDfa);
            List<String> taskPublicInput = this.readTestFile("lab_3/" + testFileNameInput);
            List<String> taskPublicResultFile = this.readTestFile("lab_3/" + testResultFileName);

            try {
//                Files.write(Paths.get(path + "/" + testFileNameDfa), taskPublicDfa);
//                Files.write(Paths.get(path + "/" + testFileNameInput), taskPublicInput);

                File file = new File(path + "/" + testFileNameDfa);
                FileWriter fileWriter = new FileWriter(file);

                for (int i = 0; i < taskPublicDfa.size(); i++) {
                    if (i == taskPublicDfa.size()-1) {
                        fileWriter.write(taskPublicDfa.get(i));
                    } else {
                        fileWriter.write(taskPublicDfa.get(i) + "\n");
                    }
                }

                fileWriter.flush();
                fileWriter.close();

                file = new File(path + "/" + testFileNameInput);
                fileWriter = new FileWriter(file);

                for (int i = 0; i < taskPublicInput.size(); i++) {
                    if (i == taskPublicInput.size()-1) {
                        fileWriter.write(taskPublicInput.get(i));
                    } else {
                        fileWriter.write(taskPublicInput.get(i) + "\n");
                    }
                }
                fileWriter.flush();
                fileWriter.close();

            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 3 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_3_1.py", "--dfa-file", path + "/" + testFileNameDfa, "--input-file", path + "/" + testFileNameInput};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn(" for noob with id " + id + " for task " + "task_3_" + subtask);

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_3_1_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_3_1_result.txt");

                } else if (Files.exists(Paths.get(path + "task3_1_result.txt"))) {
                    studentResult = this.readTextFile(path + "task3_1_result.txt");

                }

                studentResult = studentResult.stream().map(s -> s.replaceAll("(,|\"|\'|\\(|\\)|\\[|\\]|\\{|\\})", " ").replaceAll("\\s+", " ").trim().toLowerCase()).filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());

                taskPublicResultFile = taskPublicResultFile.stream().map(s -> s.replaceAll("(,|\"|\'|\\(|\\)|\\[|\\]|\\{|\\})", " ").replaceAll("\\s+", " ").trim().toLowerCase()).filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());


                System.out.println("---------Student ID" + id + " subtask " + subtask + " subtaskinput " + subtaskinput + "--------------------------------------");
                System.out.println("Student result " + studentResult);
                System.out.println("Task result " + taskPublicResultFile);
                System.out.println("grade " + studentResult.equals(taskPublicResultFile));
                System.out.println("----------------------------------End Student------------------------------------------------");


                if (!studentResult.equals(taskPublicResultFile)) {
                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);

                    return 0;
                } else {
                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);

                    return 1;
                }


            } catch (Exception e) {
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK" + subtask + " PUBLIC TEST");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

}





