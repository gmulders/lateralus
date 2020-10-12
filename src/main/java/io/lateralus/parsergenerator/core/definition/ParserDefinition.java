package io.lateralus.parsergenerator.core.definition;

import com.google.common.collect.Table;
import io.lateralus.parsergenerator.core.grammar.Grammar;
import io.lateralus.parsergenerator.core.grammar.NonTerminal;
import io.lateralus.parsergenerator.core.grammar.Terminal;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Abstract representation of a parser. This is the output of the parser generator and the input for the code generator.
 */
public class ParserDefinition {

	private final Grammar grammar;

	private final LinkedHashSet<State> canonicalCollection;

	private final Table<State, Terminal, Action> actionTable;

	private final Table<State, NonTerminal, State> gotoTable;

	private final List<Terminal> orderedTerminalList;

	public ParserDefinition(Grammar grammar, LinkedHashSet<State> canonicalCollection,
			Table<State, Terminal, Action> actionTable, Table<State, NonTerminal, State> gotoTable,
			List<Terminal> orderedTerminalList) {
		this.grammar = grammar;
		this.canonicalCollection = canonicalCollection;
		this.actionTable = actionTable;
		this.gotoTable = gotoTable;
		this.orderedTerminalList = orderedTerminalList;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	public LinkedHashSet<State> getCanonicalCollection() {
		return canonicalCollection;
	}

	public Table<State, Terminal, Action> getActionTable() {
		return actionTable;
	}

	public Table<State, NonTerminal, State> getGotoTable() {
		return gotoTable;
	}

	public List<Terminal> getOrderedTerminalList() {
		return orderedTerminalList;
	}
}
