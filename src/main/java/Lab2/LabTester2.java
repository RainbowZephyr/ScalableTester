package Lab2;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.ProcessHandler;
import AbstractClasses.TesterBaseClass;
import Lab1.DFA;
import Lab1.NFA;
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
import java.util.stream.Stream;

public class LabTester2 extends TesterBaseClass {

    private String unzipDir;
    private String javaPath;
    public LabTester2(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
                      long timeOut, int threads, String javaPath) {
        super(workingDir, logDir, pythonPath, idsFilePath, cleanBuild, timeOut, threads);

        this.javaPath = javaPath;



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
            this.unzipAndMoveFiles(file, path, id);

            double resultGrade = 0;

//            for (int j = 1; j <= 3; j++) {
//
//                taskResult = task1(id, j + "");
//
//                switch (j) {
//                    case 1:
//                        if (taskResult == 1) {
//                            resultGrade = 1.5;
//                        } else {
//                            resultGrade = 0;
//                        }
//                        break;
//                    case 2:
//                        if (taskResult == 1) {
//                            resultGrade = 1.5;
//                        } else {
//                            resultGrade = 0;
//                        }
//                        break;
//                    case 3:
//                        if (taskResult == 1) {
//                            resultGrade = 2;
//                        } else {
//                            resultGrade = 0;
//                        }
//                        break;
//                }
//
//                subTasksGrade.put("task_1_" + j, resultGrade);
//
//            }

            double taskResult2 = 0;

            for (int j = 1; j <= 11; j++) {



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
            this.moveFiles(path, path, "g4");
            this.moveFiles(path, path, "py");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.logEntry(id + " ERROR CANNOT CREATE FOLDER");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int task1(String id, String subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_2_" + subtask + ".txt";
        String testResultFileName = "task_2_" + subtask + "_result.txt";

        try {
            String path = this.unzipDir + id + "/";
            List<String> taskPublicInput = this.readTestFile("lab_2/" + testFileName);
            List<String> taskPublicResultFile = this.readTestFile("lab_2/" + testResultFileName);
            System.out.println("SDJKSDJKSD "+ taskPublicInput);

            try {
//                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);

                File file = new File(path + "/" + testFileName);
                FileWriter fileWriter = new FileWriter(file);

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
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 1 - SUBTASK: " + subtask + " ON PATH " + path);
            }



            //  java -jar ~\src\main\java\Lab2\antlr-4.7.2-complete.jar Expr.g4 -Dlanguage=Python3

            String antlrFilePath = "";

            if (Files.exists(Paths.get(path + "task_2_1.g4"))) {
                antlrFilePath = "task_2_1.g4";

            } else if (Files.exists(Paths.get(path + "task2_1.g4"))) {
                antlrFilePath = "task2_1.g4";
            }

            if (antlrFilePath.isEmpty()) {
                return  0;
            }

//            java -jar E:\Mo\Workspace\compilers\compiler_lab\Code\compiler_lab_tester\src\main\java\Lab2\antlr-4.7.2-complete.jar Expr.g4 -Dlanguage=Python3


//            String[] commandArray2 = {javaPath, "-jar", "E:\\Mo\\Workspace\\compilers\\doggies_ass2\\antlr-4.7.2-complete.jar", path +  antlrFilePath, "-Dlanguage=Python3" };

            String[] commandArray2 = {"/usr/bin/antlr4", "-Dlanguage=Python3" , path +  antlrFilePath};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray2, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }



            String[] commandArray = {pythonPath, path + "task_2_" + subtask + ".py", "--file", path + "/" + testFileName};
            env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_2_" + subtask + "_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_2_" + subtask + "_result.txt");

                } else if (Files.exists(Paths.get(path + "task1_" + subtask + "_result.txt"))) {
                    studentResult = this.readTextFile(path + "task1_" + subtask + "_result.txt");

                }

//                studentResult = this.readTextFile(Paths.get("C:\\Users\\mo\\Desktop\\task.txt").toAbsolutePath().toString());

                studentResult = studentResult.stream().map(s -> s.replaceAll("\\s+", " ").trim()).filter(l -> !l.trim().isEmpty()).collect(Collectors.toList());

                System.out.println("ID " + id + " studentResult " + studentResult);

                if (!studentResult.containsAll(taskPublicResultFile)) {
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

    private double task2Handler(String id, String subtask) {

        switch (Integer.parseInt(subtask)) {

            case 1: // Single alphabet 'a'
                ArrayList<String> testInput1 = new ArrayList<>();
                testInput1.add("a");
                int taskResult1_1 = task2(id, subtask + "_1", testInput1);
                testInput1.clear();

                testInput1.add("f");
                int taskResult1_2 = task2(id, subtask + "_2", testInput1);
                testInput1.clear();

                testInput1.add("aa");
                int taskResult1_3 = task2(id, subtask + "_1", testInput1);
                testInput1.clear();

                testInput1.add(" ");
                int taskResult1_4 = task2(id, subtask + "_1", testInput1);
                testInput1.clear();

                testInput1 = null;

                if (taskResult1_1 == 1 && taskResult1_2 == 1 && taskResult1_3 == 0 && taskResult1_4 == 0) {
                    return 0.5;
                }

                return 0;
            case 2: // Concatenation '.'

                ArrayList<String> testInput2 = new ArrayList<>();
                testInput2.add("ab");
                int taskResult2_1 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();

                testInput2.add("jl");
                int taskResult2_2 = task2(id, subtask + "_2", testInput2);
                testInput2.clear();

                testInput2.add("a");
                int taskResult2_3 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();

                testInput2.add("b");
                int taskResult2_4 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();

                testInput2.add("aa");
                int taskResult2_5 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();

                testInput2.add("bb");
                int taskResult2_6 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();

                testInput2.add("abb");
                int taskResult2_7 = task2(id, subtask + "_1", testInput2);
                testInput2.clear();

                testInput2 = null;

                if (taskResult2_1 == 1 && taskResult2_2 == 1 && taskResult2_3 == 0 && taskResult2_4 == 0 && taskResult2_5 == 0 && taskResult2_6 == 0 && taskResult2_7 == 0) {
                    return 0.5;
                }

                return 0;
            case 3: // Union 'or'

                ArrayList<String> testInput3 = new ArrayList<>();
                testInput3.add("a");
                testInput3.add("b");
                int taskResult3_1 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();

                testInput3.add("j");
                testInput3.add("o");
                int taskResult3_2 = task2(id, subtask + "_2", testInput3);
                testInput3.clear();

                testInput3.add("aa");
                int taskResult3_3 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();

                testInput3.add("bb");
                int taskResult3_4 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();

                testInput3.add("ab");
                int taskResult3_5 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();

                testInput3.add("ba");
                int taskResult3_6 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();

                testInput3.add("abb");
                int taskResult3_7 = task2(id, subtask + "_1", testInput3);
                testInput3.clear();

                testInput3 = null;

                if (taskResult3_1 == 1 && taskResult3_2 == 1 && taskResult3_3 == 0 && taskResult3_4 == 0 && taskResult3_5 == 0 && taskResult3_6 == 0 && taskResult3_7 == 0) {
                    return 0.5;
                }

                return 0;
            case 4: // Keleen star '*'

                ArrayList<String> testInput4 = new ArrayList<>();
                testInput4.add(" ");
                testInput4.add("a");
                testInput4.add("aa");
                testInput4.add("aaa");
                testInput4.add("aaaaaaaaaaaaaa");
                int taskResult4_1 = task2(id, subtask + "_1", testInput4);
                testInput4.clear();
                testInput4.add(" ");
                testInput4.add("k");
                testInput4.add("kk");
                testInput4.add("kkk");
                testInput4.add("kkkkkkkkkkkkkk");
                int taskResult4_2 = task2(id, subtask + "_2", testInput4);
                testInput4.clear();

                testInput4 = null;

                if (taskResult4_1 == 1 && taskResult4_2 == 1) {
                    return 0.5;
                }

                return 0;
            case 5: // one or more '+'

                ArrayList<String> testInput5 = new ArrayList<>();
                testInput5.add("a");
                testInput5.add("aa");
                testInput5.add("aaa");
                testInput5.add("aaaaaaaaaaaaaaaaa");
                int taskResult5_1 = task2(id, subtask + "_1", testInput5);
                testInput5.clear();
                testInput5.add("h");
                testInput5.add("hh");
                testInput5.add("hhh");
                testInput5.add("hhhhhhhhhhhhhhhhh");
                int taskResult5_2 = task2(id, subtask + "_2", testInput5);
                testInput5.clear();

                testInput5.add(" ");
                int taskResult5_3 = task2(id, subtask + "_1", testInput5);

                testInput5 = null;

                if (taskResult5_1 == 1 && taskResult5_2 == 1 && taskResult5_3 == 0) {
                    return 0.5;
                }

                return 0;
            case 6: // zero or one '?'

                ArrayList<String> testInput6 = new ArrayList<>();
                testInput6.add("a");
                testInput6.add(" ");
                int taskResult6_1 = task2(id, subtask + "_1", testInput6);
                testInput6.clear();
                testInput6.add("g");
                testInput6.add(" ");
                int taskResult6_2 = task2(id, subtask + "_2", testInput6);
                testInput6.clear();

                testInput6.add("aa");
                int taskResult6_3 = task2(id, subtask + "_1", testInput6);

                testInput6 = null;

                if (taskResult6_1 == 1 && taskResult6_2 == 1 && taskResult6_3 == 0) {
                    return 0.5;
                }

                return 0;
            case 7: // EPSILON
                return 0.5;
//                ArrayList<String> testInput7 = new ArrayList<>();
//
//                testInput7.add(" ");
//                int taskResult7_1 = task2SubTask7(id, subtask + "_1", testInput7);
//                int taskResult7_2 = task2SubTask7(id, subtask + "_2", testInput7);
//                int taskResult7_3 = task2SubTask7(id, subtask + "_3", testInput7);
//                int taskResult7_4 = task2SubTask7(id, subtask + "_4", testInput7);
//                int taskResult7_5 = task2SubTask7(id, subtask + "_5", testInput7);
//                int taskResult7_6 = task2SubTask7(id, subtask + "_6", testInput7);
//
//                testInput7 = null;
//
//                if (taskResult7_1 == 1 || taskResult7_2 == 1 || taskResult7_3 == 1 || taskResult7_4 == 1 || taskResult7_5 == 1 || taskResult7_6 == 1) {
//                    return 0.5;
//                }
//
//                return 0;
            case 8: // ((a|b)c?)*

                ArrayList<String> testInput8 = new ArrayList<>();
                testInput8.add(" ");
                testInput8.add("ac");
                testInput8.add("a");
                testInput8.add("b");
                int taskResult8_1 = task2(id, subtask + "_1", testInput8);
                testInput8.clear();
                testInput8.add(" ");
                testInput8.add("yu");
                testInput8.add("y");
                testInput8.add("u");
                int taskResult8_2 = task2(id, subtask + "_2", testInput8);
                testInput8.clear();

                testInput8 = null;

                if (taskResult8_1 == 1 && taskResult8_2 == 1) {
                    return 0.5;
                }

                return 0;
            case 9: // (s|t)*stt(s|t)*

                ArrayList<String> testInput9 = new ArrayList<>();
                testInput9.add("stt");
                testInput9.add("sstt");
                testInput9.add("tstt");
                testInput9.add("stts");
                testInput9.add("sttt");
                testInput9.add("stststtstst");
                testInput9.add("ssstttsttsssttt");
                int taskResult9_1 = task2(id, subtask + "_1", testInput9);
                testInput9.clear();
                testInput9.add("hll");
                testInput9.add("hhll");
                testInput9.add("lhll");
                testInput9.add("hllh");
                testInput9.add("hlll");
                testInput9.add("hlhlhllhlhl");
                testInput9.add("hhhlllhllhhhlll");
                int taskResult9_2 = task2(id, subtask + "_2", testInput9);
                testInput9.clear();

                testInput9.add("s");
                int taskResult9_3 = task2(id, subtask + "_1", testInput9);
                testInput9.clear();
                testInput9.add("t");
                int taskResult9_4 = task2(id, subtask + "_1", testInput9);
                testInput9.clear();
                testInput9.add("st");
                int taskResult9_5 = task2(id, subtask + "_1", testInput9);
                testInput9.clear();
                testInput9.add("sstst");
                int taskResult9_6 = task2(id, subtask + "_1", testInput9);
                testInput9.clear();
                testInput9.add("stsstst");
                int taskResult9_7 = task2(id, subtask + "_1", testInput9);

                if (taskResult9_1 == 1 && taskResult9_2 == 1 && taskResult9_3 == 0 && taskResult9_4 == 0 && taskResult9_5 == 0 && taskResult9_6 == 0 && taskResult9_7 == 0) {
                    return 0.5;
                }

                testInput9 = null;

                return 0;
            case 10: // (0|(1(01*(00)*0)*1)*)*

                ArrayList<String> testInput10 = new ArrayList<>();
                testInput10.add("0");
                testInput10.add("11");
                testInput10.add("1001");
                testInput10.add("1011101");
                testInput10.add("1000000001");
                testInput10.add("10111000001");
                testInput10.add("001110011011101100000000110111000001");
                int taskResult10_1 = task2(id, subtask + "_1", testInput10);
                testInput10.clear();
                testInput10.add("a");
                testInput10.add("bb");
                testInput10.add("baab");
                testInput10.add("babbbab");
                testInput10.add("baaaaaaaab");
                testInput10.add("babbbaaaaab");
                int taskResult10_2 = task2(id, subtask + "_2", testInput10);
                testInput10.clear();

                testInput10.add("1");
                int taskResult10_3 = task2(id, subtask + "_1", testInput10);

                testInput10.clear();
                testInput10.add(" ");
                int taskResult10_4 = task2(id, subtask + "_1", testInput10);

                testInput10 = null;


                if (taskResult10_1 == 1 && taskResult10_2 == 1 && taskResult10_3 == 0 && taskResult10_4 == 1) {
                    return 0.5;
                }

                return 0;

            case 11: // (s|et)*

                ArrayList<String> testInput11 = new ArrayList<>();
                testInput11.add(" ");
                testInput11.add("s");
                testInput11.add("t");
                testInput11.add("st");
                testInput11.add("ss");
                testInput11.add("tt");
                testInput11.add("sssttttsssttsss");
                int taskResult11_1 = task2(id, subtask + "_1", testInput11);
                testInput11.clear();
                testInput11.add(" ");
                testInput11.add("f");
                testInput11.add("h");
                testInput11.add("fh");
                testInput11.add("ff");
                testInput11.add("hh");
                testInput11.add("fffhhhhfffhhfff");
                int taskResult11_2 = task2(id, subtask + "_2", testInput11);
                testInput11.clear();


                testInput11 = null;


                if (taskResult11_1 == 1 && taskResult11_2 == 1) {
                    return 7.5;
                }

                return 0;

            default:
                return 0;

        }
    }

    private int task2(String id, String subtask, ArrayList<String> testStrings) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_2_" + subtask + ".txt";

        try {
            String path = this.unzipDir + id + "/";
            List<String> taskPublicInput = this.readTestFile("lab_2/" + testFileName);

            try {
//                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);


                File file = new File(path + "/" + testFileName);
                FileWriter fileWriter = new FileWriter(file);

                String line = "";

                for (int i = 0; i < taskPublicInput.size(); i++) {
                    line = taskPublicInput.get(i);
                    if (line.startsWith(",")) {
                        line = " " + line;
                    }
                    if (i == taskPublicInput.size()-1) {
                        fileWriter.write(line);
                    } else {
                        fileWriter.write(line + "\n");
                    }
                }

                fileWriter.flush();
                fileWriter.close();



            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 2 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_2_2.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
//                List<String> studentResult = new ArrayList<>();
                String studentResultPath = "";


                if (Files.exists(Paths.get(path + "task_2_2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_2_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "task2_2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_2_result.txt").toAbsolutePath().toString();
                }

                if (studentResultPath.isEmpty()) {
                    this.logEntry(id + ": FILE NOT FOUND TESTING TASK 2 PUBLIC TEST " + subtask);
                    return 0;
                }

                studentResultPath = Paths.get(studentResultPath).toAbsolutePath().toString();

                System.out.println("----------------DFA_started subtask " + subtask + "--------------------------");
                System.out.println("Student ID " + id);

                try (Stream<String> stream = Files.lines(Paths.get(studentResultPath))) {

                    stream.forEach(System.out::println);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                DFA dfa = DFA.constructFromFile(studentResultPath);

//                System.out.println(dfa.getDFAStates());
//                System.out.println(dfa.getAlphabetSymbols());
//                System.out.println(dfa.getStartState());
//                System.out.println(dfa.getAcceptedStates());
//                System.out.println(dfa.getDFATransitions());

                if (dfa.getDFAStates().isEmpty() || dfa.getAlphabetSymbols().isEmpty() || dfa.getStartState().isEmpty() || dfa.getAcceptedStates().isEmpty() || dfa.getDFATransitions().isEmpty()) {
                    this.logEntry(id + ": ERROR DFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    dfa = null;
                    return 0;
                }

//                System.out.println("----------------DFA-End--------------------------");

                String result = "not known";

                for (String testString : testStrings) {
                    result = dfa.evaluateString(testString, "1");
                    System.out.println("test string  " + testString + " result is " + result);
                    if (result.equals("rejected")) {
                        this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                        return 0;
                    }
                }

                result = "accepted";
                System.out.println(result);
                System.out.println("----------------DFA_result--------------------------");

                this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                dfa = null;
                return 1;

            } catch (Exception e) {
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK" + subtask + " PUBLIC TEST");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private int task2SubTask7(String id, String subtask, ArrayList<String> testStrings) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_2_" + subtask + ".txt";




        try {
            String path = this.unzipDir + id + "/";

            List<String> taskPublicInput = this.readTestFile("lab_2/" + testFileName);

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
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 2 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_2_2.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
//                List<String> studentResult = new ArrayList<>();
                String studentResultPath = "";


                if (Files.exists(Paths.get(path + "task_2_2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task_2_2_result.txt").toAbsolutePath().toString();
                } else if (Files.exists(Paths.get(path + "task2_2_result.txt"))) {
                    studentResultPath = Paths.get(path + "/task2_2_result.txt").toAbsolutePath().toString();
                }

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

                System.out.println("----------------DFA_started--------------------------");
                System.out.println("Student ID " + id);

                DFA dfa = DFA.constructFromFile(studentResultPath);

//                System.out.println(dfa.getDFAStates());
//                System.out.println(dfa.getAlphabetSymbols());
//                System.out.println(dfa.getStartState());
//                System.out.println(dfa.getAcceptedStates());
//                System.out.println(dfa.getDFATransitions());

                if (dfa.getDFAStates().isEmpty() || dfa.getStartState().isEmpty() || dfa.getAcceptedStates().isEmpty()) {
                    this.logEntry(id + ": ERROR DFA MISSING INFORMATION TESTING TASK 2 PUBLIC TEST " + subtask);
                    dfa = null;
                    return 0;
                }

                System.out.println("----------------DFA-End--------------------------");

                String result = "not known";

                for (String testString : testStrings) {
                    result = dfa.evaluateString(testString, "1");
                    System.out.println("test string  " + testString + " result is " + result);
                    if (result.equals("rejected")) {
                        this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                        return 0;
                    }
                }

                result = "accepted";
                System.out.println(result);
                System.out.println("----------------DFA_result--------------------------");

                this.logEntry(id + ": FINISHED TESTING TASK 2 PUBLIC TEST " + subtask);
                dfa = null;
                return 1;

            } catch (Exception e) {
                this.logEntry(id + " ERROR CANNOT OPEN OUTPUT FILE FOR TASK" + subtask + " PUBLIC TEST");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }


    public static void main(String[] args) {
        System.out.println("hi");

        NFA nfa = NFA.constructFromFile("C:\\Users\\xXMoXx\\Desktop\\Compiler\\compiler_lab\\Code\\compiler_lab_tester\\src\\main\\resources\\lab_2\\task_2_2_2.txt");

        DFA dfa = DFA.convertNFA(nfa);

        System.out.println(dfa.getDFAStates());
        System.out.println(dfa.getAlphabetSymbols());
        System.out.println(dfa.getStartState());
        System.out.println(dfa.getAcceptedStates());
        System.out.println(dfa.getDFATransitions());

        try {


            System.out.println(dfa.evaluateString("jl", "0"));
            System.out.println(dfa.evaluateString("jll", "0"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}




