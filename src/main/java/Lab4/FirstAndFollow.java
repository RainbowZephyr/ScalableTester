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


public class FirstAndFollow {
    public LinkedHashMap<String, String> grammarDictionary;
    public LinkedHashMap<String, List<List<String>>> grammarDictionaryList;

    public ArrayList<AbstractMap.SimpleEntry<String, String>> grammarOrder;
    public LinkedHashMap<String, HashSet<String>> first;
    public LinkedHashMap<String, HashSet<String>> follow;
    public LinkedHashMap<String, ArrayList<String>> followDependencies;
    public ArrayList<String> heads, terminals;
    public Pattern terminalPattern, headPattern;
    public String followRegex, splitRegex;
    public String start;
    public String id;
    public String task;

    public FirstAndFollow(String id, String task) {
        grammarDictionary = new LinkedHashMap<>();
        grammarOrder = new ArrayList<>();
        first = new LinkedHashMap<>();
        follow = new LinkedHashMap<>();
        followDependencies = new LinkedHashMap<>();
        this.id = id;
        this.task = task;
    }


    public void parseGrammar(LinkedHashMap<String, List<List<String>>> grammar) {
        this.grammarDictionaryList = grammar;


        //all even numbers are head and all odd numbers are body of rule
        HashMap<String, Boolean> headsMap = new HashMap<String, Boolean>();
        LinkedHashMap<String, HashSet<String>> grammarSet = new LinkedHashMap<>();

        heads = new ArrayList<>(grammar.keySet());
        heads.forEach(h -> headsMap.put(h, true));


        HashSet<String> terminalSet = new HashSet<String>();
        heads.forEach(h -> {
            for (List<String> production : grammar.get(h)) {
                production.stream().filter(p -> !headsMap.containsKey(p)).forEach(p -> terminalSet.add(p));
            }
        });

//        System.out.println("SET "+ terminalSet);

        terminals = new ArrayList<>(terminalSet);

        grammar.forEach((k, v) -> {
            Optional<String> reduction = v.stream().map(l -> String.join("", l)).reduce((b1, b2) -> b1 + "|" + b2);
            if (reduction.isPresent()) {
                grammarOrder.add(new AbstractMap.SimpleEntry<String, String>(k, reduction.get()));
                grammarDictionary.put(k, reduction.get());
                HashSet<String> set = new HashSet<>();
                v.forEach(set::addAll);
                grammarSet.put(k, set);
            }
        });


//        System.out.println("GRAMMAR SET "  + grammarSet);
        start = ((Map.Entry<String, List<List<String>>>) grammarDictionary.entrySet().toArray()[0]).getKey();

        sortHeads(heads);
        compileTerminalPattern(terminals);
        compileHeadPattern(heads);


        generateFollowPattern(heads, terminals);
        generateSplitPattern(heads, terminals);

        firstTerminals(terminals);

        try {
            first(grammar);
            ArrayList<String> clear = new ArrayList<>();
            first.forEach((k, v) -> {
//                System.out.println("KEY " + k + " VAL " + v);
                if (!k.equals("epsilon")) {


                    if (v.contains("epsilon") && !grammarSet.get(k).contains("epsilon")) {

                        for (String element : grammarSet.get(k)) {
                            if (headsMap.containsKey(element)) {
                                if (grammarSet.get(element).contains("epsilon")) {
                                    continue;
                                } else {
                                    clear.add(k);
                                    break;
                                }
                            }
                        }
                    }
                }
            });

            clear.forEach(e -> first.get(e).remove("epsilon"));

            follow(grammarOrder);
        } catch (StackOverflowError e) {

            System.err.println("###################### " + this.id + " ERROR ABORTING! ######################");
            System.err.println("################### " + this.task + " GRAMMAR IS RECURSIVE ####################");
        }

//        System.out.println("FIRST " + first);
//        System.out.println("FOLLOW " + follow);

    }

