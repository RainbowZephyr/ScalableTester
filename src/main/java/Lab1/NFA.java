package Lab1;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.stream.Collectors;


public class NFA {
    private String nfaStartState;
    private String nfaFinalState;
    private ArrayList<String> nfaStates;
    private ArrayList<String> nfaAlphabet;
    private ArrayList<String> nfaTransitions;

    public NFA() {
        this.nfaStartState = "";
        this.nfaFinalState = "";
        this.nfaStates = new ArrayList<String>();
        this.nfaAlphabet = new ArrayList<String>();
        this.nfaTransitions = new ArrayList<String>();
    }

    public NFA(ArrayList<String> nfaStates, String startState, String finalState, ArrayList<String> nfaAlphabet, ArrayList<String> nfaTransitions) {
        this.nfaStates = nfaStates;
        this.nfaStartState = startState;
        this.nfaFinalState = finalState;
        this.nfaAlphabet = nfaAlphabet;
        this.nfaTransitions = nfaTransitions;
    }

//    public Lab1.NFA(Lab1.ExpressionContainer ecResult) {
//        this.nfaStartState = ecResult.startState;
//        this.nfaFinalState = ecResult.finalState;
//        this.nfaStates = ecResult.states;
//        this.nfaAlphabet = ecResult.allSymbols;
//        this.nfaTransitions = new ArrayList<String>();
//
//        for (String tran : ecResult.transitions) {
//            tran = tran.replaceAll("\\s+", "");
//            nfaTransitions.add(tran);
//        }
//
//    }

    public static NFA constructFromFile(String path) {

        try {
            NFA nfa = new NFA();
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

            nfa.setNFAStates(new ArrayList<>(tmpHash));
            tmpHash.clear();

            line = Arrays.stream(lines.get(1).split(",", -1)).map(e -> {
                e = e.replaceAll(reg1, "");
                if (e == null || e.matches("^\\s*$")) {
                    return " ";
                }
                return e.trim().replaceAll("[\uD835\uDF00εϵ\uD835\uDF3A\uD835\uDFAE]+", " ");
            }).collect(Collectors.toCollection(ArrayList<String>::new));

            tmpHash.addAll(line);

            nfa.setNFAAlphabet(new ArrayList<>(tmpHash));
            tmpHash.clear();

            nfa.setNfaStartState(lines.get(2).replaceAll(reg1, "").trim());

            nfa.setNFAFinalState(lines.get(3).replaceAll(reg1, "").trim());

            formatTransitions(nfa, lines.get(4));

            return nfa;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void formatTransitions(NFA nfa, String transistions) {
//        String regex =  "\\(.+?\\)";
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
        String tmp = new String();

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
            nfa.getNFATransitions().add(str);
        }

        matchList = null;
        matchListResult = null;
        tmpExtra = null;

    }

    public String getNFAStartState() {
        return nfaStartState;
    }

    public void setNfaStartState(String nfaStartState) {
        this.nfaStartState = nfaStartState;
    }

    public String getNFAFinalState() {
        return nfaFinalState;
    }

    public void setNFAFinalState(String nfaFinalState) {
        this.nfaFinalState = nfaFinalState;
    }

    public ArrayList<String> getNfaStates() {
        return nfaStates;
    }

    public void setNFAStates(ArrayList<String> nfaStates) {
        this.nfaStates = nfaStates;
    }

    public ArrayList<String> getNFAAlphabet() {
        return nfaAlphabet;
    }

    public void setNFAAlphabet(ArrayList<String> nfaAlphabet) {
        this.nfaAlphabet = nfaAlphabet;
    }

    public ArrayList<String> getNFATransitions() {
        return nfaTransitions;
    }

    public void setNFATransitions(ArrayList<String> nfaTransitions) {
        this.nfaTransitions = nfaTransitions;
    }

}


