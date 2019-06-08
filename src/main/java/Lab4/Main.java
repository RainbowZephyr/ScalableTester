package Lab4;

import AbstractClasses.Grammar;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

//        FirstAndFollow.parseGrammar("Sample4.in");
//
//        ParseTable.generateTable(FirstAndFollow.grammarDictionary,FirstAndFollow.heads, FirstAndFollow.terminals, FirstAndFollow.first, FirstAndFollow.follow, "SAMPLE4TABLE.out");
//
//        Parser.readFile("Input1.in",
//                FirstAndFollow.grammarDictionary,
//                ParseTable.table,
//                FirstAndFollow.heads,
//                FirstAndFollow.terminals);

        String[] s = {"S : b S'", "S' : a S' | epsilon"};


        FirstAndFollow ff = new FirstAndFollow("","");
        ff.parseGrammar(Grammar.readGrammar(Arrays.asList(s)));

        ParseTable table = new ParseTable(ff);

        table.generateTable();

        System.out.println("Table " + table.table);

        Parser parser = new Parser(table);

        String[] input = {"b", "a", "a", "$"};

        System.out.println(parser.parseInput(input));


    }
}