    public void parseGrammar(List<AbstractMap.SimpleEntry<String, ArrayList<String>>> grammar, ArrayList<String> heads,
                             ArrayList<String> terminals) {


        this.heads = heads;
        this.terminals = terminals;
        ArrayList<AbstractMap.SimpleEntry<String, String>> transformed = grammar.stream().map(e -> {
            return new AbstractMap.SimpleEntry<String, String>(e.getKey(), String.join("|", e.getValue()));
        }).collect(Collectors.toCollection(ArrayList::new));

        transformed.stream().forEach(e -> {
            grammarDictionary.put(e.getKey(), e.getValue());
        });


//        System.out.println("OR@" + transformed);
        this.grammarOrder = transformed;
//        System.out.println("ORDER# " + grammarOrder);
        sortHeads(heads);
        compileTerminalPattern(terminals);
        compileHeadPattern(heads);


        generateFollowPattern(heads, terminals);
        generateSplitPattern(heads, terminals);

        firstTerminals(terminals);

        try {
            first(grammarOrder);
            follow(grammarOrder);
        } catch (StackOverflowError e) {
            System.err.println("###################### " + this.id + " ERROR ABORTING! ######################");
            System.err.println("################### " + this.task + " GRAMMAR IS RECURSIVE ####################");
        }

//        System.out.println("FIRST " + first);
//        System.out.println("FOLLOW " + follow);

    }


    public void sortHeads(ArrayList<String> unsorted) {
        String tmp = "";
        for (int i = 0; i < unsorted.size(); i++) {
            if (unsorted.get(i).contains("'")) {
                tmp = unsorted.get(i);
                unsorted.remove(i);
                unsorted.add(0, tmp);
            }
        }
    }

