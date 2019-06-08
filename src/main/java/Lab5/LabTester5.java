package Lab5;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.ProcessHandler;
import AbstractClasses.TesterBaseClass;
import Lab4.FirstAndFollow;
import AbstractClasses.Grammar;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class LabTester5 extends TesterBaseClass {

    private String unzipDir;

    public LabTester5(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
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
            for (int i = 1; i <= 10; i++) {
                taskResult = task1(id, i);

                switch (i) {
                    case 1:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 3.75;
                                break;
                            case 2:
                                resultGrade = 3.75;
                                break;
                        }
                        break;
                    case 2:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 3:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 4:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 5:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 6:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 7:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 8:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 0.5;
                                break;
                            case 2:
                                resultGrade = 1;
                                break;
                        }
                        break;
                    case 9:
//                        switch ((int) taskResult) {
//                            case 0:
//                                resultGrade = 0;
//                                break;
//                            case 1:
//                                resultGrade = 1.5;
//                                break;
//                            case 2:
//                                resultGrade = 3;
//                                break;
//                        }
                        resultGrade = 3;
                        break;
                    case 10:
                        switch ((int) taskResult) {
                            case 0:
                                resultGrade = 0;
                                break;
                            case 1:
                                resultGrade = 1;
                                break;
                            case 2:
                                resultGrade = 2;
                                break;
                        }
                        break;
                }
                subTasksGrade.put("task_5_" + i, resultGrade);
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

    private double task1(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_5_" + subtask + ".txt";
        String testResultFileName = "task_5_" + subtask + "_result.txt";

        try {
            String path = this.unzipDir + id + "/";
            List<String> taskPublicInput = this.readTestFile("lab_5/" + testFileName);
            List<String> taskPublicResultFile = this.readTestFile("lab_5/" + testResultFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 5 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_5_1.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_5_1_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_5_1_result.txt");

                } else if (Files.exists(Paths.get(path + "task5_1_result.txt"))) {
                    studentResult = this.readTextFile(path + "task5_1_result.txt");

                }

                studentResult = studentResult.stream().filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());

                HashMap<String, HashMap<Set<String>, Set<String>>> studentMap = this.generateFFHashMap(studentResult,
                        true);

                HashMap<String, HashMap<Set<String>, Set<String>>> result = this.generateFFHashMap(taskPublicResultFile,
                        true);


                double grade = 0;
//                if (result.equals(studentMap)) {
//                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);
//                    return 1;
//                }


                boolean flagFirst = true;
                boolean flagFollow = true;

                System.out.println("----------------------------------Student ID" + id + " Task " + subtask + "------------------------------------------------");

                System.out.println("student result " + studentResult);
                System.out.println("student map " + studentMap);
                System.out.println("our map " + result);

                if (studentMap.isEmpty()) {
                    grade = 0;
                } else {

                    for (String key : result.keySet()
                    ) {

                        if (result.get(key) == null || studentMap.get(key) == null) {
                            System.out.println("Fail");
                            continue;
                        }

                        if (flagFirst && result.get(key).keySet().size() == studentMap.get(key).keySet().size() && result.get(key).keySet().containsAll(studentMap.get(key).keySet())) {
                            flagFirst = true;
                        } else {
                            flagFirst = false;
                        }

                        if (flagFollow && result.get(key).values().size() == studentMap.get(key).values().size() && result.get(key).values().containsAll(studentMap.get(key).values())) {
                            flagFollow = true;
                        } else {
                            flagFollow = false;
                        }

                    }

                    System.out.println("first " + flagFirst + " follow " + flagFollow);

                    if (flagFirst) {
                        grade += 1;
                    }
                    if (flagFollow) {
                        grade += 1;
                    }

//                studentMap = this.generateFFHashMap(studentResult, false);
//
//                result = this.generateFFHashMap(taskPublicResultFile, false);
//
//                if (result.equals(studentMap)) {
//                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);
//
//                    return 0.5;
//                }
                }
//
                System.out.println("grade " + grade);
                System.out.println("----------------------------------End Student------------------------------------------------");

                return grade;

            } catch (Exception e) {
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK" + subtask + " PUBLIC TEST");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static HashMap<String, HashMap<Set<String>, Set<String>>> generateFFHashMap(List<String> studentFile,
                                                                                        boolean handleSpace) {
        HashMap<String, HashMap<Set<String>, Set<String>>> firstAndFollow = new HashMap<>();
        studentFile.forEach(line -> {
            String[] tuple = line.split(":");
//            System.out.println("TUPLE " + Arrays.deepToString(tuple));

            firstAndFollow.put(tuple[0].trim(), new HashMap<Set<String>, Set<String>>());

            try {
                if (handleSpace) {
                    firstAndFollow.get(tuple[0].trim()).put(
                            new HashSet<>(Arrays.stream(tuple[1].split(" ")).filter(x -> !x.equals("")).collect(Collectors.toList())),
                            new HashSet<>(Arrays.stream(tuple[2].split(" ")).filter(x -> !x.equals("")).collect(Collectors.toList())));
                } else {
                    firstAndFollow.get(tuple[0].trim()).put(
                            new HashSet<>(Arrays.stream(tuple[1].split("")).filter(x -> !x.equals("") && !x.equals(" ")).collect(Collectors.toList())),
                            new HashSet<>(Arrays.stream(tuple[2].split("")).filter(x -> !x.equals("") && !x.equals(" ")).collect(Collectors.toList())));
                }
            } catch (Exception e) {
                System.out.println("Wrong format");
            }

        });

        return firstAndFollow;
    }

    public static void main(String[] args) {
        String[] s = {
        "S : a b c : $",
        "A : a epsilon : b c",
        "B : b epsilon : c",
        "C : c : d e $",
        "D : d epsilon : e $",
        "E : e epsilon : $"
};
        String[] s2 = {
                "S : a b c : $",
                "B : b epsilon : c",
                "A : a epsilon : b c",
                "C : c : d e $",
                "D : d epsilon : e $",
                "E : e epsilon : $"
        };


        System.out.println(        generateFFHashMap(Arrays.asList(s), false).equals(        generateFFHashMap(Arrays.asList(s2), false))
);


    }

}





