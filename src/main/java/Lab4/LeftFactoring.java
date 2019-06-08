package Lab4;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LeftFactoring {
    public LeftFactoring() {

    }

    public void readGrammarFile(String path) {
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            //all even numbers are head and all odd numbers are body of rule
            String lines[] = stream.toArray(String[]::new);
            ArrayList<AbstractMap.SimpleEntry<String, String>> grammarOrder = new ArrayList<>();

            for (int i = 0; i < lines.length; i += 2) {
                grammarOrder.add(new AbstractMap.SimpleEntry<String, String>(lines[i], lines[i + 1]));
            }

            leftFactor(grammarOrder);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> parseGrammar(LinkedHashMap<String, List<List<String>>> grammar) {

        HashMap<String, Boolean> headsMap = new HashMap<String, Boolean>();
        ArrayList<AbstractMap.SimpleEntry<String, String>> grammarOrder = new ArrayList<>();

        grammar.forEach((k,v) -> {
            Optional<String> reduction = v.stream().map(l -> String.join("", l)).reduce((b1,b2) -> b1 + "|" +b2);
            if (reduction.isPresent()){
                grammarOrder.add(new AbstractMap.SimpleEntry<String, String>(k, reduction.get()));
            }
        });

        return leftFactor(grammarOrder);
    }

    //Left Factoring 1st pass
    public  ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> leftFactor(ArrayList<AbstractMap.SimpleEntry<String, String>> grammar) {
        ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> finalRules = new ArrayList<>();
        ArrayList<String> rules;
        String longestPrefix;

        for (int i = 0; i < grammar.size(); i++) {
            rules = Arrays.stream(grammar.get(i).getValue().split("\\|")).collect(Collectors.toCollection(ArrayList::new));
            if (!checkRuleFactoring(rules)) {
                String head = grammar.get(i).getKey();
                rules.forEach(r -> addToGrammar(finalRules, head, r));
                continue;
            }
            longestPrefix = longestPrefix(rules);
//            System.out.println("Rules " + rules);
//            System.out.println("Head: " + grammar.get(i).getKey() + " Longest Prefix: " + longestPrefix(rules));
            for (int j = 0; j < rules.size(); j++) {
                if (rules.get(j).startsWith(longestPrefix)) {
                    addToGrammar(finalRules, grammar.get(i).getKey(), longestPrefix + grammar.get(i).getKey() + "'");

                    if (rules.get(j).equals(longestPrefix)) {
                        addToGrammar(finalRules, grammar.get(i).getKey() + "'", "epsilon");

                    } else {
                        addToGrammar(finalRules, grammar.get(i).getKey() + "'", rules.get(j).substring(longestPrefix.length()));
                    }
                } else {
                    addToGrammar(finalRules, grammar.get(i).getKey(), rules.get(j));

                }
            }


        }
        int i = 0;
        while (checkGrammarFactoring(finalRules)) {
            mergeSubGrammars(finalRules, leftFactor2(finalRules));

        }


//        System.out.println(finalRules);
        return finalRules;
    }

    //Left Factoring 2nd pass
    public  ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> leftFactor2(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> grammar) {
        ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> finalRules = new ArrayList<>();
        ArrayList<String> rules;
        String longestPrefix;

        for (int i = 0; i < grammar.size(); i++) {
            rules = grammar.get(i).getValue();

            if (!checkRuleFactoring(rules)) {
                String head = grammar.get(i).getKey();
                rules.forEach(r -> addToGrammar(finalRules, head, r));
                continue;
            }
            longestPrefix = longestPrefix(rules);
            for (int j = 0; j < rules.size(); j++) {
                if (rules.get(j).startsWith(longestPrefix)) {
                    addToGrammar(finalRules, grammar.get(i).getKey(), longestPrefix + grammar.get(i).getKey() + "'");

                    if (rules.get(j).equals(longestPrefix)) {
                        addToGrammar(finalRules, grammar.get(i).getKey() + "'", "epsilon");

                    } else {
                        addToGrammar(finalRules, grammar.get(i).getKey() + "'", rules.get(j).substring(longestPrefix.length()));
                    }
                } else {
                    addToGrammar(finalRules, grammar.get(i).getKey(), rules.get(j));

                }
            }


        }
        return finalRules;
    }

    public  String longestPrefix(ArrayList<String> rules) {
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

    public  void addToHashMap(AbstractMap<String, ArrayList<String>> table, String key, String value) {
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

    public  void addToGrammar(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> grammars, String head, String production) {
        int index = -1;
        for (int i = 0; i < grammars.size(); i++) {
            if (grammars.get(i).getKey().equals(head)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            if (!grammars.get(index).getValue().contains(production)) {
                grammars.get(index).getValue().add(production);
            }
        } else {
            grammars.add(new AbstractMap.SimpleEntry<String, ArrayList<String>>(head, new ArrayList<String>() {{
                add(production);
            }}));
        }
    }


    public  AbstractMap<ArrayList<String>, ArrayList<String>> invertMap(AbstractMap<String, ArrayList<String>> table) {
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


    public  boolean checkRuleFactoring(ArrayList<String> rules) {
        if (longestPrefix(rules).equals("")) {
            return false;
        } else {
            return true;
        }
    }

    public  boolean checkGrammarFactoring(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> grammar) {
        for (int i = 0; i < grammar.size(); i++) {
            if (checkRuleFactoring(grammar.get(i).getValue())) {
                return true;
            }
        }
        return false;
    }

    public  void mergeSubGrammars(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> main, ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> sub) {
        for (int i = 0; i < main.size(); i++) {

            for (int j = 0; j < sub.size(); j++) {

                if (main.get(i).getKey().equals(sub.get(j).getKey())) {

                    if (main.get(i).getValue().size() != sub.get(j).getValue().size()) {
                        main.set(i, new AbstractMap.SimpleEntry<String, ArrayList<String>>(main.get(i).getKey(), sub.get(j).getValue()));
                    } else {

                        for (int k = 0; k < main.get(i).getValue().size(); k++) {

                            for (int l = 0; l < sub.get(j).getValue().size(); l++) {
                                if (!main.get(i).getValue().get(k).equals(sub.get(j).getValue().get(l))) {
                                    main.set(i, new AbstractMap.SimpleEntry<String, ArrayList<String>>(main.get(i).getKey(), sub.get(j).getValue()));

                                }
                            }

                        }
                    }
                }
            }
        }


        for (int j = 0; j < sub.size(); j++) {
            if (!main.contains(sub.get(j))) {
                main.add(new AbstractMap.SimpleEntry<String, ArrayList<String>>(sub.get(j).getKey(), sub.get(j).getValue()));
            }
        }


    }


    public  void write(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> rules, String path, Boolean append) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, append)));

            rules.forEach(r -> {
                pw.println(r.getKey() + "->" + r.getValue());
            });
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
