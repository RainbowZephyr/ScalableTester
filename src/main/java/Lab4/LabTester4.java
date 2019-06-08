package Lab4;

import AbstractClasses.ArchiveExtractor;
import AbstractClasses.Grammar;
import AbstractClasses.ProcessHandler;
import AbstractClasses.TesterBaseClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;


public class LabTester4 extends TesterBaseClass {
    private String unzipDir;
    private HashMap<Integer, Integer> leftFactoringTestStrings;

    public LabTester4(String workingDir, String logDir, String pythonPath, String idsFilePath, boolean cleanBuild,
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

        this.leftFactoringTestStrings = new HashMap<>();
        this.leftFactoringTestStrings.put(1, 5);
        this.leftFactoringTestStrings.put(2, 3);
        this.leftFactoringTestStrings.put(3, 3);
        this.leftFactoringTestStrings.put(4, 6);
        this.leftFactoringTestStrings.put(5, 11);
        this.leftFactoringTestStrings.put(6, 4);
        this.leftFactoringTestStrings.put(7, 0);

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

            for (int i = 1; i <= 3; i++) {
                resultGrade = this.leftRecursionSubTaskN(id, i);
                subTasksGrade.put("Task_4_1_" + i, resultGrade);

            }

            resultGrade = this.leftFactoringSubTaskN(id, 1);
            if (resultGrade == 1) {
                subTasksGrade.put("Task_4_2_" + 1, 0.5);
            } else {
                subTasksGrade.put("Task_4_2_" + 1, resultGrade);

            }


            resultGrade = this.leftFactoringSubTaskN(id, 5);
            if (resultGrade == 11) {
                subTasksGrade.put("Task_4_2_" + 5, 0.25);
            } else {
                subTasksGrade.put("Task_4_2_" + 5, 0.0);

            }

            resultGrade = this.leftFactoringSubTask7(id, 7);
            if (resultGrade == 1) {
                subTasksGrade.put("Task_4_2_" + 7, 0.25);
            } else {
                subTasksGrade.put("Task_4_2_" + 7, resultGrade);

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

    public static boolean has_lcp(List<List<String>> rules) {
        List<String> lcp = new ArrayList<String>();

        for (int i = 0; i < rules.size(); i++) {
            String startSymbol = rules.get(i).get(0);
            for (int j = i + 1; j < rules.size(); j++) {
                String startSymbolNext = rules.get(j).get(0);
                if (startSymbol.equals(startSymbolNext)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static HashMap<String, List<List<String>>> reconstructGrammarRec(HashMap<String, List<List<String>>> originalGrammar, HashMap<String, List<List<String>>> newGrammar) {
        List<String> oldVariables = new ArrayList<String>(originalGrammar.keySet());
        List<String> allVariables = new ArrayList<String>(newGrammar.keySet());
        for (String var : oldVariables) {
            List<List<String>> newRules = newGrammar.get(var);
            List<List<String>> replaceRules = new ArrayList<List<String>>();
            int temp = newRules.get(0).size();
            String lastItem = newRules.get(0).get(temp - 1);
            boolean replace = false;
            if (!oldVariables.contains(lastItem) && allVariables.contains(lastItem)) {
                replace = true;

                for (List<String> newRule : newRules) {
                    replaceRules.add(newRule.subList(0, newRule.size() - 1));
                }

                List<List<String>> newVarRules = newGrammar.get(lastItem);

                for (List<String> newRule : newVarRules) {
                    if (newRule.get(0).equals("epsilon")) {
                        continue;
                    }

                    List<String> newRule2 = new ArrayList<String>();
                    newRule2.add(var);
                    newRule2.addAll(newRule.subList(0, newRule.size() - 1));
                    replaceRules.add(newRule2);
                }

            }

            if (replace) {
                newGrammar.put(var, replaceRules);
            }
        }

        for (String var : allVariables) {
            if (!oldVariables.contains(var)) {
                newGrammar.remove(var);
            }
        }


        return newGrammar;
    }


    public static boolean lcp_eps(HashMap<String, List<List<String>>> grammar) {
        Iterator it = grammar.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            List<List<String>> currentRules = (List<List<String>>) pair.getValue();
            if (has_lcp(currentRules)) {
                return false;
            }
            //it.remove(); // avoids a ConcurrentModificationException
        }
        return true;
    }

    //////////////////////////////////

    public static boolean equalGrammar(HashMap<String, List<List<String>>> originalGrammar, HashMap<String, List<List<String>>> newGrammar) {
        List<String> oldVariables = new ArrayList<String>(originalGrammar.keySet());
        List<String> allVariables = new ArrayList<String>(newGrammar.keySet());

        for (String var : oldVariables) {
            if (!allVariables.contains(var)) {
                return false;
            }
            List<List<String>> oldRules = originalGrammar.get(var);
            List<List<String>> newRules = newGrammar.get(var);

            List<String> oldRulesStr = new ArrayList<String>();
            List<String> newRulesStr = new ArrayList<String>();

            for (List<String> rule : oldRules) {
                oldRulesStr.add(String.join(" ", rule));
            }

            for (List<String> rule : newRules) {
                newRulesStr.add(String.join(" ", rule));
            }

            for (String rule : oldRulesStr) {
                if (!newRulesStr.contains(rule)) {
                    return false;
                }
            }
        }

        return true;
    }

    private int leftRecursionSubTaskN(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_4_1_" + subtask + ".txt";
        String testResultFileName = "task_4_1_" + subtask + "_result.txt";


        try {
            String path = this.unzipDir + id + "/";

            this.clearOrCreateFile(path + "task_4_1_result.txt");


            List<String> taskPublicInput = this.readTestFile("lab_4/" + testFileName);
            List<String> taskPublicResultFile = this.readTestFile("lab_4/" + testResultFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            try {
                Files.write(Paths.get(path + "/" + testResultFileName), taskPublicResultFile);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC RESULT TEST FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_4_1.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }


            TreeMap<String, HashSet<List<String>>> studentGrammar;


            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_4_1_result.txt"))) {
                    try {
                        studentResult = this.readTextFile(path + "task_4_1_result.txt");
                        studentGrammar = Grammar.readSortedGrammar(studentResult);

                    } catch (Exception e) {
                        System.err.println("CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                        this.logEntry("ERROR: CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                        return 0;
                    }


                } else {
                    return 0;
                }


                int result;
                TreeMap<String, HashSet<List<String>>> ourGrammar = Grammar.readSortedGrammar(taskPublicResultFile);


                if (ourGrammar.equals(studentGrammar)) {
                    this.logEntry(id + ": FINISHED TESTING TASK 4 PUBLIC TEST " + subtask);
                    System.err.println("GRAMMARS EQUAL " + id + " " + subtask);
                    result = 1;
                } else {
                    System.err.println("GRAMMARS NOT EQUAL " + id + " " + subtask);

                    System.err.println("OUR GRAMMAR " + ourGrammar);
                    System.err.println("STUDENT GRAMMAR " + studentGrammar);
                    result = 0;
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

    private int leftFactoringSubTaskN(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_4_2_" + subtask + ".txt";
        String testResultFileName = "task_4_2_result.txt";
        System.out.println("READING TEST FILE " + testFileName);

        try {
            String path = this.unzipDir + id + "/";

            this.clearOrCreateFile(path + testResultFileName);


            List<String> taskPublicInput = this.readTestFile("lab_4/" + testFileName);
//            List<String> taskPublicResultFile = this.readTestFile("lab_4/" + testResultFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_4_2.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_4_2_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_4_2_result.txt");

                } else {
                    return 0;
                }

                if (studentResult.size() < taskPublicInput.size()) {
                    this.logEntry(id + ": ERROR IN COMPLETE GRAMMAR FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
                    return 0;

                }


                LinkedHashMap<String, List<List<String>>> studentGrammar;

                try {
                    studentGrammar = Grammar.readGrammar(studentResult);

                } catch (Exception e) {
                    System.err.println("CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                    this.logEntry("ERROR: CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                    return 0;
                }

                FirstAndFollow studentFirstAndFollow = new FirstAndFollow(id, testFileName);
                studentFirstAndFollow.parseGrammar(studentGrammar);

                ParseTable studentParseTable = new ParseTable(studentFirstAndFollow);
                boolean validLL1 = studentParseTable.generateTable();

                if (!validLL1) {
                    return 0;
                }

                Parser studentParser = new Parser(studentParseTable);
                int result = 0;
                List<String> inputString;
                for (int i = 1; i <= this.leftFactoringTestStrings.get(subtask); i++) {
                    this.logEntry(id + ": TESTING TASK 4 LEFT FACTORING PUBLIC TEST " + subtask + " WITH STRING " + i);

                    try {
                        inputString = this.readTestFile("lab_4/task_4_2_" + subtask + "_string_" + i + ".txt");
                        List<String> formattedInputString = Arrays.stream(inputString.get(0).split(" ")).map(String::trim).collect(Collectors.toCollection(ArrayList::new));
                        if (studentParser.parseInput(formattedInputString)) {
                            result++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.logEntry(id + ": FINISHED TESTING TASK 4 LEFT FACTORING PUBLIC TEST " + subtask + " WITH STRING " + i);


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

    private int leftFactoringSubTask2(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_4_2_" + subtask + ".txt";
        String testResultFileName = "task_4_2_result.txt";
        System.out.println("READING TEST FILE " + testFileName);

        try {
            String path = this.unzipDir + id + "/";

            this.clearOrCreateFile(path + testResultFileName);


            List<String> taskPublicInput = this.readTestFile("lab_4/" + testFileName);
//            List<String> taskPublicResultFile = this.readTestFile("lab_4/" + testResultFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_4_2.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            try {
                List<String> studentResult = new ArrayList<>();

                if (Files.exists(Paths.get(path + "task_4_2_result.txt"))) {
                    studentResult = this.readTextFile(path + "task_4_2_result.txt");

                } else {
                    return 0;
                }

                if (studentResult.size() < taskPublicInput.size()) {
                    this.logEntry(id + ": ERROR IN COMPLETE GRAMMAR FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
                    return 0;

                }

                LinkedHashMap<String, List<List<String>>> studentGrammar;

                try {
                    studentGrammar = Grammar.readGrammar(studentResult);

                } catch (Exception e) {
                    System.err.println("CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                    this.logEntry("ERROR: CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                    return 0;
                }
                HashSet<String> studentBody = new HashSet<>();

                for (List<String> list : studentGrammar.get("S")) {
                    for (String element : list) {
                        if (studentBody.contains(element)) {
                            return 0;
                        } else {
                            studentBody.add(element);
                        }
                    }
                }

                HashSet<String> body = new HashSet<>();

                for (List<String> list : studentGrammar.get("S")) {
                    for (String element : list) {

                        body.add(element);
                    }
                }

                if (body.equals(studentBody)) {
                    return 1;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }


    private int leftFactoringSubTask7(String id, int subtask) {
        this.logEntry(id + ": TESTING TASK " + subtask + " PUBLIC TEST");
        String testFileName = "task_4_2_" + subtask + ".txt";
        String testResultFileName = "task_4_2_7_result.txt";
        System.out.println("READING TEST FILE " + testFileName);

        try {
            String path = this.unzipDir + id + "/";

            this.clearOrCreateFile(path + testResultFileName);


            List<String> taskPublicInput = this.readTestFile("lab_4/" + testFileName);
            List<String> taskPublicResultFile = this.readTestFile("lab_4/" + testResultFileName);

            try {
                Files.write(Paths.get(path + "/" + testFileName), taskPublicInput);
            } catch (Exception e) {
                this.logEntry(id + ": ERROR CANNOT WRTIE PUBLIC TEST FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
            }

            String[] commandArray = {pythonPath, path + "task_4_2.py", "--file", path + "/" + testFileName};
            Map<String, String> env = new HashMap<String, String>();
            env.put("PYTHONPATH", path);

            ProcessHandler processHandler = new ProcessHandler(commandArray, this.getTimeOut(), TimeUnit.MINUTES, path, env);

            boolean safeExit = processHandler.spawn();

            if (!safeExit) {
                return 0;
            }

            List<String> studentResult = new ArrayList<>();

            if (Files.exists(Paths.get(path + "task_4_2_result.txt"))) {
                studentResult = this.readTextFile(path + "task_4_2_result.txt");

            } else {
                return 0;
            }

            if (studentResult.size() < taskPublicInput.size()) {
                this.logEntry(id + ": ERROR IN COMPLETE GRAMMAR FOR TASK 4 - SUBTASK: " + subtask + " ON PATH " + path);
                return 0;

            }

            TreeMap<String, HashSet<List<String>>> studentGrammar;

            try {
                studentGrammar = Grammar.readSortedGrammar(studentResult);

            } catch (Exception e) {
                System.err.println("CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                this.logEntry("ERROR: CANNOT READ STUDENT " + id + " FOR LEFT FACTORING TASK " + subtask);
                return 0;
            }

            TreeMap<String, HashSet<List<String>>> ourGrammar = Grammar.readSortedGrammar(taskPublicResultFile);

            if (ourGrammar.equals(studentGrammar)) {
                return 1;
            } else {

                return 0;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }

    public static HashMap<String, List<List<String>>> reconstructGrammar(HashMap<String, List<List<String>>> originalGrammar, HashMap<String, List<List<String>>> newGrammar) {
        List<String> oldVariables = new ArrayList<String>(originalGrammar.keySet());
        List<String> allVariables = new ArrayList<String>(newGrammar.keySet());
        for (String var : oldVariables) {
            List<List<String>> newRules = newGrammar.get(var);
            int i = 0;
            while (i < newRules.size()) {
                List<String> rule = newRules.get(i);
                boolean replace = false;
                String item = rule.get(rule.size() - 1);
                if (allVariables.contains(item) && !(oldVariables.contains(item))) {
                    replace = true;
                }
                if (replace) {
                    newRules.remove(i);


                    List<List<String>> rulesToReplace = newGrammar.get(item);
                    // System.out.println(item);
                    // System.out.println(rulesToReplace);
                    for (List<String> newRuleToReplace : rulesToReplace) {
                        List<String> newList = new ArrayList<String>();
                        for (int k = 0; k < rule.size() - 1; k++) {
                            newList.add(rule.get(k));
                        }
                        for (int k = 0; k < newRuleToReplace.size(); k++) {
                            if (newRuleToReplace.get(k).equals("epsilon")) {
                                continue;
                            }
                            newList.add(newRuleToReplace.get(k));
                        }
                        newRules.add(i, newList);
                    }
                    //System.out.println(newRules);
                    newGrammar.put(var, newRules);
                }
                if (!replace) {
                    i++;
                }
            }
        }

        for (String var : allVariables) {
            if (!oldVariables.contains(var)) {
                newGrammar.remove(var);
            }
        }


        return newGrammar;
    }


    public static String longestPrefix(ArrayList<String> rules) {
        HashMap<String, ArrayList<String>> prefixes = new HashMap<>();
        HashMap<ArrayList<String>, ArrayList<String>> invertedTable;
        String substring;

        for (int i = 0; i < rules.size(); i++) {

            for (int j = 1; j <= rules.get(i).length(); j++) {

                substring = rules.get(i).substring(0, j);

                for (int k = 0; k < rules.size(); k++) {
                    if (rules.get(k).startsWith(substring)) {
                        addToHashMap(prefixes, substring, rules.get(k));
                    }
                }
            }
        }
        prefixes = (HashMap) prefixes.entrySet().stream().filter(e -> e.getValue().size() != 1).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        invertedTable = (HashMap) invertMap(prefixes); //find rules of longest prefix

        Optional<ArrayList<String>> longestPrefix = invertedTable.keySet().stream().max((entry1, entry2) -> entry1.size() > entry2.size() ? 1 : -1);
        if (longestPrefix.isPresent()) {
            return invertedTable.get(longestPrefix.get()).stream().max((entry1, entry2) -> entry1.length() > entry2.length() ? 1 : -1).get();
        } else {
            return "";
        }
    }

    public static void addToHashMap(AbstractMap<String, ArrayList<String>> table, String key, String value) {
        if (table.containsKey(key)) {
            if (!table.get(key).contains(value)) {
                table.get(key).add(value);
            }
        } else {
            table.put(key, new ArrayList<String>() {{
                add(value);
            }});
        }
    }

    public static AbstractMap<ArrayList<String>, ArrayList<String>> invertMap(AbstractMap<String, ArrayList<String>> table) {
        AbstractMap<ArrayList<String>, ArrayList<String>> invertedTable = new HashMap<>();

        table.forEach((value, key) -> {
            if (invertedTable.containsKey(key)) {
                if (!invertedTable.get(key).contains(value)) {
                    invertedTable.get(key).add(value);
                }
            } else {
                invertedTable.put(key, new ArrayList<String>() {{
                    add(value);
                }});
            }

        });
        return invertedTable;
    }


    public static void main(String[] args) throws Exception {

        String[] lines = {"S : a | b | c | d | S | epsilon"};
        // String[] lines = {"S : S S | S S a"};
        LinkedHashMap<String, List<List<String>>> grammar = Grammar.readGrammar(Arrays.asList(lines));

        String[] newlines = {"S : a | b | c | d | S | epsilon", "S' : "};
        // String[] newlines = {"S' : a | epsilon", "S : S S S'"};
        LinkedHashMap<String, List<List<String>>> newGrammar = Grammar.readGrammar(Arrays.asList(newlines));

        System.out.println("The original grammar has lcp = epsilon? " + lcp_eps(grammar));
        System.out.println("The new grammar has lcp = epsilon? " + lcp_eps(newGrammar));

        HashMap<String, List<List<String>>> recGrammar = reconstructGrammar(grammar, newGrammar);

        boolean isEqualGrammar = equalGrammar(grammar, recGrammar);

        System.out.println("The original grammar and the new one are equal? " + isEqualGrammar);

        Iterator it = recGrammar.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }


        // String[] newlines = {"S : S S' | ( S ) | a", "S' : + S | S | *", "A : A A' | b d | epsilon", "A' : c | a d"};
//        String[] newlines = {"S : ( S ) S' | a A S'", "S' : + S S' | * S' | epsilon", "A : b d A' | A'", "A' : c A' | a d A' | epsilon"};
//        String[] newlines = {"S : x y | S z S | ( x ) | x S S | S S y | ( x A ) | ( x y ) A | epsilon", "A : a b A | a a A | a b A c | b c | a | b"};

        String[] newlines1 = {
        "S : A",
        "A : B d A'",
        "A' : C | d",
        "B : a | epsilon",
        "C : D | b",
        "D : c C"

        };


//        List<String> lines1 = Files.readAllLines(Paths.get("/home/ahmed-hesham/compilers_submissions/lab_4/T8/build/34_1410/task_4_1_result.txt"));
//        lines1 = lines1.stream().map(String::trim).filter(v -> !v.isEmpty()).collect(toCollection(ArrayList::new));

//        lines = lines.stream().filter(v -> !v.trim().isEmpty()).collect(toCollection(ArrayList::new));

//        List<String> ourlines = Files.readAllLines(Paths.get("/home/ahmed-hesham/compilers_submissions/lab_4/T8/build/34_1410/task_4_1_1_result.txt"));
//        ourlines = ourlines.stream().map(String::trim).filter(v -> !v.isEmpty()).collect(toCollection(ArrayList::new));


        LinkedHashMap<String, List<List<String>>> g = Grammar.readGrammar(Arrays.asList(newlines1));
//        TreeMap<String, HashSet<List<String>>> og = Grammar.readSortedGrammar(ourlines);


//        System.out.println(sg);
//        System.out.println(og);
//        System.out.println(sg.equals(og));

//        LinkedHashMap<String, List<List<String>>> gl = Grammar.readGrammar(Arrays.asList(newlines1));
//        for (int i = 1; i <= 10000; i++) {
//            System.out.println(Grammar.generateStrings(gl, 10000));
//
//        }
//
        FirstAndFollow ff = new FirstAndFollow("1", "1");

        ff.parseGrammar(g);
        System.out.println("FF" + ff.first + "\n" + ff.follow);
        ParseTable pt = new ParseTable(ff);
        System.out.println("SUCCESSFUL TABLE? " + pt.generateTable());
//        pt.printTable(new HashSet<String>(ff.terminals));

        for (Map.Entry<String, TreeMap<String, String>> entry : pt.table.entrySet()) {
            System.out.println(entry);

        }

        String[] input = {"a", "b", "c", "w", "y", "$"};

        String[] input1 = {"d c c c c c b $",
                "a d c c c c c b $",
                "d c c c c c c c c c c c c c c b $",
                "d b $",
                "d d $"
        };

        String[] input5 = {"a b c w y $",
                "a b c w $",
                "a b c $",
                "a b $",
                "a $",
                "x y z $",
                "x y $",
                "x $",
                "k m n $",
                "m o $",
                "$"
        };

//
        Parser p = new Parser(pt);
        for (String in : input1) {
            System.out.println("INPUT " + in + " " + p.parseInput(in.split(" ")));

        }


//        for (int i = 0; i < 3000; i++) {
//
//            System.out.println(Grammar.generateStrings(g, 9000000));
//        }

    }


}
