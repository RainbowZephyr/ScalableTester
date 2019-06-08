package Lab1;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.OSValidator;
import AbstractClasses.ProcessHandler;
import AbstractClasses.TesterBaseClass;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.sleep;

public class LabTester1 extends TesterBaseClass {

    private String unzipDir;

    public LabTester1(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
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
            int taskResult = 0;
            if (this.cleanBuild) {
                this.unzipAndMoveFiles(file, path, id);
            }

            double resultGrade = 0;

      for (int i = 1; i <= 8; i++) {
                System.out.println("TASK " + i);
                if (i == 8) {
                    taskResult = task1SubTask8Public(id, i);
                    if (taskResult == 1) {
                        resultGrade = 1.5;
                    } else {
                        resultGrade = 0;
                    }
                } else {

                    taskResult = task1SubTaskNPublic(id, i);

                    switch (i) {
                        case 1:
                            if (taskResult == 1) {
                                resultGrade = 0.25;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                        case 2:
                            if (taskResult == 1) {
                                resultGrade = 0.25;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                        case 3:
                            if (taskResult == 1) {
                                resultGrade = 0.25;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                        case 4:
                            if (taskResult == 1) {
                                resultGrade = 0.5;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                        case 5:
                            if (taskResult == 1) {
                                resultGrade = 0.5;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                        case 6:
                            if (taskResult == 1) {
                                resultGrade = 0.5;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                        case 7:
                            if (taskResult == 1) {
                                resultGrade = 1.25;
                            } else {
                                resultGrade = 0;
                            }
                            break;
                    }

                }
                subTasksGrade.put("task_1_" + i, (double) resultGrade);
            }


            double taskResult2 = 0;

            for (int j = 1; j <= 13; j++) {

                taskResult2 = task2Handler(id, j + "");

                subTasksGrade.put("task_2_" + j, taskResult2);

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


    private int task1SubTaskNPublic(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_1_" + subtask + "_public.txt";
        String testResultFileName = "task_1_" + subtask + "_public_result.txt";


        try {
            String path = this.unzipDir + id + "/";

            this.clearOrCreateFile(path + "task_1_" + subtask + "_result.txt");
            this.clearOrCreateFile(path + "task1_" + subtask + "_result.txt");


            List<String> taskPublicInput = this.readTestFile("lab_1/" + testFileName);
            List<String> taskPublicResultFile = this.readTestFile("lab_1/" + testResultFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 1 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_1_" + subtask + ".py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_1_" + subtask + "_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_1_" + subtask + "_result.txt");

                } else if (Files.exists(Paths.get(path + "task1_" + subtask + "_result.txt"))) {
                    studentResult = this.readTextFile(path + "task1_" + subtask + "_result.txt");

                }
//                studentResult = studentResult.stream().filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());


                System.out.println("----------------Regex " + subtask + " --------------------------");
                System.out.println("Student ID " + id);

                System.out.println(studentResult);

                System.out.print("grade is ");
                System.out.println(!taskPublicResultFile.equals(studentResult)? 0 : 1);

                System.out.println("------------------------- End ----------------------------------");


                if (!taskPublicResultFile.equals(studentResult)) {
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

    private int task1SubTask8Public(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_1_" + subtask + "_public.txt";
        String testResultFileNameInplace = "task_1_" + subtask + "_public_result_inplace.txt";
        String testResultFileNameNew = "task_1_" + subtask + "_public_result_new.txt";
        String testResultFileNameNewStripped = "task_1_" + subtask + "_public_result_new_stripped.txt";


        try {
            String path = this.unzipDir + id + "/";

            this.clearOrCreateFile(path + "task_1_" + subtask + "_result.txt");
            this.clearOrCreateFile(path + "task1_" + subtask + "_result.txt");

            List<String> taskPublicInput = this.readTestFile("lab_1/" + testFileName);
            List<String> taskPublicResultFileInplace = this.readTestFile("lab_1/" + testResultFileNameInplace);
            List<String> taskPublicResultFileNew = this.readTestFile("lab_1/" + testResultFileNameNew);
            List<String> taskPublicResultFileNewStripped = this.readTestFile("lab_1/" + testResultFileNameNewStripped);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 1 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_1_" + subtask + ".py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_1_" + subtask + "_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_1_" + subtask + "_result.txt");

                } else if (Files.exists(Paths.get(path + "task1_" + subtask + "_result.txt"))) {
                    studentResult = this.readTextFile(path + "task1_" + subtask + "_result.txt");

                }
                studentResult = studentResult.stream().filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());


                System.out.println("----------------Regex " + subtask + " --------------------------");
                System.out.println("Student ID " + id);

                System.out.println(studentResult);

                System.out.print("grade is ");
                System.out.println(!taskPublicResultFileInplace.equals(studentResult)? 0 : 1);
                System.out.println(!taskPublicResultFileNew.equals(studentResult)? 0 : 1);
                System.out.println(!taskPublicResultFileNewStripped.equals(studentResult)? 0 : 1);

                System.out.println("------------------------- End ----------------------------------");

                int result = 0;

                if (taskPublicResultFileInplace.equals(studentResult)) {
                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);
                    this.logEntry(id + ": STUDENT USED INPLACE RESULTS");
                    result = 1;
                } else if (taskPublicResultFileNew.equals(studentResult)) {
                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);
                    this.logEntry(id + ": STUDENT USED NEW FILE RESULTS WITHOUT STRIPPING");
                    result = 1;
                } else if (taskPublicResultFileNewStripped.equals(studentResult)) {
                    this.logEntry(id + ": FINISHED TESTING TASK 1 PUBLIC TEST " + subtask);
                    this.logEntry(id + ": STUDENT USED NEW FILE RESULTS WITH STRIPPING");
                    result = 1;
                }

                return result;

            } catch (Exception e) {
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK" + subtask + " PUBLIC TEST");
            }

        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double task2Handler(String id, String subtask) {


        switch (Integer.parseInt(subtask)) {

            case 1: // Single alphabet 'a'
                ArrayList<String> testInput1 = new ArrayList<>();
                testInput1.add("s");
                int taskResult1_1 = task2(id, subtask + "_1", testInput1);
                testInput1.clear();
                testInput1.add("a");
                int taskResult1_2 = task2(id, subtask + "_2", testInput1);
                testInput1.clear();
                testInput1.add("w");
                int taskResult1_3 = task2(id, subtask + "_3", testInput1);
                testInput1.clear();
                testInput1.add("www");
                int taskResult1_4 = task2(id, subtask + "_3", testInput1);
                testInput1.clear();
                testInput1.add("ww");
                int taskResult1_5 = task2(id, subtask + "_3", testInput1);
                testInput1.clear();
                testInput1.add(" ");
                int taskResult1_6 = task2(id, subtask + "_3", testInput1);

                if (taskResult1_1 == 1 && taskResult1_2 == 1 && taskResult1_3 == 1 && taskResult1_4 == 0 && taskResult1_5 == 0 && taskResult1_6 == 0) {
                    return 0.5;
                }

                testInput1 = null;

                return 0;

            case 2: // Concatenation '.'
                ArrayList<String> testInput2 = new ArrayList<>();
                testInput2.add("ab");
                int taskResult2_1 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();
                testInput2.add("st");
                int taskResult2_2 = task2(id, subtask + "_2", testInput2);
                testInput2.clear();
                testInput2.add("wy");
                int taskResult2_3 = task2(id, subtask + "_3", testInput2);
                testInput2.clear();
                testInput2.add("w");
                int taskResult2_4 = task2(id, subtask + "_3", testInput2);
                testInput2.clear();
                testInput2.add("w");
                int taskResult2_5 = task2(id, subtask + "_3", testInput2);
                testInput2.clear();
                testInput2.add("y");
                int taskResult2_6 = task2(id, subtask + "_3", testInput2);
                testInput2.clear();
                testInput2.add("wyy");
                int taskResult2_7 = task2(id, subtask + "_3", testInput2);
                testInput2.clear();
                testInput2.add("wyy");
                int taskResult2_8 = task2(id, subtask + "_3", testInput2);
                testInput2.clear();
                testInput2.add(" ");
                int taskResult2_9 = task2(id, subtask + "_3", testInput2);

                testInput2 = null;

                if (taskResult2_1 == 1 && taskResult2_2 == 1 && taskResult2_3 == 1 && taskResult2_4 == 0 && taskResult2_5 == 0 && taskResult2_6 == 0 && taskResult2_7 == 0 && taskResult2_8 == 0 && taskResult2_9 == 0) {
                    return 1;
                }

                return 0;

            case 3: // Union 'or'
                ArrayList<String> testInput3 = new ArrayList<>();
                testInput3.add("s");
                testInput3.add("t");
                int taskResult3_1 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();
                testInput3.add("a");
                testInput3.add("b");
                int taskResult3_2 = task2(id, subtask + "_2", testInput3);
                testInput3.clear();
                testInput3.add("w");
                testInput3.add("y");
                int taskResult3_3 = task2(id, subtask + "_3", testInput3);
                testInput3.clear();
                testInput3.add("wy");
                int taskResult3_4 = task2(id, subtask + "_3", testInput3);
                testInput3.clear();
                testInput3.add("wyy");
                int taskResult3_5 = task2(id, subtask + "_3", testInput3);
                testInput3.clear();
                testInput3.add(" ");
                int taskResult3_6 = task2(id, subtask + "_3", testInput3);

                testInput3 = null;

                if (taskResult3_1 == 1 && taskResult3_2 == 1 && taskResult3_3 == 1 && taskResult3_4 == 0 && taskResult3_5 == 0 && taskResult3_6 == 0) {
                    return 1;
                }

                return 0;

            case 4: // Keleen star '*'
                ArrayList<String> testInput4 = new ArrayList<>();
                testInput4.add(" ");
                testInput4.add("s");
                testInput4.add("ss");
                testInput4.add("sss");
                testInput4.add("sssssssssssssssss");
                int taskResult4_1 = task2(id, subtask + "_1", testInput4);
                testInput4.clear();
                testInput4.add(" ");
                testInput4.add("a");
                testInput4.add("aa");
                testInput4.add("aaa");
                testInput4.add("aaaaaaaaaaaaaaaaa");
                int taskResult4_2 = task2(id, subtask + "_2", testInput4);
                testInput4.clear();
                testInput4.add(" ");
                testInput4.add("w");
                testInput4.add("ww");
                testInput4.add("www");
                testInput4.add("wwwwwwwwwwwwwwwww");
                int taskResult4_3 = task2(id, subtask + "_3", testInput4);

                testInput4 = null;

                if (taskResult4_1 == 1 && taskResult4_2 == 1 && taskResult4_3 == 1) {
                    return 1;
                }

                return 0;

            case 5: // one or more '+'
                ArrayList<String> testInput5 = new ArrayList<>();
                testInput5.add("s");
                testInput5.add("ss");
                testInput5.add("sss");
                testInput5.add("sssssssssssssssss");
                int taskResult5_1 = task2(id, subtask + "_1", testInput5);
                testInput5.clear();
                testInput5.add("a");
                testInput5.add("aa");
                testInput5.add("aaa");
                testInput5.add("aaaaaaaaaaaaaaaaa");
                int taskResult5_2 = task2(id, subtask + "_2", testInput5);
                testInput5.clear();
                testInput5.add("w");
                testInput5.add("ww");
                testInput5.add("www");
                testInput5.add("wwwwwwwwwwwwwwwww");
                int taskResult5_3 = task2(id, subtask + "_3", testInput5);
                testInput5.clear();
                testInput5.add(" ");
                int taskResult5_4 = task2(id, subtask + "_3", testInput5);

                testInput5 = null;

                if (taskResult5_1 == 1 && taskResult5_2 == 1 && taskResult5_3 == 1 && taskResult5_4 == 0) {
                    return 0.5;
                }

                return 0;

            case 6: // zero or one '?'
                ArrayList<String> testInput6 = new ArrayList<>();
                testInput6.add("s");
                testInput6.add(" ");
                int taskResult6_1 = task2(id, subtask + "_1", testInput6);
                testInput6.clear();
                testInput6.add("a");
                testInput6.add(" ");
                int taskResult6_2 = task2(id, subtask + "_2", testInput6);
                testInput6.clear();
                testInput6.add("w");
                testInput6.add(" ");
                int taskResult6_3 = task2(id, subtask + "_3", testInput6);
                testInput6.clear();
                testInput6.add("ww");
                int taskResult6_4 = task2(id, subtask + "_3", testInput6);

                testInput6 = null;

                if (taskResult6_1 == 1 && taskResult6_2 == 1 && taskResult6_3 == 1 && taskResult6_4 == 0) {
                    return 0.5;
                }

                return 0;

            case 7:     // EPSILON
                ArrayList<String> testInput7 = new ArrayList<>();

                testInput7.add(" ");
                int taskResult7_1 = task2SubTask7(id, subtask + "_1", testInput7);
                int taskResult7_2 = task2SubTask7(id, subtask + "_2", testInput7);
                int taskResult7_3 = task2SubTask7(id, subtask + "_3", testInput7);
                int taskResult7_4 = task2SubTask7(id, subtask + "_4", testInput7);
                int taskResult7_5 = task2SubTask7(id, subtask + "_5", testInput7);
                int taskResult7_6 = task2SubTask7(id, subtask + "_6", testInput7);

                if (taskResult7_1 == 1 || taskResult7_2 == 1 || taskResult7_3 == 1 || taskResult7_4 == 1 || taskResult7_5 == 1 || taskResult7_6 == 1) {
                    return 0.5;
                }

                testInput7 = null;

                return 0;

            case 8:     // (st)+
                ArrayList<String> testInput8 = new ArrayList<>();
                testInput8.add("st");
                testInput8.add("stst");
                testInput8.add("stststststst");
                int taskResult8_1 = task2(id, subtask + "_1", testInput8);
                testInput8.clear();
                testInput8.add("ab");
                testInput8.add("abab");
                testInput8.add("abababababab");
                int taskResult8_2 = task2(id, subtask + "_2", testInput8);
                testInput8.clear();
                testInput8.add("wy");
                testInput8.add("wywy");
                testInput8.add("wywywywywywy");
                int taskResult8_3 = task2(id, subtask + "_3", testInput8);
                testInput8.clear();
                testInput8.add("w");
                int taskResult8_4 = task2(id, subtask + "_3", testInput8);
                testInput8.clear();
                testInput8.add("ww");
                int taskResult8_5 = task2(id, subtask + "_3", testInput8);
                testInput8.clear();
                testInput8.add(" ");
                int taskResult8_6 = task2(id, subtask + "_3", testInput8);

                if (taskResult8_1 == 1 && taskResult8_2 == 1 && taskResult8_3 == 1 && taskResult8_4 == 0 && taskResult8_5 == 0 && taskResult8_6 == 0) {
                    return 1;
                }

                testInput8 = null;

                return 0;

            case 9:     // (s|t)*
                ArrayList<String> testInput9 = new ArrayList<>();
                testInput9.add(" ");
                testInput9.add("st");
                testInput9.add("ststst");
                testInput9.add("sssttt");
                testInput9.add("ssststtt");
                int taskResult9_1 = task2(id, subtask + "_1", testInput9);
                testInput9.clear();
                testInput9.add(" ");
                testInput9.add("ab");
                testInput9.add("ababab");
                testInput9.add("aaabbb");
                testInput9.add("aaababbb");
                int taskResult9_2 = task2(id, subtask + "_2", testInput9);
                testInput9.clear();
                testInput9.add(" ");
                testInput9.add("wy");
                testInput9.add("wywywy");
                testInput9.add("wwwyyy");
                testInput9.add("wwwywyyy");
                int taskResult9_3 = task2(id, subtask + "_3", testInput9);

                if (taskResult9_1 == 1 && taskResult9_2 == 1 && taskResult9_3 == 1) {
                    return 0.5;
                }

                testInput9 = null;

                return 0;

            case 10:    // (s*|t*)*
                ArrayList<String> testInput10 = new ArrayList<>();
                testInput10.add(" ");
                testInput10.add("st");
                testInput10.add("ststst");
                testInput10.add("sssttt");
                testInput10.add("ssststtt");
                int taskResult10_1 = task2(id, subtask + "_1", testInput10);
                testInput10.clear();
                testInput10.add(" ");
                testInput10.add("ab");
                testInput10.add("ababab");
                testInput10.add("aaabbb");
                testInput10.add("aaababbb");
                int taskResult10_2 = task2(id, subtask + "_2", testInput10);
                testInput10.clear();
                testInput10.add(" ");
                testInput10.add("wy");
                testInput10.add("wywywy");
                testInput10.add("wwwyyy");
                testInput10.add("wwwywyyy");
                int taskResult10_3 = task2(id, subtask + "_3", testInput10);

                if (taskResult10_1 == 1 && taskResult10_2 == 1 && taskResult10_3 == 1) {
                    return 0.5;
                }

                testInput10 = null;

                return 0;

            case 11:    // ((a|b)c?)∗
                ArrayList<String> testInput11 = new ArrayList<>();
                testInput11.add(" ");
                testInput11.add("st");
                testInput11.add("s");
                testInput11.add("t");
                testInput11.add("sr");
                testInput11.add("tr");
                testInput11.add("stssstttsrtrsrsrsrtrtrtr");
                int taskResult11_1 = task2(id, subtask + "_1", testInput11);
                testInput11.clear();
                testInput11.add(" ");
                testInput11.add("ab");
                testInput11.add("a");
                testInput11.add("b");
                testInput11.add("ac");
                testInput11.add("bc");
                testInput11.add("abaaabbbacbcacacacbcbcbc");
                int taskResult11_2 = task2(id, subtask + "_2", testInput11);
                testInput11.clear();
                testInput11.add(" ");
                testInput11.add("wy");
                testInput11.add("w");
                testInput11.add("y");
                testInput11.add("wz");
                testInput11.add("yz");
                testInput11.add("wywwwyyywzyzwzwzwzyzyzyz");

                int taskResult11_3 = task2(id, subtask + "_3", testInput11);

                if (taskResult11_1 == 1 && taskResult11_2 == 1 && taskResult11_3 == 1) {
                    return 1;
                }

                testInput11 = null;

                return 0;
            case 12:    // (s|t)*stt(s|t)*
                ArrayList<String> testInput12 = new ArrayList<>();
                testInput12.add("stt");
                testInput12.add("sstt");
                testInput12.add("tstt");
                testInput12.add("stts");
                testInput12.add("sttt");
                testInput12.add("stststtstst");
                testInput12.add("ssstttsttsssttt");
                int taskResult12_1 = task2(id, subtask + "_1", testInput12);
                testInput12.clear();
                testInput12.add("abb");
                testInput12.add("aabb");
                testInput12.add("babb");
                testInput12.add("abba");
                testInput12.add("abbb");
                testInput12.add("abababbabab");
                testInput12.add("aaabbbabbaaabbb");
                int taskResult12_2 = task2(id, subtask + "_2", testInput12);
                testInput12.clear();
                testInput12.add("wyy");
                testInput12.add("wwyy");
                testInput12.add("ywyy");
                testInput12.add("wyyw");
                testInput12.add("wyyy");
                testInput12.add("wywywyywywy");
                testInput12.add("wwwyyywyywwwyyy");
                int taskResult12_3 = task2(id, subtask + "_3", testInput12);
                testInput12.clear();
                testInput12.add("w");
                int taskResult12_4 = task2(id, subtask + "_3", testInput12);
                testInput12.clear();
                testInput12.add("y");
                int taskResult12_5 = task2(id, subtask + "_3", testInput12);
                testInput12.clear();
                testInput12.add("wy");
                int taskResult12_6 = task2(id, subtask + "_3", testInput12);
                testInput12.clear();
                testInput12.add("wwyywy");
                int taskResult12_7 = task2(id, subtask + "_3", testInput12);
                testInput12.clear();
                testInput12.add("wywwywy");
                int taskResult12_8 = task2(id, subtask + "_3", testInput12);

                // TODO check error
                if (taskResult12_1 == 1 && taskResult12_2 == 1 && taskResult12_3 == 1 && taskResult12_4 == 0 && taskResult12_5 == 0 && taskResult12_6 == 0 && taskResult12_7 == 0 && taskResult12_8 == 0) {
                    return 1;
                }

                testInput12 = null;

                return 0;

            case 13:    // (0|(1(01*(00)*0)*1)*)*
                ArrayList<String> testInput13 = new ArrayList<>();
                testInput13.add("0");
                testInput13.add("11");
                testInput13.add("1001");
                testInput13.add("1011101");
                testInput13.add("1000000001");
                testInput13.add("10111000001");
                testInput13.add("001110011011101100000000110111000001");
                int taskResult13_1 = task2(id, subtask + "_1", testInput13);
                testInput13.clear();
                testInput13.add("a");
                testInput13.add("bb");
                testInput13.add("baab");
                testInput13.add("babbbab");
                testInput13.add("baaaaaaaab");
                testInput13.add("babbbaaaaab");
                int taskResult13_2 = task2(id, subtask + "_2", testInput13);
                testInput13.clear();
                testInput13.add("w");
                testInput13.add("yy");
                testInput13.add("ywwy");
                testInput13.add("ywyyywy");
                testInput13.add("ywwwwwwwwy");
                testInput13.add("ywyyywwwwwy");
                int taskResult13_3 = task2(id, subtask + "_3", testInput13);
                testInput13.clear();
                testInput13.add("1");
                int taskResult13_4 = task2(id, subtask + "_1", testInput13);
                testInput13.clear();
                testInput13.add(" ");
                int taskResult13_5 = task2(id, subtask + "_1", testInput13);

                testInput13 = null;

                if (taskResult13_1 == 1 && taskResult13_2 == 1 && taskResult13_3 == 1 && taskResult13_4 == 0 && taskResult13_5 == 1) {
                    return 1;
                }

                return 0;

            default:
                return 0;

        }
    }

    private int task2(String id, String subtask, ArrayList<String> testStrings) {
        this.logEntry(id + ": TESTING TASK 2 " + subtask + " PUBLIC TEST");
        String testFileName = "task_2_" + subtask + "_public.txt";

        try {
            String path = Paths.get(this.unzipDir + id + "/").toAbsolutePath().toString();

            this.clearOrCreateFile(path + "task_2_" + subtask + "_result.txt");
            this.clearOrCreateFile(path + "task2_" + subtask + "_result.txt");

            List<String> taskPublicInput = this.readTestFile("lab_1/" + testFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 2 SUBTASK " + subtask + ": ON PATH " + path);

            }

            String[] commandArray = {pythonPath, Paths.get(path + "/" + "task_2.py").toAbsolutePath().toString(),
                    "--file", Paths.get(path + "/" + testFileName).toAbsolutePath().toString()};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                String studentResultPath = "";

                if (Files.exists(Paths.get(path + "/task_2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task_2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task_2_" + subtask + "_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_" + subtask + "_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_" + subtask + "_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_" + subtask + "_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task_2_result.txt"))) {
                    System.out.println("FOUND");
                    studentResultPath = Paths.get(path + "/task_2_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_result.txt").toAbsolutePath().toString();
                }
//                studentResult = studentResult.stream().filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());
                boolean successFlag = false;


                if (studentResultPath.isEmpty()) {
                    this.logEntry(id + ": FILE NOT FOUND TESTING TASK 2 PUBLIC TEST " + subtask);
                    return 0;
                }

                studentResultPath = Paths.get(studentResultPath).toAbsolutePath().toString();


                System.out.println("----------------NFA_started " + subtask + " --------------------------");
                System.out.println("Student ID " + id);

                try (Stream<String> stream = Files.lines(Paths.get(studentResultPath))) {

                    stream.forEach(System.out::println);

                } catch (IOException e) {
                    e.printStackTrace();
                }


                NFA nfa = NFA.constructFromFile(studentResultPath);

//                System.out.println(nfa.getNfaStates());
//                System.out.println(nfa.getNFAAlphabet());
//                System.out.println(nfa.getNFAStartState());
//                System.out.println(nfa.getNFAFinalState());
//                System.out.println(nfa.getNFATransitions());

                if (nfa == null || nfa.getNfaStates().isEmpty() || nfa.getNFAAlphabet().isEmpty() || nfa.getNFAStartState().isEmpty() || nfa.getNFAFinalState().isEmpty() || nfa.getNFATransitions().isEmpty()) {
                    this.logEntry(id + ": ERROR NFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    nfa = null;
                    return 0;
                }

//                System.out.println("----------------NFA-End--------------------------");


//                System.out.println("----------------DFA_started--------------------------");

                DFA dfa = DFA.convertNFA(nfa);

//                System.out.println(dfa.getDFAStates());
//                System.out.println(dfa.getAlphbetSymbols());
//                System.out.println(dfa.getStartState());
//                System.out.println(dfa.getAcceptedStates());
//                System.out.println(dfa.getDFATransitions());



                if (dfa == null || dfa.getDFAStates().isEmpty() || dfa.getAlphabetSymbols().isEmpty() || dfa.getStartState().isEmpty() || dfa.getAcceptedStates().isEmpty() || dfa.getDFATransitions().isEmpty()) {
                    this.logEntry(id + ": ERROR DFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    nfa = null;
                    dfa = null;
                    return 0;
                }

//                System.out.println("----------------DFA_started--------------------------");

                String result = "not known";

                for (String testString : testStrings) {
                    String res = dfa.evaluateString(testString, "0");
//                    System.out.println("test string " + testString + "res " + res);
                    if (res.equals("rejected")) {
                        this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                        nfa = null;
                        dfa = null;
                        result = "rejected";

                        return 0;
                    }
                }

                result = "accepted";
                System.out.println(result);
                System.out.println("----------------DFA_result--------------------------");

                this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                nfa = null;
                dfa = null;
                return 1;

            } catch (Exception e) {
                e.printStackTrace();
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK 2 SUBTASK: " + subtask + " PUBLIC TEST");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int task2SubTask7(String id, String subtask, ArrayList<String> testStrings) {
        this.logEntry(id + ": TESTING TASK 2 " + subtask + " PUBLIC TEST");
        String testFileName = "task_2_" + subtask + "_public.txt";

        try {
            String path = Paths.get(this.unzipDir + id + "/").toAbsolutePath().toString();

            this.clearOrCreateFile(path + "task_2_" + subtask + "_result.txt");
            this.clearOrCreateFile(path + "task2_" + subtask + "_result.txt");

            List<String> taskPublicInput = this.readTestFile("lab_1/" + testFileName);

            try {
                if(subtask.equals("7_1")){
                    File file = new File(path + "/" + testFileName);

                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(" ");
                    fileWriter.flush();
                    fileWriter.close();
                } else {
                    Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
                }
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 2 SUBTASK " + subtask + ": ON PATH " + path);

            }

            String[] commandArray = {pythonPath, Paths.get(path + "/" + "task_2.py").toAbsolutePath().toString(),
                    "--file", Paths.get(path + "/" + testFileName).toAbsolutePath().toString()};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                String studentResultPath = "";

                if (Files.exists(Paths.get(path + "/task_2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task_2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_" + subtask + "_public_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_" + subtask + "_public_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task_2_" + subtask + "_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_" + subtask + "_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_" + subtask + "_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_" + subtask + "_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task_2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "/task2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_result.txt").toAbsolutePath().toString();
                }
//                studentResult = studentResult.stream().filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());
                boolean successFlag = false;


                if (studentResultPath.isEmpty()) {
                    this.logEntry(id + ": FILE NOT FOUND TESTING TASK 2 PUBLIC TEST " + subtask);
                    return 0;
                }

                studentResultPath = Paths.get(studentResultPath).toAbsolutePath().toString();

                File resultFile = new File(studentResultPath);

                if (resultFile.length() == 0) {
                    this.logEntry(id + ": FILE IS EMPTY TESTING TASK 2 PUBLIC TEST " + subtask);
                    return 0;
                }

                System.out.println("----------------NFA_started " + subtask + " --------------------------");
                System.out.println("Student ID " + id);

                try (Stream<String> stream = Files.lines(Paths.get(studentResultPath))) {

                    stream.forEach(System.out::println);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                NFA nfa = NFA.constructFromFile(studentResultPath);

//                System.out.println(nfa.getNfaStates());
//                System.out.println(nfa.getNFAAlphabet());
//                System.out.println(nfa.getNFAStartState());
//                System.out.println(nfa.getNFAFinalState());
//                System.out.println(nfa.getNFATransitions());

                if (nfa == null || nfa.getNfaStates().isEmpty() || nfa.getNFAAlphabet().isEmpty() || nfa.getNFAStartState().isEmpty() || nfa.getNFAFinalState().isEmpty() || nfa.getNFATransitions().isEmpty()) {
                    this.logEntry(id + ": ERROR NFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    System.out.println(id + ": ERROR NFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    nfa = null;
                    return 0;
                }

//                System.out.println("----------------NFA-End--------------------------");


//                System.out.println("----------------DFA_started--------------------------");

                DFA dfa = DFA.convertNFA(nfa);

//                System.out.println(dfa.getDFAStates());
//                System.out.println(dfa.getStartState());
//                System.out.println(dfa.getAcceptedStates());
//                System.out.println(dfa.getDFATransitions());

                if (dfa == null || dfa.getDFAStates().isEmpty() || dfa.getStartState().isEmpty() || dfa.getAcceptedStates().isEmpty()) {
                    this.logEntry(id + ": ERROR DFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    System.out.println(id + ": ERROR DFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    nfa = null;
                    dfa = null;
                    return 0;
                }

                System.out.println("----------------DFA_started--------------------------");

                String result = "not known";

                for (String testString : testStrings) {
                    String res = dfa.evaluateString(testString, "0");
                    if (res.equals("rejected")) {
                        this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                        nfa = null;
                        dfa = null;
                        result = "rejected";

                        return 0;
                    }
                }

                result = "accepted";
                System.out.println(result);
                System.out.println("----------------DFA_result--------------------------");

                this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                nfa = null;
                dfa = null;
                return 1;

            } catch (Exception e) {
                e.printStackTrace();
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK 2 SUBTASK: " + subtask + " PUBLIC TEST");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
//        LabTester1 lab = new LabTester1(" /home/ahmed/compilers_submissions/lab_1/", "/home/ahmed/compilers_submissions/logs/", "/usr/bin/python3", "ids.csv", true, 2);

//        lab.test()
//        Class x = Class.forName("Lab1.LabTester1");



    }

}


