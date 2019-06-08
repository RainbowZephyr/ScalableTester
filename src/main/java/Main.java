import AbstractClasses.TesterBaseClass;
import Lab1.DFA;
import Lab1.NFA;
import Lab1.LabTester1;
import Lab2.LabTester2;
import Lab3.LabTester3;
import Lab4.LabTester4;
import Lab5.LabTester5;
import Lab0.LabTesterMoveFiles;
import Lab6.LabTester6;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("logdir", true, "Sets path for logging directory");
        options.addOption("submissiondir", true, "Sets path for submission directory");
        options.addOption("ids", true, "Path to csv file mapping ids to tutorial groups");
        options.addOption("lab", true, "Choose lab test to run");
        options.addOption("clean", false, "Cleans any previous build directories");
        options.addOption("stats", false, "Enables statsistics");
        options.addOption("python", true, "Path to the python executable");
        options.addOption("java", true, "Path to the java executable");

        options.addOption("timeout", true, "Choose lab test to run");
        options.addOption("threads", true, "Choose number of parallel tests to run");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);


        System.out.println("CHAR " + Charset.defaultCharset());

        if (cmd.hasOption("lab") && cmd.hasOption("ids") && cmd.hasOption("submissiondir") && cmd.hasOption("python")) {
            String submissionDir = (String) cmd.getParsedOptionValue("submissiondir");
            String idsFile = (String) cmd.getParsedOptionValue("ids");
            String loggingDir = "";
            String pythonPath = (String) cmd.getParsedOptionValue("python");
            String javaPath = "";

            int labNumber = Integer.parseInt((String) cmd.getParsedOptionValue("lab"));
            long timeout = 2;
            boolean clean = false;
            boolean stats = false;
            int threads = Runtime.getRuntime().availableProcessors();

            if (cmd.hasOption("logdir")) {
                loggingDir = (String) cmd.getParsedOptionValue("logdir");
            }

            if (cmd.hasOption("clean")) {
                clean = true;
            }

            if (cmd.hasOption("threads")) {
                threads = Integer.parseInt((String) cmd.getParsedOptionValue("threads"));
            }

            if (cmd.hasOption("stats")) {
                stats = true;
            }

            if (cmd.hasOption("timeout")) {
                timeout = Long.parseLong((String) cmd.getParsedOptionValue("timeout"));

            }


            File file = new File(idsFile);
            if (!file.exists() || !file.isFile()) {
                System.err.println("IDS FILE MUST EXIST");
                return;
            }

            file = new File(submissionDir);

            if (!file.exists() && !file.isDirectory()) {
                System.out.println(file.getAbsolutePath());
                System.err.println("SUBMISSION DIRECTORY MUST EXIST");
                return;
            }

            file = new File(pythonPath);

            if (!file.exists() || file.isDirectory()) {
                System.out.println(file.getAbsolutePath());
                System.err.println("PYTHON EXECUTABLE MUST EXIST");
                return;
            }

            if (cmd.hasOption("java")) {
                javaPath = (String) cmd.getParsedOptionValue("java");
                file = new File(javaPath);

                if (!file.exists() || file.isDirectory()) {
                    System.out.println(file.getAbsolutePath());
                    System.err.println("JAVA EXECUTABLE MUST EXIST");
                    return;
                }


            }

            file = null; // Allows for faster garbage collection


            switch (labNumber) {
                case 0:
                    TesterBaseClass lab0 = new LabTesterMoveFiles(submissionDir, loggingDir, pythonPath, idsFile, clean,
                            timeout
                            , threads);
                    lab0.run();
                    lab0.generateGradesPerTutorial(stats);
                    break;
                case 1:
                    TesterBaseClass lab1 = new LabTester1(submissionDir, loggingDir, pythonPath, idsFile, clean, timeout
                            , threads);
                    lab1.run();
                    lab1.generateGradesPerTutorial(stats);
                    break;
                case 2:
                    TesterBaseClass lab2 = new LabTester2(submissionDir, loggingDir, pythonPath, idsFile, clean, timeout
                            , threads, javaPath);
                    lab2.run();
                    lab2.generateGradesPerTutorial(stats);
                    break;
                case 3:
                    TesterBaseClass lab3 = new LabTester3(submissionDir, loggingDir, pythonPath, idsFile, clean,
                            timeout
                            , threads);
                    lab3.run();
                    lab3.generateGradesPerTutorial(stats);
                    break;
                case 4:
                    TesterBaseClass lab4 = new LabTester4(submissionDir, loggingDir, pythonPath, idsFile, clean, timeout
                            , threads);
                    lab4.run();
                    lab4.generateGradesPerTutorial(stats);
                    break;
                case 5:
                    TesterBaseClass lab5 = new LabTester5(submissionDir, loggingDir, pythonPath, idsFile, clean,
                            timeout
                            , threads);
                    lab5.run();
                    lab5.generateGradesPerTutorial(stats);
                    break;
                case 6:
                    TesterBaseClass lab6 = new LabTester6(submissionDir, loggingDir, pythonPath, idsFile, clean,
                            timeout
                            , threads);
                    lab6.run();
                    lab6.generateGradesPerTutorial(stats);
                    break;
                default:
                    System.err.println("LAB TEST NOT YET IMPLEMENTED");
            }

        } else {
            System.err.println("ALL OPTIONS WITH THE EXCEPTION OF LOGDIR MUST BE DEFINED");
        }

    }


    static String Test2(String path, String expression) throws Exception {
        NFA nfa = NFA.constructFromFile(path);
        DFA dfa = DFA.convertNFA(nfa);
        return dfa.evaluateString(expression, "0");


    }

//	static void Test3() {
//
//		String postRegex = Lab1.RegExInfToPostConverter.infixToPostfix("(a|b|ab)");
//
//		System.out.println(postRegex);
//
//		Lab1.ExpressionContainer ecResult = Lab1.RegexPostToNFA.constructNFAFromRegex(postRegex);
//
////		System.out.println(ecResult.exp);
////		System.out.println(ecResult.startState);
////		System.out.println(ecResult.finalState);
////		System.out.println(ecResult.allSymbols);
////		System.out.println(ecResult.states);
////		System.out.println(ecResult.transitions);
//
//		NFAReader nfaInput = new NFAReader(ecResult);
//
//		System.out.println(nfaInput.getNfaStates());
//		System.out.println(nfaInput.getNfaAlpha());
//		System.out.println(nfaInput.getNfaStartState());
//		System.out.println(nfaInput.getNfaFinalState());
//		System.out.println(nfaInput.getNfaTransition());
//
//		Lab1.DFA ttable = new Lab1.DFA();
//		ttable.convertNFA(nfaInput);
//
//		System.out.println(ttable.getNfaStates());
//		System.out.println(ttable.getDFAStates());
//		System.out.println(ttable.getAlphbetSymbols());
//		System.out.println(ttable.getAlphabetStates());
//		System.out.println(ttable.getAcceptedStates());
//
//		String input = "aaab";
//
//		System.out.println(ttable.evaluateString(input));
//
//	}

}
