package Lab6;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.ProcessHandler;
import AbstractClasses.TesterBaseClass;
import Lab4.FirstAndFollow;
import AbstractClasses.Grammar;
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

public class LabTester6 extends TesterBaseClass {

    private String unzipDir;
    private HashMap<Integer, Integer> testInputFiles;

    public LabTester6(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
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

        this.testInputFiles = new HashMap<>();
        this.testInputFiles.put(1, 0);
        this.testInputFiles.put(2, 0);
        this.testInputFiles.put(3, 3);
        this.testInputFiles.put(4, 4);


    }

    @Override
    public void test(String file) {
        Matcher match = TesterBaseClass.idRegex.matcher(file);
        String id = "";
        if (match.find()) {
            id = match.group(1) + "_" + match.group(2);
            TreeMap<String, Double> subTasksGrade = new TreeMap<>();
            String path = this.unzipDir + id + "/";

            this.unzipAndMoveFiles(file, path, id);


            double taskResult = 0;
            double resultGrade = 0;
            for (int i = 1; i <= 4; i++) {
//                resultGrade = task1(id, i);

                taskResult = task1(id, i);

                if (taskResult == 0) {
                    resultGrade = 0;
                } else {
                    switch (i) {
                        case 1:
                            resultGrade = 1;
                            break;
                        case 2:
                            resultGrade = 1;
                            break;
                        case 3:
                            if (taskResult == 3) {
                                resultGrade = 5.5;
                            }
                            if (taskResult == 2) {
                                resultGrade = 3.5;
                            }
                            if (taskResult == 1) {
                                resultGrade = 1.5;
                            }
                            break;
                        case 4:
                            if (taskResult == 4) {
                                resultGrade = 7.5;
                            }
                            if (taskResult == 3) {
                                resultGrade = 5.5;
                            }
                            if (taskResult == 2) {
                                resultGrade = 3.5;
                            }
                            if (taskResult == 1) {
                                resultGrade = 1.5;
                            }

                            break;
                    }
                }


                subTasksGrade.put("task_6_" + i, resultGrade);
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
        String grammarFile = "task_6_" + subtask + "_grammar.txt";
        String tableFile = "task_6_" + subtask + "_table.txt";
        String path = this.unzipDir + id + "/";


        List<String> grammarFileLines;
        List<String> tableFileLines;
        List<String> grammarParseResult;
        List<String> studentTable;
        List<String> studentParseResult;
        int result = 0;

        System.out.println("---------Student ID" + id + " subtask " + subtask + "--------------------------------------");

        if (this.testInputFiles.get(subtask) == 0) {
            try {
                grammarFileLines = this.readTestFile("lab_6/" + grammarFile);
            } catch (Exception e) {
                e.printStackTrace();
                this.logEntry("ERROR: CANNOT READ TEST FILE FOR TASK 6 - SUBTASK " + subtask);
                return 0;
            }


            try {
//                Files.write(Paths.get(path + "/" + grammarFile), grammarFileLines);


                File file = new File(path + "/" + grammarFile);
                FileWriter fileWriter = new FileWriter(file);

                for (int i = 0; i < grammarFileLines.size(); i++) {
                    if (i == grammarFileLines.size()-1) {
                        fileWriter.write(grammarFileLines.get(i));
                    } else {
                        fileWriter.write(grammarFileLines.get(i) + "\n");
                    }
                }

                fileWriter.flush();
                fileWriter.close();


            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 6 - SUBTASK: " + subtask + " ON PATH " + path);
                return 0;
            }


            Map<String, String> env = new HashMap<>();
            env.put("PYTHONPATH", path);

            String[] commandArray = {pythonPath, path + "task_6_1.py", "--grammar", path + "/" + grammarFile, "--input", path + "/" + grammarFile};


//            System.out.println("RUNNING PROCESS "+ Arrays.deepToString(commandArray));

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);
            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                System.err.println("ERROR ON CONVERGING");
                this.logEntry("ERROR: STUDENT CODE DID NOT CONVERGE ON TASK 6 FOR SUBTASK " + subtask + " WITH ID " + id);
            }



            try {

                if (Files.exists(Paths.get(path + "task_6_1_result.txt"))) {
                    studentTable = this.readTextFile(path + "task_6_1_result.txt");

                } else if (Files.exists(Paths.get(path + "task_6_2_result.txt"))) {
                    studentTable = this.readTextFile(path + "task_6_2_result.txt");
                } else {
                    return 0;
                }


                if (studentTable.get(0).toLowerCase().contains("invalid")) {
                    System.out.println("STUDENT SUCCESSFULY MATCHED " + subtask + " WITH ID " + id);
                    return 1;
                } else {
                    System.out.println("STUDENT UNSUCCESSFULY MATCHED " + subtask + " WITH ID " + id);

                    return 0;
                }

            } catch (Exception e) {
                e.printStackTrace();
                this.logEntry("ERROR: CANNOT READ STUDENT OUTPUT FOR TASK 6 - SUBTASK " + subtask + " ON PATH" + path);
            }


        } else {
            try {
                grammarFileLines = this.readTestFile("lab_6/" + grammarFile);
                tableFileLines = this.readTestFile("lab_6/" + tableFile);
            } catch (Exception e) {
                e.printStackTrace();
                this.logEntry("ERROR: CANNOT READ TEST FILE FOR TASK 6 - SUBTASK " + subtask);
                return 0;
            }


            try {
//                Files.write(Paths.get(path + "/" + grammarFile), grammarFileLines);


                File file = new File(path + "/" + grammarFile);
                FileWriter fileWriter = new FileWriter(file);

                for (int i = 0; i < grammarFileLines.size(); i++) {
                    if (i == grammarFileLines.size()-1) {
                        fileWriter.write(grammarFileLines.get(i));
                    } else {
                        fileWriter.write(grammarFileLines.get(i) + "\n");
                    }
                }

                fileWriter.flush();
                fileWriter.close();

                String grammarInput;
                List<String> grammarInputLines;
                for (int i = 1; i <= this.testInputFiles.get(subtask); i++) {
                    grammarInput = "task_6_" + subtask + "_input_" + i + ".txt";
                    grammarInputLines = this.readTestFile("lab_6/" + grammarInput);
//                    Files.write(Paths.get(path + "/" + grammarInput), grammarInputLines);


                    file = new File(path + "/" + grammarInput);
                    fileWriter = new FileWriter(file);

                    for (int j = 0; j < grammarInputLines.size(); j++) {
                        if (j == grammarInputLines.size()-1) {
                            fileWriter.write(grammarInputLines.get(j));
                        } else {
                            fileWriter.write(grammarInputLines.get(j) + "\n");
                        }
                    }
                    fileWriter.flush();
                    fileWriter.close();

                }


            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE GRAMMAR TEST STRING FOR TASK 6 - SUBTASK: " + subtask + " ON PATH " + path);
            }


            Map<String, String> env;

            ProcessHandler processHandler;

            boolean safeExit;

            String grammarInput;
            String grammarParseResultFile;

            for (int i = 1; i <= this.testInputFiles.get(subtask); i++) {
                grammarInput = "task_6_" + subtask + "_input_" + i + ".txt";
                grammarParseResultFile = "task_6_" + subtask + "_input_" + i + "_result.txt";


                String[] commandArray = {pythonPath, path + "task_6_1.py", "--grammar", path + "/" + grammarFile, "--input", path + "/" + grammarInput};

                env = new HashMap<>();
                env.put("PYTHONPATH", path);

//                System.out.println("RUNNING PROCESS "+ Arrays.deepToString(commandArray));

                processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);
                safeExit = processHandler.spawn();

                if (!safeExit) {
                    System.err.println("ERROR ON CONVERGING");
                    this.logEntry("ERROR: STUDENT CODE DID NOT CONVERGE ON TASK 6 FOR SUBTASK " + subtask + " INPUT " + i + " WITH ID " + id);
                }

                TreeMap<String, TreeMap<String, List<String>>> ourTableMap = Grammar.readTable(tableFileLines);
                TreeMap<String, TreeMap<String, List<String>>> studentTableMap;
                try {
                    studentTable = this.readTextFile(path + "task_6_1_result.txt");
                    studentParseResult = this.readTextFile(path + "task_6_2_result.txt");
                    studentTableMap = Grammar.readTable(studentTable);

                    System.out.println("Student table " + studentTableMap);
                    System.out.println("Our table " + ourTableMap);
                    System.out.println("Both are equals "  + studentTableMap.equals(ourTableMap ));


                    if(studentTableMap.equals(ourTableMap)) {
                        grammarParseResult = this.readTestFile("lab_6/" + grammarParseResultFile);
                        if(studentParseResult.get(0).toLowerCase().equals(grammarParseResult.get(0))) {
                            System.out.println("Student result " + studentParseResult.get(0).toLowerCase());
                            System.out.println("Our result " + grammarParseResult.get(0));
                            System.out.println("ID " + id + " PASSED TEST SUBTASK " + subtask + " ON INPUT " + i);
                            result++;
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    this.logEntry("ERROR: CANNOT READ STUDENT OUTPUT FOR TASK 6 - SUBTASK " + subtask + " ON PATH" + path);
                }




            }


        }

        System.out.println(result);
        System.out.println("---------------------------------End----------------------------------------");

        return result;
    }


    public static void main(String[] args) {
        String[] s = {
                "S : ( : ( S + F )",
                "S : ) :",
                "S : a : F",
                "S : + :",
                "S : $ :",
                "F : ( :",
                "F : ) :",
                "F : a : a",
                "F : + :",
                "F : $ :"

        };

        String[] s2 = {
                "S : ( : ( S + F )",
                "S : a : F",
                "S : ) :",
                "S : + :",
                "F : + :",
                "F : ( :",
                "F : ) :",
                "S : $ :",
                "F : a : a",
                "F : $ :"

        };


        TreeMap<String, TreeMap<String, List<String>>> t = Grammar.readTable(Arrays.asList(s));
        TreeMap<String, TreeMap<String, List<String>>> t2 = Grammar.readTable(Arrays.asList(s2));


        System.out.println(t);
        System.out.println(t2);
        System.out.println(t.equals(t2));

//        System.out.println(Grammar.readTable(Arrays.asList(s)));


//        FirstAndFollow ff = new FirstAndFollow("","");
//        ff.parseGrammar(Grammar.readGrammar(Arrays.asList(s)));
        LinkedHashMap<String, List<List<String>>> g = Grammar.readGrammar(Arrays.asList(s));

//        for (int i = 0; i < 100; i++) {
//            System.out.println(Grammar.generateStrings(g,1000));
//
//        }
//

    }

}





