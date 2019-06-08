package Lab1;

import java.util.ArrayList;

public class RegexPostToNFA {

	static int stateCounter = 0;
	static String startState;
	static String finalState;
	static ArrayList<String> allSymbols;
	static ArrayList<String> allOperators;

	private static void RegexPostToNFAStartUp() {
		stateCounter = 0;
		startState = "";
		finalState = "";
		allSymbols = new ArrayList<String>();
		allOperators = new ArrayList<String>();
		allOperators.add(".");
		allOperators.add("|");
		allOperators.add("?");
		allOperators.add("*");
		allOperators.add("+");
	}

	public static ExpressionContainer constructNFAFromRegex(String postRegex) {

		RegexPostToNFAStartUp();

		ArrayList<ExpressionContainer> stack = new ArrayList<ExpressionContainer>();

		for (int i = 0; i < postRegex.length(); i++) {
			String s = postRegex.charAt(i) + "";

			if (!allOperators.contains(s)) {
				if (!allSymbols.contains(s)) {
					allSymbols.add(s);
				}
				stack.add(symbol(s));
				continue;
			}

			ExpressionContainer ec2;
			ExpressionContainer ec1;

			switch (s) {
			case ".":
				ec2 = stack.remove(stack.size() - 1);
				ec1 = stack.remove(stack.size() - 1);
				stack.add(concatinate(ec1, ec2));
				break;
			case "|":
				ec2 = stack.remove(stack.size() - 1);
				ec1 = stack.remove(stack.size() - 1);
				stack.add(or(ec1, ec2));
				break;
			case "+":
				ec1 = stack.remove(stack.size() - 1);
				stack.add(plus(ec1));
				break;
			case "*":
				ec1 = stack.remove(stack.size() - 1);
				stack.add(star(ec1));
				break;
			case "?":
				ec1 = stack.remove(stack.size() - 1);
				stack.add(question(ec1));
				break;
			default:
				System.out.println("Error not valid symbol");
				return null;
			}
		}

		ExpressionContainer ecResult = stack.remove(stack.size() - 1);
		ecResult.allSymbols = allSymbols;

		return ecResult;

	}

	private static ExpressionContainer symbol(String sym) {
		String initState = "q" + stateCounter++;
		String finalState = "q" + stateCounter++;

		if (sym.equals("ε")) {
			sym = " ";
		}

		String tran = initState + ", " + sym + "," + finalState;
		ArrayList<String> states = new ArrayList<String>();
		states.add(initState);
		states.add(finalState);
		ArrayList<String> trans = new ArrayList<String>();
		trans.add(tran);
		return new ExpressionContainer(initState, finalState, sym, states, trans);
	}

	private static ExpressionContainer concatinate(ExpressionContainer ec1, ExpressionContainer ec2) {

		ArrayList<String> states = new ArrayList<String>();
		for (String state : ec1.states) {
			states.add(state);
		}

		for (String state : ec2.states) {
			if (!state.equals(ec2.startState)) {
				states.add(state);
			}
		}

		ArrayList<String> trans = new ArrayList<String>();

		for (String tran : ec1.transitions) {
			trans.add(tran);
		}

		for (String tran : ec2.transitions) {
			trans.add(tran.replace(ec2.startState, ec1.finalState));
		}

		return new ExpressionContainer(ec1.startState, ec2.finalState, ec1.exp + ec2.exp + ".", states, trans);
	}

	private static ExpressionContainer or(ExpressionContainer ec1, ExpressionContainer ec2) {

		String initState = "q" + stateCounter++;
		String finalState = "q" + stateCounter++;

		String tran1 = initState + ", , " + ec1.startState;
		String tran2 = initState + ", , " + ec2.startState;
		String tran3 = ec1.finalState + ", , " + finalState;
		String tran4 = ec2.finalState + ", , " + finalState;

		ArrayList<String> states = new ArrayList<String>();
		for (String state : ec1.states) {
			states.add(state);
		}

		for (String state : ec2.states) {
			states.add(state);
		}

		states.add(initState);
		states.add(finalState);

		ArrayList<String> trans = new ArrayList<String>();

		for (String tran : ec1.transitions) {
			trans.add(tran);
		}
		for (String tran : ec2.transitions) {
			trans.add(tran);
		}
		trans.add(tran1);
		trans.add(tran2);
		trans.add(tran3);
		trans.add(tran4);

		String exp = ec1.exp + ec2.exp + "|";

		return new ExpressionContainer(initState, finalState, exp, states, trans);

	}

