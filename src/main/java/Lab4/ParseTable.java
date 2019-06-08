package Lab4;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseTable {
    public TreeMap<String, TreeMap<String, String>> table; // Head -> Terminal -> Rule
    public HashMap<String, HashSet<String>> filteredFirst;
    public String splitRegex;
    public FirstAndFollow firstAndFollowInstance;

    public ParseTable(FirstAndFollow ff) {
        table = new TreeMap<>();
        this.firstAndFollowInstance = ff;
    }

    public boolean generateTable() {
        LinkedHashMap<String, String> grammarDictionary = firstAndFollowInstance.grammarDictionary;
        ArrayList<String> heads = firstAndFollowInstance.heads;
        ArrayList<String> nonFilteredTerminals = firstAndFollowInstance.terminals;
        ArrayList<String> terminals = new ArrayList<>(nonFilteredTerminals);


        LinkedHashMap<String, HashSet<String>> first = firstAndFollowInstance.first;
        LinkedHashMap<String, HashSet<String>> follow = firstAndFollowInstance.follow;

        generateSplitPattern(heads, new ArrayList<>(terminals));
        heads.forEach(h -> table.put(h, new TreeMap<String, String>()));

        terminals.remove("epsilon");
        terminals.add("$");

        table.forEach((h, tbl) -> {
            terminals.forEach(t -> {
                if (!t.equals("epsilon")) {
                    tbl.put(t, "null");
                }
            });
//            tbl.put("$", "null");
        });


        filteredFirst = (HashMap<String, HashSet<String>>) first.entrySet().stream().filter(h -> (!terminals.contains(h.getKey()) && !h.getKey().equals("epsilon"))).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

//        System.out.println("FILTERED " + filteredFirst);
        ArrayList<String> rules;
        Pattern splittingPattern;
        Matcher splittingMatcher;

        for (HashMap.Entry<String, HashSet<String>> entry : filteredFirst.entrySet()) {
            for (String firstItem : entry.getValue()) {
                if (firstItem.equals("epsilon")) {
                    for (String fo : follow.get(entry.getKey())) {
                        if (!checkLL1(table, entry.getKey(), fo)) {
                            System.err.println("HEAD " + entry.getKey() + " FOLL " + fo);
                            System.err.println("NOT LL(1) GRAMMAR FOLLOW " + firstAndFollowInstance.id);
                            System.err.println("TASK " + firstAndFollowInstance.task);
                            return false;
                        } else {
                            table.get(entry.getKey()).put(fo, "epsilon");
                        }
                    }
                } else {
                    List<List<String>> body = this.firstAndFollowInstance.grammarDictionaryList.get(entry.getKey());

                    if (body.size() == 1) {
                        if (this.table.get(entry.getKey()).get(firstItem).equals("null")) {
                            this.table.get(entry.getKey()).put(firstItem, String.join("",body.get(0)));
                        } else {
                            return false;
                        }

                    } else {
                        for (List<String> rule : body) {
                            if (this.firstAndFollowInstance.heads.contains(rule.get(0))) {
                                if (this.filteredFirst.get(rule.get(0)).contains(firstItem)) {
                                    if (this.table.get(entry.getKey()).get(firstItem).equals("null")) {
                                        this.table.get(entry.getKey()).put(firstItem, String.join("",rule));
                                    } else {
                                        return false;
                                    }

//                                    if (!this.filteredFirst.get(rule.get(0)).contains("epsilon")) {
//                                        break;
//                                    }
                                }
                            } else {
                                if(rule.get(0).equals(firstItem)) {
                                    if (this.table.get(entry.getKey()).get(firstItem).equals("null")) {
                                        this.table.get(entry.getKey()).put(firstItem, String.join("",rule));
                                    } else {
                                        return false;
                                    }
                                }
//                                else {
//                                    System.err.println("UNKNOWN TERMINAL " + firstItem + " HEAD " + entry.getKey());
//                                    return false;
//                                }
                            }
                        }
                    }
                }
            }
        }


//        for (HashMap.Entry<String, HashSet<String>> entry : filteredFirst.entrySet()) {
//            System.out.println("ENTRY SET " + entry);
//            for (String f : entry.getValue()) {
//                if (f.trim().equals("epsilon")) {
//                    for (String fo : follow.get(entry.getKey())) {
//                        if (!checkLL1(table, entry.getKey(), fo)) {
//                            System.err.println("HEAD " + entry.getKey() + " FOLL " + fo);
//                            System.err.println("NOT LL(1) GRAMMAR FOLLOW " + firstAndFollowInstance.id);
//                            System.err.println("TASK " + firstAndFollowInstance.task);
//                            return false;
//                        } else {
//                            table.get(entry.getKey()).put(fo, "epsilon");
//                        }
//                    }
//                } else {
//                    System.out.println("ENTRY KEY " + entry.getKey());
//                    if (grammarDictionary.get(entry.getKey()).startsWith(f)) {
//                        rules = new ArrayList<>(Arrays.asList(grammarDictionary.get(entry.getKey()).split("\\|")));
//                        rules = rules.stream().filter(r -> r.startsWith(f)).collect(Collectors.toCollection(ArrayList::new));
////                        System.out.println("RULES "+ rules);
//                        if (rules.size() > 1) {
//                            System.err.println("NOT LL(1) GRAMMAR FOLLOW " + firstAndFollowInstance.id);
//                            System.err.println("TASK " + firstAndFollowInstance.task);
//
//                            return false;
//                        }
//                        if (table.get(entry.getKey()).get(f).equals("null")) {
//                            table.get(entry.getKey()).put(f, rules.get(0));
//                        } else if (!table.get(entry.getKey()).get(f).equals(rules.get(0))) {
//                            System.err.println("NOT LL(1) GRAMMAR FOLLOW " + firstAndFollowInstance.id);
//                            System.err.println("TASK " + firstAndFollowInstance.task);
//
//                            return false;
//                        }
//
//                    } else {
//                        rules = new ArrayList<>(Arrays.asList(grammarDictionary.get(entry.getKey()).split("\\|")));
//                        splittingPattern = Pattern.compile(splitRegex);
//                        System.out.println("RULES " + rules);
//                        for (String rule : rules) {
//                            splittingMatcher = splittingPattern.matcher(rule);
//                            if (splittingMatcher.find()) {
//                                System.out.println("MATCH " + splittingMatcher.group(0));
//                                if (first.get(splittingMatcher.group("split")).contains(f)) {
//                                    table.get(entry.getKey()).put(f, rule);
//                                    if (table.get(entry.getKey()).get(f).equals("null")) {
//                                        table.get(entry.getKey()).put(f, rule);
//                                    } else if (!table.get(entry.getKey()).get(f).equals(rule)) {
//                                        System.err.println("NOT LL(1) GRAMMAR FOLLOW " + firstAndFollowInstance.id);
//                                        System.err.println("TASK " + firstAndFollowInstance.task);
//
//                                        return false;
//                                    }
//
//                                    if (first.get(splittingMatcher.group("split")).contains("epsilon")) {
//
//                                    }
//
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }
//        }


        return true;
    }


    public boolean checkLL1(TreeMap<String, TreeMap<String, String>> table, String head, String terminal) {
        return table.get(head).get(terminal).equals("null");
    }

    public void generateSplitPattern(ArrayList<String> heads, ArrayList<String> terminals) {
        ArrayList<String> regex = new ArrayList<>();
        for (int i = 0; i < terminals.size(); i++) {
            regex.add(terminals.get(i).replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", "\\*").replace("?", "\\?").replace("[", "\\[").replace("]", "\\]"));

        }

        splitRegex = "(?<split>" + heads.stream().collect(Collectors.joining("|")) + "|" + regex.stream().collect(Collectors.joining("|")) + ")";
    }


    public void printTable(Set<String> terminals) {
        String row = "     ";

        if (!terminals.contains("$")) {
            terminals.add("$");
        }

        if (terminals.contains("epsilon")) {
            terminals.remove("epsilon");
        }

        row += String.join("        ", terminals);
//        System.out.println(row);

        table.forEach((h, tbl) -> {
            String tmp = h;
            for (String t : terminals) {
                tmp += "    " + tbl.get(t);
            }
//            System.out.println(tmp);
        });

    }

    public void writeTable(ArrayList<String> terminals, String path) {

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));

            String row = "     ";

            row += terminals.stream().collect(Collectors.joining("    "));
            pw.println(row);

            table.forEach((h, tbl) -> {
                String tmp = h;
                for (String t : terminals) {
                    tmp += "    " + tbl.get(t);
                }
                pw.println(tmp);
            });
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
