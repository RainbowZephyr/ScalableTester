package Lab4;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LeftRecursion {

    public ArrayList<AbstractMap.SimpleEntry<String, String>> grammarOrder;
    public LinkedHashMap<String, String> grammarDictionary;
    public ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> finalRules;


    public LeftRecursion(){
        grammarOrder = new ArrayList<>();
        grammarDictionary = new LinkedHashMap<>();
        finalRules = new ArrayList<>();
    }


    public void parseGrammar(LinkedHashMap<String, List<List<String>>> grammar) {

        HashMap<String, Boolean> headsMap = new HashMap<String, Boolean>();

        grammar.forEach((k,v) -> {
            Optional<String> reduction = v.stream().map(l -> String.join("", l)).reduce((b1,b2) -> b1 + "|" +b2);
            if (reduction.isPresent()){
                grammarOrder.add(new AbstractMap.SimpleEntry<String, String>(k, reduction.get()));
                grammarDictionary.put(k , reduction.get());
            }
        });
//        System.out.println("ORDER " + grammarOrder);
            eliminateLeftRecursion(grammarOrder);
    }

    public void eliminateLeftRecursion(ArrayList<AbstractMap.SimpleEntry<String, String>> grammar) {
//        System.out.println("Grammar: " + grammar);

        if (isImmediateLeft(grammar.get(0))) {
            eliminateImmediateLeft(finalRules, grammar.get(0).getKey(), grammar.get(0).getValue());
        } else {
            addToRules(finalRules, grammar.get(0).getKey(), grammar.get(0).getValue().split("\\|"));
        }
        boolean handled; //= false;

        for (int i = 1; i < grammar.size(); i++) {
            handled = false;
//            System.out.println(grammar.get(i));
            for (int j = 0; j < i; j++) {
                if (grammar.get(i).getValue().contains(grammar.get(j).getKey())) {
                    handled = true;
                    String head = grammar.get(i).getKey();
                    String body = grammar.get(i).getValue();
                    String substitutedBody = substituteRule(grammar.get(j).getKey(), grammar.get(j).getValue(), body);

                    if (isImmediateLeft(new AbstractMap.SimpleEntry<String, String>(head, substitutedBody))) {
                        eliminateImmediateLeft(finalRules, head, substitutedBody);
                    } else {
                        addToRules(finalRules, grammar.get(i).getKey(), grammar.get(i).getValue().split("\\|"));
                    }
                }
            }

//            if(i == 2) {
//                System.out.println(2);
//                System.out.println(handled);
//            }
            if (!handled) {
                if (isImmediateLeft(grammar.get(i))) {
                    eliminateImmediateLeft(finalRules, grammar.get(i).getKey(), grammar.get(i).getValue());
                } else {
                    addToRules(finalRules, grammar.get(i).getKey(), grammar.get(i).getValue().split("\\|"));
                }
            }
        }
        appendEpsilon(finalRules);
    }

    public boolean isImmediateLeft(AbstractMap.SimpleEntry<String, String> rule) {
        String subRules[] = rule.getValue().split("\\|");
        for (int i = 0; i < subRules.length; i++) {
            if (subRules[i].contains(rule.getKey()) && rule.getKey().equals(subRules[i].substring(0, rule.getKey().length()))) {

                return true;
            }
        }
        return false;
    }

    public String substituteRule(String head, String sourceRule, String destinationRule) {
        String copy = destinationRule;
        String extractor = head + "([a-zA-Z0-9,.])";
        Pattern pattern = Pattern.compile(extractor);
        Matcher matcher = pattern.matcher(copy);
        while (matcher.find()) {
            String splitted[] = sourceRule.split("\\|");
            String result = String.join("|", Arrays.stream(splitted).map(s -> s + matcher.group(1)).collect(Collectors.toList()));
            return copy.replace(head + matcher.group(1), result);
        }
        return copy;
    }

    public void eliminateImmediateLeft(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> rules, String head, String body) {
        String subRules[] = body.split("\\|");

        for (int i = 0; i < subRules.length; i++) {
//            System.out.println("SUB " + subRules[i] + "  " + subRules[i].substring(head.length()));

            //If contains head then this is α case else β
            if (subRules[i].startsWith(head)) {
                addToRules(rules, head + "'", subRules[i].substring(head.length()) + head + "'");
            } else {
                if (subRules[i].equals("epsilon")){
                    addToRules(rules, head, head + "'");
                } else {
                    addToRules(rules, head, subRules[i] + head + "'");
                }
            }
        }
    }

    public void addToRules(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> rules, String head, String body) {
        if (rules.stream().filter(p -> p.getKey().equals(head)).count() == 0) {
            rules.add(new AbstractMap.SimpleEntry<String, ArrayList<String>>(head, new ArrayList<String>() {{
                add(body);
            }}));
        } else {
            rules.stream().filter(p -> p.getKey().equals(head)).findFirst().get().getValue().add(body);
        }
    }

    public void addToRules(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> rules, String head, String body[]) {
        if (rules.stream().filter(p -> p.getKey().equals(head)).count() == 0) {
            rules.add(new AbstractMap.SimpleEntry<String, ArrayList<String>>(head, new ArrayList<String>() {{
                for (int i = 0; i < body.length; i++) {
                    add(body[i]);
                }
            }}));
        } else {
            for (int i = 0; i < body.length; i++) {
                rules.stream().filter(p -> p.getKey().equals(head)).findFirst().get().getValue().add(body[i]);
            }
        }
    }

    public void appendEpsilon(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> rules) {
        //𝜀
        rules.stream().filter(p -> p.getKey().contains("'")).forEach(e -> e.getValue().add("epsilon"));

    }

    public synchronized static void writeTransformedGrammar(ArrayList<AbstractMap.SimpleEntry<String, ArrayList<String>>> rules, String path, boolean append) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, append)));
            rules.forEach(r -> pw.println(r.toString().replace("=", "->")));

            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

