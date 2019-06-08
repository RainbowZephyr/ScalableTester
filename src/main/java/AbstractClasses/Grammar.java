package AbstractClasses;

import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Grammar {

    public static LinkedHashMap<String, List<List<String>>> readGrammar(List<String> lines) {
        LinkedHashMap<String, List<List<String>>> result = new LinkedHashMap<>();
        ArrayList<String> rules, newRule;

        String[] tuple;
        String variable;
        List<List<String>> newRules = new ArrayList<List<String>>();
            for (int i = 0; i < lines.size(); i++) {
                tuple = lines.get(i).split(":");
                variable = tuple[0].trim();

                rules = new ArrayList<>(Arrays.asList(tuple[1].trim().split("\\|")));
                //System.out.println(rules);
                for (String rule : rules) {
                    newRule = new ArrayList<>(Arrays.asList(rule.trim().split("\\s+")));
                    newRules.add(newRule);
                }

                handleEntry(result, variable, newRules);
                newRules.clear();

            }

        return result;
    }

    public static TreeMap<String, HashSet<List<String>>> readSortedGrammar(List<String> lines) {
        TreeMap<String, HashSet<List<String>>> result = new TreeMap<>();
        ArrayList<String> rules, newRule;

        String[] tuple;
        String variable;
        HashSet<List<String>> newRules = new HashSet<>();
        for (int i = 0; i < lines.size(); i++) {
            tuple = lines.get(i).split(":");
            variable = tuple[0].trim();

            rules = new ArrayList<>(Arrays.asList(tuple[1].trim().split("\\|")));
            //System.out.println(rules);
            for (String rule : rules) {
                newRule = new ArrayList<>(Arrays.asList(rule.trim().split("\\s+")));
                newRules.add(newRule);
            }

            handleEntry(result, variable, newRules);
            newRules.clear();

        }

        return result;
    }

    private static void handleEntry(Map<String, List<List<String>>> grammar, String variable, List<List<String>> newRules) {
        if (grammar.containsKey(variable)) {
            for (List<String> list : newRules){
                grammar.get(variable).add(new ArrayList<>(list));
            }
        } else {
            grammar.put(variable, new ArrayList<>(newRules));
        }
    }

    private static void handleEntry(Map<String, HashSet<List<String>>> grammar, String variable, HashSet<List<String>> newRules) {
        if (grammar.containsKey(variable)) {
            for (List<String> list : newRules){
                grammar.get(variable).add(new ArrayList<>(list));
            }
        } else {
            grammar.put(variable, new HashSet<List<String>>(newRules));
        }
    }

    public static TreeMap<String, TreeMap<String, List<String>>> readTable(List<String> lines){
        TreeMap<String, TreeMap<String, List<String>>> table = new TreeMap<>();
        String head;
        for(String line: lines){
            final String[] tuple = line.split(":", 3);
            final ArrayList<String>  body = Arrays.stream(tuple[2].split(" ")).map(String::trim).filter(b -> !b.isEmpty()).collect(Collectors.toCollection(ArrayList<String>::new));

            head = tuple[0].trim();

            if(!table.containsKey(head)) {
                table.put(head, new TreeMap<String, List<String>>() {{put(tuple[1].trim(), body);}});
            } else {
                table.get(head).put(tuple[1].trim(), body);
            }

        }

        return table;

    }

    public static ArrayList<String> extractHeads(LinkedHashMap<String, List<List<String>>> grammar) {
        return new ArrayList<>(grammar.keySet());
    }

    public static ArrayList<String> extractTerminals(LinkedHashMap<String, List<List<String>>> grammar) {
        List<String> heads = extractHeads(grammar);
        HashMap<String, Boolean> headsMap = new HashMap<String, Boolean>();
        heads.forEach(h -> headsMap.put(h, true));

        HashSet<String> terminalSet = new HashSet<String>();
        heads.forEach(h -> {
            for (List<String> production : grammar.get(h)) {
                production.stream().filter(p -> !headsMap.containsKey(p)).forEach(terminalSet::add);
            }
        });

        return new ArrayList<>(terminalSet);
    }

    public static ArrayList<String> extractHeads(List<AbstractMap.SimpleEntry<String, ArrayList<String>>> grammar) {
        return grammar.stream().map(x -> x.getKey()).collect(Collectors.toCollection(ArrayList::new));
    }

    public static String generateStrings(LinkedHashMap<String, List<List<String>>> grammar, int counter) {
        Stack<String> stack = new Stack<>();
        String start = ((Map.Entry<String, List<List<String>>>) grammar.entrySet().toArray()[0]).getKey();

        stack.push(start);
        StringBuilder stringInGrammar = new StringBuilder();
        String currentHead;
        List<String> chosenRule;
        Scanner sc = new Scanner(System.in);
        Random random = new Random();


        while (!stack.empty()) {
            if (counter <= 0 ){
                return null;
            }

            currentHead = stack.pop();

            if (grammar.containsKey(currentHead)) {
                chosenRule = randomSelection(grammar.get(currentHead), random);

                for (int i = chosenRule.size() - 1; i >= 0; i--) {
                    stack.push(chosenRule.get(i));
                }


            } else {
                if (!currentHead.equals("epsilon")) {
                    stringInGrammar.append(currentHead);
                    stringInGrammar.append(" ");

                }
            }
//            sc.next();
            counter--;
        }
        return stringInGrammar.toString();


    }

    private static List<String> randomSelection(List<List<String>> rule, Random randomGenerator) {
        Collections.shuffle(rule);
        return rule.get(randomGenerator.nextInt(rule.size()));
    }
}