	private static ExpressionContainer plus(ExpressionContainer ec) {

		ArrayList<String> states = new ArrayList<String>();
		ArrayList<String> newStates = new ArrayList<String>();

		for (String state : ec.states) {
			String s = "q" + stateCounter++;
			newStates.add(s);
			states.add(state);
		}

		for (String state : newStates) {
			states.add(state);
		}

		String finalState = "q" + stateCounter++;
		states.add(finalState);

		ArrayList<String> trans = new ArrayList<String>();
		ArrayList<String> newtrans = new ArrayList<String>();

		for (String tran : ec.transitions) {
			trans.add(tran);

			String s1 = tran.substring(0, tran.indexOf(','));
			int indexS1 = ec.states.indexOf(s1);
			String s2 = tran.substring(tran.lastIndexOf(',') + 1);
			int indexS2 = ec.states.indexOf(s2);

			tran = tran.replace(s1, newStates.get(indexS1));
			tran = tran.replace(s2, newStates.get(indexS2));

			newtrans.add(tran);

		}

		for (String tran : newtrans) {
			trans.add(tran);
		}

		String tran1 = ec.finalState + ", , " + ec.startState;
		String tran2 = ec.finalState + ", , " + finalState;
		String tran3 = newStates.get(newStates.size() - 1) + ", , " + ec.startState;
		String tran4 = newStates.get(newStates.size() - 1) + ", , " + finalState;

		trans.add(tran1);
		trans.add(tran2);
		trans.add(tran3);
		trans.add(tran4);

		int indexS3 = ec.states.indexOf(ec.startState);

		return new ExpressionContainer(newStates.get(indexS3), finalState, ec.exp + "+", states, trans);

	}

	private static ExpressionContainer star(ExpressionContainer ec) {

		String initState = "q" + stateCounter++;
		String finalState = "q" + stateCounter++;

		String tran1 = initState + ", , " + finalState;
		String tran2 = initState + ", , " + ec.startState;
		String tran3 = ec.finalState + ", , " + ec.startState;
		String tran4 = ec.finalState + ", , " + finalState;

		ArrayList<String> states = new ArrayList<String>();
		for (String state : ec.states) {
			states.add(state);
		}

		states.add(initState);
		states.add(finalState);

		ArrayList<String> trans = new ArrayList<String>();

		for (String tran : ec.transitions) {
			trans.add(tran);
		}

		trans.add(tran1);
		trans.add(tran2);
		trans.add(tran3);
		trans.add(tran4);

		return new ExpressionContainer(initState, finalState, ec.exp + "*", states, trans);
	}

	private static ExpressionContainer question(ExpressionContainer ec) {
		String initState = "q" + stateCounter++;
		String trans1 = "q" + stateCounter++;
		String trans2 = "q" + stateCounter++;
		String finalState = "q" + stateCounter++;

		String tran1 = initState + ", , " + ec.startState;
		String tran2 = initState + ", , " + trans1;
		String tran3 = ec.finalState + ", , " + finalState;
		String tran4 = trans1 + ", , " + trans2;
		String tran5 = trans2 + ", , " + finalState;

		ArrayList<String> states = new ArrayList<String>();
		for (String state : ec.states) {
			states.add(state);
		}

		states.add(initState);
		states.add(trans1);
		states.add(trans2);
		states.add(finalState);

		ArrayList<String> trans = new ArrayList<String>();

		for (String tran : ec.transitions) {
			trans.add(tran);
		}

		trans.add(tran1);
		trans.add(tran2);
		trans.add(tran3);
		trans.add(tran4);
		trans.add(tran5);

		return new ExpressionContainer(initState, finalState, ec.exp + "?", states, trans);
	}

}
