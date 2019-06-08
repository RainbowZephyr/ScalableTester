package Lab1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DFA {

    private static int currentState;
    private ArrayList<String> dfaStates;
    private ArrayList<ArrayList<String>> alphabetStates;
    private ArrayList<String> alphabetSymbols;
    private ArrayList<String> acceptedStates;
    private ArrayList<String> dfaTransitions;
    private String startState;

    public DFA() {
        this.dfaStates = new ArrayList<String>();
        this.alphabetStates = new ArrayList<ArrayList<String>>();
        this.alphabetSymbols = new ArrayList<String>();
        this.acceptedStates = new ArrayList<String>();
        this.dfaTransitions = new ArrayList<String>();
        this.startState = "";
        currentState = 0;
    }

    public DFA(ArrayList<String> states, String startState, ArrayList<ArrayList<String>> alphabetStates, ArrayList<String> alphabetSymbols, ArrayList<String> acceptedStates) {
        this.dfaStates = states;
        this.startState = startState;
        this.alphabetStates = alphabetStates;
        this.alphabetSymbols = alphabetSymbols;
        this.acceptedStates = acceptedStates;
        currentState = 0;

    }


    public static DFA constructFromFile(String path) {
        try {
            currentState = 0;

            DFA dfa = new DFA();
            List<String> lines;
            HashSet<String> tmpHash = new HashSet<>();

            lines = Files.readAllLines(Paths.get(path));

            if (lines.size() < 5) {
                return null;
            }

            String reg1 = "[\\[\\]\\(\\)\\'\\\"{}:]+";

            ArrayList<String> line = Arrays.stream(lines.get(0).split(",")).map(e -> {
                return e.replaceAll(reg1, "").trim();
            }).collect(Collectors.toCollection(ArrayList<String>::new));

            tmpHash.addAll(line);

            dfa.setDFAStates(new ArrayList<>(tmpHash));
            tmpHash.clear();

            line = Arrays.stream(lines.get(1).split(",")).map(e -> {
                return e.replaceAll(reg1, "").trim();
            }).collect(Collectors.toCollection(ArrayList<String>::new));

            tmpHash.addAll(line);

            dfa.setAlphabetSymbols(new ArrayList<>(tmpHash));
            tmpHash.clear();

            dfa.setDFAStartState(lines.get(2).replaceAll(reg1, "").trim());

            line = Arrays.stream(lines.get(3).split(",")).map(e -> {
                return e.replaceAll(reg1, "").trim();
            }).collect(Collectors.toCollection(ArrayList<String>::new));

            tmpHash.addAll(line);

            ArrayList<String> tmp = new ArrayList<>();

            for (String state : dfa.getDFAStates()
            ) {
                if (tmpHash.contains(state)) {
                    tmp.add("accepted");
                } else {
                    tmp.add("rejected");
                }
            }

            dfa.setDFAFinalStates(tmp);
            tmpHash.clear();

            formatTransitions(dfa, lines.get(4));
            return dfa;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void formatTransitions(DFA dfa, String transistions) {
        String reg1 = "\\(([^()])*\\)";
        String reg2 = "[\\[\\]\\'\\\"]+";
        String reg3 = "[\\s\\.\\w\\#\\\\\\/\\'\\\"\\;]+(?=,)|(?<=,)[\\s\\.\\w\\#\\\\\\/\\'\\\"\\;]+"; // comma


        transistions = transistions.replaceAll(reg2, "");

        ArrayList<String> matchList = new ArrayList<>();
        Pattern regex = Pattern.compile(reg1);
        Matcher matcher = regex.matcher(transistions);

        while (matcher.find()) {
            matchList.add(matcher.group());
        }

        HashSet<String> matchListResult = new HashSet<String>();

        regex = Pattern.compile(reg3);
        String tmp = "";
        String tmp2 = "";
        int count = 0;
        HashSet<String> tmpExtra = new HashSet<>();

        for (String str : matchList) {
            tmp = "";
            count = 0;
            tmpExtra.clear();
            str = str.replaceAll("[\uD835\uDF00εϵ\uD835\uDF3A\uD835\uDFAE]+", " ");
            matcher = regex.matcher(str);
            while (matcher.find()) {
                tmp2 = matcher.group();
                tmp2 = tmp2.replaceAll("\\s+", "").trim();
                if (tmp2.isEmpty()) {
                    tmp2 = " ";
                }

                if (count > 1) {
                    tmpExtra.add(tmp2.trim());
                } else {
                    tmp += tmp2 + ",";
                }

                count++;
            }


            for (String s : tmpExtra
            ) {
                matchListResult.add(tmp + s);
            }

        }

        for (String str : matchListResult) {
            dfa.getDFATransitions().add(str);
        }

        matchList = null;
        matchListResult = null;
        tmpExtra = null;
    }

    public static DFA convertNFA(NFA nfa) {
        DFA dfa = new DFA();

        ArrayList<List<String>> nfaStates = new ArrayList<>();
        ArrayList<String> nfaAlphabet = nfa.getNFAAlphabet();

        nfaAlphabet.remove("");

        dfa.getAlphabetSymbols().addAll(nfaAlphabet);
        dfa.getAlphabetSymbols().remove(" ");


        // Adding start state
        dfa.getDFAStates().add(currentState + "");
        dfa.setDFAStartState(dfa.getDFAStates().get(currentState));
        currentState++;

        List<String> states = new ArrayList<String>();
        states.add(nfa.getNFAStartState());
        nfaStates.add(getEpsilonClosure(nfa, states));

        createTransitionTable(dfa, nfa, nfaStates);

        addAcceptedStates(dfa, nfa, nfaStates);

        addDFATransitions(dfa);

        return dfa;
    }

    public String evaluateString(String exp, String flag) throws Exception {
        String result = "rejected";

        if (flag.equals("0")) {
            result = evaluateStringTable(exp);
        }

        if (flag.equals("1")) {
            result = evaluateStringTransitions(exp);

        }

        return result;
    }

    private String evaluateStringTable(String exp) throws Exception {
        String result = "rejected";

        String state = this.getStartState();
        int indexSym = -1;
        int indexState = 0;
        String ch = "";

        for (int i = 0; i < exp.length(); i++) {
            ch = exp.charAt(i) + "";

            if (ch.equals(" ")) {
                continue;
            }

            indexSym = this.alphabetSymbols.indexOf(ch);

            if (indexSym == -1) {
                throw new Exception("Alphabet unknown: `" + ch + "`");
//                return "rejected";
            }

            state = this.alphabetStates.get(indexState).get(indexSym);

            if (state.isEmpty()) {
                throw new Exception("State not found: `" + state + "`");
//                return "rejected";
            }

            indexState = dfaStates.indexOf(state);
        }

        result = acceptedStates.get(indexState);

        return result;
    }

    private String evaluateStringTransitions(String exp) throws Exception {
        String result = "rejected";

        String state = this.getStartState();
        String ch = "";

        for (int i = 0; i < exp.length(); i++) {
            ch = exp.charAt(i) + "";

            if (ch.equals(" ")) {
                continue;
            }

            state = findStatesOnSymbol(this, ch, state);

            if (state == null) {
                return result;
            }
        }

        result = acceptedStates.get(dfaStates.indexOf(state));

        return result;
    }

    private static void createTransitionTable(DFA dfa, NFA nfa, ArrayList<List<String>> nfaStates) {

        for (int i = 0; i < nfaStates.size(); i++) {
            ArrayList<String> alphabet = new ArrayList<String>();

            for (String symbol : dfa.getAlphabetSymbols()) {

                List<String> symbolsOnStates = getSymbolsOnStates(nfa, symbol, nfaStates.get(i));
                List<String> epsilonClosure = getEpsilonClosure(nfa, symbolsOnStates);

                int index = searchNFA(nfaStates, epsilonClosure);

                if (index == -1) {
                    nfaStates.add(epsilonClosure);
                    dfa.getDFAStates().add(currentState + "");
                    alphabet.add(currentState + "");
                    currentState++;
                } else {
                    alphabet.add(dfa.getDFAStates().get(index));
                }
            }

            dfa.getAlphabetStates().add(alphabet);
        }

    }

    private static void addDFATransitions(DFA dfa) {
        String state = dfa.getDFAStates().get(0);
        int indexSym = -1;
        int indexState = 0;
        String res = "";

        for (int i = 0; i < dfa.getDFAStates().size(); i++) {
            for (int j = 0; j < dfa.getAlphabetSymbols().size(); j++) {
                String dfaFrom = dfa.getDFAStates().get(i);
                String alp = dfa.getAlphabetSymbols().get(j);
                String dfaTo = dfa.getAlphabetStates().get(i).get(j);
                res = dfaFrom + "," + alp + "," + dfaTo;

                dfa.getDFATransitions().add(res);
            }
        }
    }

    private static List<String> getSymbolsOnStates(NFA nfa, String symbol, List<String> states) {
        List<String> result = new ArrayList<String>();

        for (String state : states) {
            result.addAll(findStatesOnSymbol(nfa, symbol, state));
        }

        return result;
    }

    private static List<String> getEpsilonClosure(NFA nfa, List<String> states) {
        for (int i = 0; i < states.size(); i++) {
            states.addAll(findStatesOnSymbol(nfa, " ", states.get(i)));
            states = states.stream().distinct().collect(Collectors.toList());

        }

        states = states.stream().distinct().sorted().collect(Collectors.toList());

        return states;
    }

    private static List<String> findStatesOnSymbol(NFA nfa, String symbol, String state) {
        List<String> result = new ArrayList<String>();
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
        StringBuilder s3 = new StringBuilder();
        for (String tran : nfa.getNFATransitions()) {

            s1.delete(0, s1.length());
            s2.delete(0, s2.length());
            s3.delete(0, s3.length());


            s1.append(tran.substring(0, tran.indexOf(',')));
            s2.append(tran.substring(tran.indexOf(',') + 1, tran.lastIndexOf(',')));
            s3.append(tran.substring(tran.lastIndexOf(',') + 1));

            if (s1.toString().equals(state) && s2.toString().equals(symbol) && !result.contains(s3.toString())) {
                result.add(s3.toString());
            }
        }
        s1.delete(0, s1.length());
        s2.delete(0, s2.length());
        s3.delete(0, s3.length());

        return result;
    }

    private static String findStatesOnSymbol(DFA dfa, String symbol, String state) {
        String result = null;
        StringBuilder s1 = new StringBuilder();
        StringBuilder s2 = new StringBuilder();
        StringBuilder s3 = new StringBuilder();
        for (String tran : dfa.getDFATransitions()) {

            s1.delete(0, s1.length());
            s2.delete(0, s2.length());
            s3.delete(0, s3.length());


            s1.append(tran.substring(0, tran.indexOf(',')));
            s2.append(tran.substring(tran.indexOf(',') + 1, tran.lastIndexOf(',')));
            s3.append(tran.substring(tran.lastIndexOf(',') + 1));

            if (s1.toString().equals(state) && s2.toString().equals(symbol)) {
                result = s3.toString();
                break;
            }
        }
        s1.delete(0, s1.length());
        s2.delete(0, s2.length());
        s3.delete(0, s3.length());

        return result;
    }

    private static int searchNFA(ArrayList<List<String>> nfaStates, List<String> temp2) {
        int res = -1;

        for (int i = 0; i < nfaStates.size(); i++) {
            if (nfaStates.get(i).containsAll(temp2) && temp2.containsAll(nfaStates.get(i))) {
                return i;
            }
        }

        return res;
    }

    private static void addAcceptedStates(DFA dfa, NFA nfa, ArrayList<List<String>> nfaStates) {
        for (List<String> list : nfaStates) {
            if (list.contains(nfa.getNFAFinalState())) {
                dfa.getAcceptedStates().add("accepted");
            } else {
                dfa.getAcceptedStates().add("rejected");
            }
        }
    }

    public ArrayList<String> getDFAStates() {
        return dfaStates;
    }

    public ArrayList<ArrayList<String>> getAlphabetStates() {
        return this.alphabetStates;
    }

    public ArrayList<String> getAcceptedStates() {
        return acceptedStates;
    }

    public String getStartState() {
        return startState;
    }

    public void setDFAStartState(String startState) {
        this.startState = startState;
    }

    public void setDFAStates(ArrayList<String> dfaStates) {
        this.dfaStates = dfaStates;
    }

    public ArrayList<String> getAlphabetSymbols() {
        return alphabetSymbols;
    }

    public void setAlphabetSymbols(ArrayList<String> alphabetSymbols) {
        this.alphabetSymbols = alphabetSymbols;
    }

    public void setDFAFinalStates(ArrayList<String> acceptedStates) {
        this.acceptedStates = acceptedStates;
    }

    public ArrayList<String> getDFATransitions() {
        return dfaTransitions;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("hi");

        NFA nfa = NFA.constructFromFile("/home/ahmed/Documents/compiler_lab/Code/compiler_lab_tester/src/main/resources/lab_2/task_2_3_1.txt");


//        DFA dfa = DFA.convertNFA(nfa);

        DFA dfa = DFA.constructFromFile("/home/ahmed/compilers_submissions/lab_2/Task_2_T-09/build/" +
                "34_0705" + "/task_2_2_result.txt");
//        DFA dfa = DFA.convertNFA(nfa);



//        NFA nfa = NFA.constructFromFile("C:\\Users\\xXMoXx\\Desktop\\Compiler\\compiler_lab\\Code\\compiler_lab_tester\\src\\main\\resources\\lab_1\\test.txt");
//
//        System.out.println(nfa.getNfaStates());
//        System.out.println(nfa.getNFAAlphabet());
//        System.out.println(nfa.getNFAStartState());
//        System.out.println(nfa.getNFAFinalState());
//        System.out.println(nfa.getNFATransitions());
//        System.out.println("----------------NFA-End--------------------------");
//        DFA dfa = DFA.convertNFA(nfa);
//
////        DFA dfa = DFA.constructFromFile("C:\\Users\\xXMoXx\\Desktop\\Compiler\\compiler_lab\\Code\\compiler_lab_tester\\src\\main\\resources\\lab_2\\dfa_test_2_1.txt");
//
        System.out.println(dfa.getDFAStates());
        System.out.println(dfa.getAlphabetSymbols());
        System.out.println(dfa.getStartState());
        System.out.println(dfa.getAcceptedStates());
        System.out.println(dfa.getDFATransitions());
//


        ArrayList<String> testInput9 = new ArrayList<>();
//        testInput9.add("s");
//        testInput9.add("sssssstststsssttt");
//        testInput9.add("d");

        testInput9.add("0");
        testInput9.add("11");
        testInput9.add("1001");
        testInput9.add("1011101");
        testInput9.add("1000000001");
        testInput9.add("10111000001");
        testInput9.add("001110011011101100000000110111000001");


//        testInput9.add("stt");
//        testInput9.add("sstt");
//        testInput9.add("tstt");
//        testInput9.add("stts");
//        testInput9.add("sttt");
//        testInput9.add("stststtstst");
//        testInput9.add("ssstttsttsssttt");

        for (String input: testInput9) {
//            System.out.println();
            System.out.println("INPUT " + input + " " +dfa.evaluateString(input, "1"));
        }




//
//        try {
//            System.out.println(dfa.evaluateString("s", "0"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

}
