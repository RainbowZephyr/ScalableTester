package Lab4;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {

    public  Stack<String> stack;
    public  Node parseTree;
    public  String splitRegex;
    public  ArrayList<String> parseSteps;
    public  boolean error;
    private ParseTable table;

    public Parser(ParseTable table) {
        stack = new Stack<>();
        error = false;
        this.table = table;
    }

//    public  void readFile(String path,
//                                LinkedHashMap<String, String> grammarDictionary,
//                                TreeMap<String, TreeMap<String, String>> table,
//                                ArrayList<String> heads,
//                                ArrayList<String> terminals) {
//
//        try (Stream<String> stream = Files.lines(Paths.get(path))) {
//
//            //all even numbers are head and all odd numbers are body of rule
//            String lines[] = stream.toArray(String[]::new);
//            terminals.remove("$");
//            generateSplitPattern(heads, terminals);
//
//            parse(lines, (String) grammarDictionary.keySet().toArray()[0], grammarDictionary, table, heads, terminals);
//            writeParseOutput("PARSE OUPUT: "+ path.split("\\.")[0]+".out");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public boolean parseInput(List<String> input) {
        table.firstAndFollowInstance.terminals.remove("$");
        generateSplitPattern(table.firstAndFollowInstance.heads,table.firstAndFollowInstance.terminals);
        return parse(input, table.firstAndFollowInstance.start, table.table, table.firstAndFollowInstance.terminals);

    }

    public boolean parseInput(String[] input) {
        table.firstAndFollowInstance.terminals.remove("$");
//        System.out.println(table.firstAndFollowInstance.terminals);
        generateSplitPattern(table.firstAndFollowInstance.heads,table.firstAndFollowInstance.terminals);
        return parse(input, table.firstAndFollowInstance.start, table.table, table.firstAndFollowInstance.terminals);

    }

    public boolean parse(String[] lines, String startHead, TreeMap<String, TreeMap<String, String>> table, ArrayList<String> terminals ) {
        stack.clear();
//        System.out.println("-------------------------------------Start Parsing---------------------------------------------------------");
//        System.out.println("lines " + Arrays.toString(lines));
//        System.out.println("start head " + startHead);
//        System.out.println("table " + table);
//        System.out.println("terminals " + terminals);

        String topStack = "";
        String input = "";

        ArrayList<String> production = new ArrayList<>();
        Pattern splittingPattern = Pattern.compile(splitRegex);
        Matcher splittingMatcher;

        stack.push("$");
        stack.push(startHead);


        for (int i = 0; i < lines.length; i++) {
            topStack = stack.peek();
            input = lines[i];

//            System.out.println("input " + input + " stack " + stack);

            if (!terminals.contains(input) && !input.equals("$")) {
                return false;
            }

            if (input.equals("$") && topStack.equals("$")) {
                return true;
            }

            if (input.equals("$") && terminals.contains(topStack)) {
                return false;
            }

            if (!input.equals("$") && topStack.equals("$")) {
                return false;
            }

            if (terminals.contains(input) && terminals.contains(topStack)) {
                if (input.equals(topStack)) {
                    stack.pop();
                    continue;
                } else {
                    return false;
                }
            }

            if (!terminals.contains(topStack)) {

                production.clear();

                splittingMatcher = splittingPattern.matcher(table.get(topStack).get(input));
                while (splittingMatcher.find()) {
                    production.add(splittingMatcher.group("split"));
                }

                stack.pop();
                production.remove("epsilon");

                production.stream()
                        .collect(Collectors.collectingAndThen(Collectors.toList(),
                                lst -> {
                                    Collections.reverse(lst);
                                    return lst.stream();
                                }
                        ))
                        .forEach(e -> stack.push(e));

                i--;
                continue;
            }

            return false;

        }


//        System.out.println("-------------------------------------End Parsing---------------------------------------------------------");
        return false;
    }


    public  boolean parse(List<String> lines, String startHead,TreeMap<String, TreeMap<String, String>> table, ArrayList<String> terminals) {
        stack.clear();
//        System.out.println("-------------------------------------Start Parsing---------------------------------------------------------");
//        System.out.println("lines " + Arrays.toString(lines));
//        System.out.println("start head " + startHead);
//        System.out.println("table " + table);
//        System.out.println("terminals " + terminals);

        String topStack = "";
        String input = "";

        ArrayList<String> production = new ArrayList<>();
        Pattern splittingPattern = Pattern.compile(splitRegex);
        Matcher splittingMatcher;

        stack.push("$");
        stack.push(startHead);


        for (int i = 0; i < lines.size(); i++) {
            topStack = stack.peek();
            input = lines.get(i);

//            System.out.println("input " + input + " stack " + stack);

            if (!terminals.contains(input) && !input.equals("$")) {
                return false;
            }

            if (input.equals("$") && topStack.equals("$")) {
                return true;
            }

            if (input.equals("$") && terminals.contains(topStack)) {
                return false;
            }

            if (!input.equals("$") && topStack.equals("$")) {
                return false;
            }

            if (terminals.contains(input) && terminals.contains(topStack)) {
                if (input.equals(topStack)) {
                    stack.pop();
                    continue;
                } else {
                    return false;
                }
            }

            if (!terminals.contains(topStack)) {

                production.clear();

                splittingMatcher = splittingPattern.matcher(table.get(topStack).get(input));
                while (splittingMatcher.find()) {
                    production.add(splittingMatcher.group("split"));
                }

                stack.pop();
                production.remove("epsilon");

                production.stream()
                        .collect(Collectors.collectingAndThen(Collectors.toList(),
                                lst -> {
                                    Collections.reverse(lst);
                                    return lst.stream();
                                }
                        ))
                        .forEach(e -> stack.push(e));

                i--;
                continue;
            }
        }

        return false;
    }


    public  void generateSplitPattern(ArrayList<String> heads, ArrayList<String> terminals) {
        ArrayList<String> regex = new ArrayList<>();
        for (int i = 0; i < terminals.size(); i++) {
            regex.add(terminals.get(i).replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", "\\*").replace("?", "\\?").replace("[", "\\[").replace("]", "\\]"));

        }

        splitRegex = "(?<split>" + heads.stream().collect(Collectors.joining("|")) + "|" + regex.stream().collect(Collectors.joining("|")) + ")";
    }


    public  void writeParseOutput(String path) {

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
            if (error) {
                pw.println("PARSE ERROR");
            } else {
                parseSteps.forEach(s -> pw.println(s));
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
