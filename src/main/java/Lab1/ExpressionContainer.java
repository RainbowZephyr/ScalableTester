package Lab1;

import java.util.ArrayList;

public class ExpressionContainer {

	String startState;
	String finalState;
	String exp;
	ArrayList<String> allSymbols;
	ArrayList<String> states;
	ArrayList<String> transitions;

	public ExpressionContainer(String startState, String finalState, String exp, ArrayList<String> states, ArrayList<String> trans) {
		this.startState = startState;
		this.finalState = finalState;
		this.exp = exp;
		this.states = states;
		transitions = trans;
	}

}
