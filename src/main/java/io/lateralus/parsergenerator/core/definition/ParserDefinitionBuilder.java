package io.lateralus.parsergenerator.core.definition;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import io.lateralus.parsergenerator.core.Action;
import io.lateralus.parsergenerator.core.definition.closer.Closer;
import io.lateralus.parsergenerator.core.Grammar;
import io.lateralus.parsergenerator.core.Item;
import io.lateralus.parsergenerator.core.NonTerminal;
import io.lateralus.parsergenerator.core.Production;
import io.lateralus.parsergenerator.core.State;
import io.lateralus.parsergenerator.core.Symbol;
import io.lateralus.parsergenerator.core.Terminal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds a parser definition ({@link ParserDefinition}) given a grammar ({@link Grammar}).
 */
public class ParserDefinitionBuilder {

	private final Closer closer;

	public ParserDefinitionBuilder(Closer closer) {
		this.closer = closer;
	}

	public ParserDefinition build(Grammar grammar, List<Terminal> orderedTerminalList)
			throws ParserDefinitionException {

		// Take the grammar and determine the canonical collection
		LinkedHashSet<State> canonicalCollection = createCanonicalCollection(grammar, closer);

		// From the canonical collection we create the goto table and the action table.
		Table<State, NonTerminal, State> gotoTable = buildGotoTable(canonicalCollection);
		Table<State, Terminal, Action> actionTable = buildActionTable(canonicalCollection);

		return new ParserDefinition(grammar, canonicalCollection, actionTable, gotoTable, orderedTerminalList);
	}

	private static Table<State, Terminal, Action> buildActionTable(Set<State> canonicalCollection)
			throws ParserDefinitionException {

		Table<State, Terminal, Action> actionTable = HashBasedTable.create();
		for (State state : canonicalCollection) {

			for (Symbol symbol : state.getTransitions().keySet()) {
				if (symbol.isTerminal()) {
					updateActionTable(actionTable, state, (Terminal)symbol,
							Action.shift(state.getTransitions().get(symbol)));
				}
			}

			for (Item item : state.getItems()) {
				if (item.getExpectedSymbol() != null) {
					continue;
				}

				if (item.getProduction().getLhs() == NonTerminal.START) {
					updateActionTable(actionTable, state, Terminal.EOF, Action.accept());
				} else {
					updateActionTable(actionTable, state, item.getLookahead(), Action.reduce(item.getProduction()));
				}
			}
		}
		return actionTable;
	}

	/**
	 * Adds the given action to the action table for a single state and a set of terminals.
	 *
	 * @param actionTable The action table to update
	 * @param state The state to represent the row of the table to add the action for
	 * @param terminals A set of terminals to represent the columns to add the action for
	 * @param action The action to add
	 */
	private static void updateActionTable(Table<State, Terminal, Action> actionTable, State state,
			Set<Terminal> terminals, Action action) throws ParserDefinitionException {

		for (Terminal terminal : terminals) {
			updateActionTable(actionTable, state, terminal, action);
		}
	}

	/**
	 * Adds the given action to the action table for a single state and a single terminal.
	 *
	 * @param actionTable The action table to update
	 * @param state The state to represent the row of the table to add the action for
	 * @param terminal A terminal to represent the columns to add the action for
	 * @param action The action to add
	 */
	private static void updateActionTable(Table<State, Terminal, Action> actionTable, State state, Terminal terminal,
			Action action) throws ParserDefinitionException {

		Action currentAction = actionTable.get(state, terminal);
		if (currentAction != null) {
			throw new ParserDefinitionException("Grammar leads to a " + currentAction.getActionType() + "-" +
					action.getActionType() + " conflict. State: " + state + ", terminal: " + terminal);
		}
		actionTable.put(state, terminal, action);
	}

	protected static Table<State, NonTerminal, State> buildGotoTable(Set<State> canonicalCollection) {
		Table<State, NonTerminal, State> gotoTable = HashBasedTable.create();

		for (State state : canonicalCollection) {
			for (Map.Entry<Symbol, State> entry : state.getTransitions().entrySet()) {
				if (entry.getKey().isTerminal()) {
					continue;
				}
				gotoTable.put(state, (NonTerminal)entry.getKey(), entry.getValue());
			}
		}

		return gotoTable;
	}

	/**
	 * Calculates the canonical collection.
	 *
	 * The method returns a {@link LinkedHashSet}, because that set implementation maintains the insertion order. This
	 * is only important because we need to know the start state.
	 *
	 * @param grammar The {@link Grammar} to calculate the canonical collection of
	 * @param closer A {@link Closer} that calculates the closure of a set of items.
	 * @return a {@link LinkedHashSet} containing {@link State}s of which the first element is the start state.
	 */
	protected static LinkedHashSet<State> createCanonicalCollection(Grammar grammar, Closer closer) {
		LinkedHashSet<State> canonicalCollection = new LinkedHashSet<>();
		Map<State, State> internedStates = new HashMap<>();

		Deque<State> workList = new ArrayDeque<>();

		// Determine the state from which to start
		State startState = new State(closer.closure(createStartKernel(grammar)));
		internedStates.put(startState, startState);
		canonicalCollection.add(startState);
		workList.push(startState);

		while (!workList.isEmpty()) {
			State currentState = workList.pop();

			// Find all new kernels
			SetMultimap<Symbol, Item> kernels = HashMultimap.create();
			for (Item item : currentState.getItems()) {
				Symbol expectedSymbol = item.getExpectedSymbol();
				if (expectedSymbol != null) {
					kernels.put(expectedSymbol, item);
				}
			}

			for (Symbol expectedSymbol : kernels.keySet()) {
				Set<Item> kernelBase = kernels.get(expectedSymbol);

				// Create new items for the kernel (increase the dot pointer).
				Set<Item> kernel = kernelBase.stream()
						.map(item -> new Item(item.getProduction(), item.getLookahead(), item.getPosition() + 1))
						.collect(Collectors.toSet());

				// Create the next state by taking the closure of the kernel
				State nextState = internedStates.computeIfAbsent(new State(closer.closure(kernel)), Function.identity());

				if (canonicalCollection.add(nextState)) {
					workList.push(nextState);
				}
				currentState.getTransitions().put(expectedSymbol, nextState);
			}
		}
		return canonicalCollection;
	}

	private static Set<Item> createStartKernel(Grammar grammar) {
		Set<Production> startProductions = grammar.getProductions(grammar.getSentenceSymbol());
		// Note that while we do a stream here and collect to a set, we only expect the set of "start productions" to
		// contain one element.
		return startProductions.stream()
				.map(startProduction -> new Item(startProduction, Set.of(Terminal.EOF), 0))
				.collect(Collectors.toSet());
	}
}