    public void compileTerminalPattern(ArrayList<String> terminals) {
        ArrayList<String> regex = new ArrayList<>();
        for (int i = 0; i < terminals.size(); i++) {
            regex.add(terminals.get(i).replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", "\\*").replace("?", "\\?"));
        }

        String regexPattern = "^(" + regex.stream().collect(Collectors.joining("|")) + "|!)";
        terminalPattern = Pattern.compile(regexPattern);
    }

    public void compileHeadPattern(ArrayList<String> heads) {
        ArrayList<String> regex = new ArrayList<>();
        for (int i = 0; i < heads.size(); i++) {
            regex.add(heads.get(i).replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", "\\*").replace("?", "\\?"));
        }

        String regexPattern = "^(" + regex.stream().collect(Collectors.joining("|")) + ")";
        headPattern = Pattern.compile(regexPattern);
    }

    public void generateFollowPattern(ArrayList<String> heads, ArrayList<String> terminals) {
        ArrayList<String> regex = new ArrayList<>();
        for (int i = 0; i < terminals.size(); i++) {
            regex.add(terminals.get(i).replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", "\\*").replace("?", "\\?").replace("[", "\\[").replace("]", "\\]"));

        }

        followRegex = "(?:(?<head>" + heads.stream().collect(Collectors.joining("|")) + ")|(?<terminal>" + regex.stream().collect(Collectors.joining("|")) + "|$))";

    }

    public void generateSplitPattern(ArrayList<String> heads, ArrayList<String> terminals) {
        ArrayList<String> regex = new ArrayList<>();
        for (int i = 0; i < terminals.size(); i++) {
            regex.add(terminals.get(i).replace("(", "\\(").replace(")", "\\)").replace("+", "\\+").replace("*", "\\*").replace("?", "\\?").replace("[", "\\[").replace("]", "\\]"));

        }

        splitRegex = "(?<split>" + heads.stream().collect(Collectors.joining("|")) + "|" + regex.stream().collect(Collectors.joining("|")) + ")";
    }

    public void firstTerminals(ArrayList<String> terminals) {
        terminals.forEach(t -> first.put(t, new HashSet<String>() {{
            add(t);
        }}));
    }

    public void first(ArrayList<AbstractMap.SimpleEntry<String, String>> grammar) {
        Node root;

        for (int i = 0; i < grammar.size(); i++) {
            if (first.containsKey(grammar.get(i).getKey())) {
                continue;
            }

            root = new Node(grammar.get(i).getKey());
            appendLeftProductions(grammar.get(i), root);
            expandTree(root);
            traverseTree(root, new HashSet<String>());
        }
//        System.out.println(first);
    }

    public void first(LinkedHashMap<String, List<List<String>>> grammar) {
        for (Map.Entry<String, List<List<String>>> entry : grammar.entrySet()) {
            for (List<String> body : entry.getValue()) {
                for (int i = 0; i < body.size(); i++) {

                    if (terminals.contains(body.get(i))) {
                        addFirstRule(first, entry.getKey(), body.get(i));
                        break;
                    }

                    if (heads.contains(body.get(i))) {
                        recurseFirst(grammar, body.get(i));
                    }

                    if (heads.contains(body.get(i))) {
                        recurseFirst(grammar, body.get(i));
                        HashSet<String> tmp = new HashSet<String>(first.get(body.get(i)));

                        tmp.remove("epsilon");
                        if (first.containsKey(entry.getKey())) {
                            first.get(entry.getKey()).addAll(tmp);
                        } else {
                            first.put(entry.getKey(), new HashSet<>());
                            first.get(entry.getKey()).addAll(tmp);
                        }

                        if (!first.get(body.get(i)).contains("epsilon")) {
                            break;
                        }
                    }

                }
            }
        }
    }

    public void recurseFirst(LinkedHashMap<String, List<List<String>>> grammar, String head) {


        for (List<String> body : grammar.get(head)) {
            for (int i = 0; i < body.size(); i++) {

                if (terminals.contains(body.get(i))) {
                    addFirstRule(first, head, body.get(i));
//                        set.add(body.get(0));
                    break;
                }

                if (heads.contains(body.get(i))) {
                    recurseFirst(grammar, body.get(i));
                    HashSet<String> tmp = new HashSet<String>(first.get(body.get(i)));
                    tmp.remove("epsilon");
                    if (first.containsKey(head)) {
                        first.get(head).addAll(tmp);
                    } else {
                        first.put(head, new HashSet<>());
                        first.get(head).addAll(tmp);
                    }

                    if (!first.get(body.get(i)).contains("epsilon")) {
                        break;
                    }
                }

            }
        }

    }

    public void addFirstRule(LinkedHashMap<String, HashSet<String>> first, String head, String item) {
        if (first.containsKey(head)) {
            first.get(head).add(item);
        } else {
            first.put(head, new HashSet<>());
            first.get(head).add(item);
        }
    }

    public ArrayList<String> ruleTerminals(AbstractMap.SimpleEntry<String, String> rule) {
        Matcher matcher;
        ArrayList<String> matchGroups = new ArrayList<>();
//        System.out.println("RUL "+rule);
        String rules[] = rule.getValue().split("\\|");
        for (int i = 0; i < rules.length; i++) {
            matcher = terminalPattern.matcher(rules[i]);
            while (matcher.find()) {
                matchGroups.add(matcher.group());
            }
        }
        return matchGroups;
    }

    public void appendLeftProductions(AbstractMap.SimpleEntry<String, String> rule, Node rootNode) {
        Matcher matcher;
        ArrayList<String> matchGroups = new ArrayList<>();
        String rules[] = rule.getValue().split("\\|");
        for (int i = 0; i < rules.length; i++) {
            matcher = headPattern.matcher(rules[i]);
            while (matcher.find()) {
                matchGroups.add(matcher.group());
            }
        }
        matchGroups.forEach(m -> new Node(m, rootNode));
    }

    public void expandTree(Node rootNode) {
        String nodeName;
        for (int i = 0; i < rootNode.getChildren().size(); i++) {
            nodeName = rootNode.getChildren().get(i).getNodeName();
            appendLeftProductions(new AbstractMap.SimpleEntry<String, String>(nodeName, grammarDictionary.get(nodeName)), rootNode.getChildren().get(i));
            expandTree(rootNode.getChildren().get(i));
        }
        nodeName = rootNode.getNodeName();
        rootNode.appendDataList(ruleTerminals(new AbstractMap.SimpleEntry<String, String>(nodeName, grammarDictionary.get(nodeName))));
    }

    public void traverseTree(Node root, HashSet<String> firsts) {
        if (root.hasChildNodes()) {
            for (int i = 0; i < root.getChildren().size(); i++) {
                traverseTree(root.getChildren().get(i), firsts);
            }
            firsts.addAll(root.getData());
        } else {
            firsts.addAll(root.getData());
        }
        first.put(root.getNodeName(), firsts);
    }

    public void follow(ArrayList<AbstractMap.SimpleEntry<String, String>> grammar) {
        HashSet<String> follows;
        for (int i = 0; i < grammar.size(); i++) {
            follows = new HashSet<>();
            if (i == 0) {
                follows.add("$");
            }

            follows.addAll(followExtractor(grammar.get(i).getKey()));
            follows.remove("");
            follows.remove("epsilon");
            follow.put(grammar.get(i).getKey(), follows);
        }

        handleDependencies();
//        System.out.println(follow);
    }

    public void handleDependencies() {
        boolean change = true;
        LinkedHashMap<String, HashSet<String>> followOld;

        while (change ) {
        change = false;
        followOld = (LinkedHashMap<String, HashSet<String>>) follow.clone();
        followDependencies.forEach((su, sb) -> {            //super, sub
//            System.out.println("SUP " + su + " SUB " +  sb);
            sb.forEach(h -> {
                HashSet<String> tmp = new HashSet<>();
                tmp.addAll(follow.get(h));

                if (follow.containsKey(su)) {
                    tmp.addAll(follow.get(su));

                }
                tmp.remove("");
                tmp.remove("epsilon");
                follow.put(su, tmp);
            });
        });

            if (!followOld.equals(follow)){
                change = true;
            }

        }
    }

    public ArrayList<String> followExtractor(String head) {
        ArrayList<String> matchGroups = new ArrayList<>();
        Pattern followPattern = Pattern.compile(head + followRegex);

        grammarDictionary.forEach((h, r) -> {
            Pattern bodyPattern;
            Pattern splittingPattern;
            HashSet<String> firstTmp;
            ArrayList<String> splitsArray = new ArrayList<>();
            String rules[] = r.split("\\|");

            for (int i = 0; i < rules.length; i++) {
                if (followPattern.matcher(rules[i]).find()) {

                    Matcher matcher = followPattern.matcher(rules[i]);
                    if (matcher.find()) {
                        if (matcher.group("head") != null) {
                            firstTmp = first.get(matcher.group("head"));
//                            System.out.println("FIRSTTMP " + firstTmp);
                            matchGroups.addAll(firstTmp);

                            if (firstTmp.contains("epsilon")) {

                                //extracting rest of strings after current
                                bodyPattern = Pattern.compile(head + "(?<body>(?:.+))");
                                String body;
                                Matcher bodyMatcher = bodyPattern.matcher(rules[i]);

                                if (bodyMatcher.find()) {
                                    body = bodyMatcher.group("body");

                                    splittingPattern = Pattern.compile(splitRegex);
                                    Matcher splittingMatcher = splittingPattern.matcher(body);

                                    while (splittingMatcher.find()) {
                                        splitsArray.add(splittingMatcher.group("split"));
                                    }

                                    int j = 0;
                                    for (; j < splitsArray.size(); j++) {

                                        if (grammarDictionary.containsKey(splitsArray.get(j)) && grammarDictionary.get(splitsArray.get(j)).contains("epsilon")) {
                                            matchGroups.addAll(first.get(splitsArray.get(j)));

                                        } else {
                                            break;
                                        }
                                    }

                                    if (j == splitsArray.size() && grammarDictionary.containsKey(splitsArray.get(j - 1)) && grammarDictionary.get(splitsArray.get(j - 1)).contains("epsilon")) {
                                        addDependency(head, h);
                                    }
                                }

                            }

                        }

                        if (matcher.group("terminal") != null) {
                            if (matcher.group("terminal").equals("")) {
                                addDependency(head, h);

                            }
                            matchGroups.add(matcher.group("terminal"));
                        }
                    }
                }
            }
        });

        return matchGroups.stream().filter(m -> !m.equals("epsilon")).collect(Collectors.toCollection(ArrayList::new));
    }

    public void addDependency(String superSet, String subSet) {
        if (subSet.equals(superSet)) {
            return;
        }

        if (followDependencies.containsKey(superSet)) {
            followDependencies.get(superSet).add(subSet);
        } else {
            followDependencies.put(superSet, new ArrayList<String>() {{
                add(subSet);
            }});
        }
    }

    public void write(String path, Boolean append) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(path, append)));

            first.forEach((k, v) -> {
                pw.println("First(" + k + "): " + v);
            });
            follow.forEach((k, v) -> {
                pw.println("Follow(" + k + "): " + v);
            });
            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
