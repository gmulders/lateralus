package io.lateralus.parsergenerator.parser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import io.lateralus.parsergenerator.core.grammar.Grammar;
import io.lateralus.parsergenerator.core.grammar.GrammarException;
import io.lateralus.parsergenerator.core.grammar.NonTerminal;
import io.lateralus.parsergenerator.core.grammar.Symbol;
import io.lateralus.parsergenerator.core.grammar.Terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.lateralus.parsergenerator.core.grammar.Terminal.EPSILON;

public class GrammarParser {

	private static final Pattern SYMBOL_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z_0-9]*)(\\(([a-z][a-zA-Z0-9]*)\\))?");
	private static final Splitter PRODUCTION_SPLITTER = Splitter.on(Pattern.compile("->")).trimResults().omitEmptyStrings();
	private static final Splitter PRODUCTION_OR_SPLITTER = Splitter.on(Pattern.compile("\\|")).trimResults().omitEmptyStrings();
	private static final Splitter RHS_SPLITTER = Splitter.on(":").trimResults().omitEmptyStrings();
	private static final Splitter SYMBOL_SPLITTER = Splitter.on(CharMatcher.whitespace()).trimResults().omitEmptyStrings();
	public static final String EXPECTED_SYNTAX = "Expect <TERMINAL> -> <SYMBOL>* (: <NODE> (binary)?)? (| <SYMBOL>* (: <NODE> (binary)?)?)*";

	private GrammarParser() {
	}

	private static boolean isValidSymbol(String symbol) {
		Matcher matcher = SYMBOL_PATTERN.matcher(symbol);
		return matcher.matches();
	}

	private static Symbol convertToSymbol(String symbol, Set<String> nonTerminals) {
		if (symbol.equals(EPSILON.getName())) {
			return EPSILON;
		}
		if (nonTerminals.contains(symbol)) {
			return new NonTerminal(symbol);
		}
		return new Terminal(symbol);
	}

	public static Grammar from(String grammar) throws GrammarParserException {
		// Note the use of LinkedHashSet to maintain the insertion order.
		Set<String> nonTerminals = new LinkedHashSet<>();
		Map<String, List<RhsDef>> productionMap = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new StringReader(grammar))) {
			String line;
			while ((line = reader.readLine()) != null) {
				List<String> parts = PRODUCTION_SPLITTER.splitToList(line);
				if (parts.size() != 2) {
					throw new GrammarParserException("Expect <TERMINAL> -> <SYMBOL>* (: <NODE> (binary)?)? (| <SYMBOL>* (: <NODE> (binary)?)?)*");
				}
				String lhs = parts.get(0);

				List<RhsDef> rhsDef = new ArrayList<>();
				for(String rhs : PRODUCTION_OR_SPLITTER.split(parts.get(1))) {
					rhsDef.add(parseRhsDef(rhs));
				}

				nonTerminals.add(lhs);
				List<RhsDef> currentRhsDef = productionMap.computeIfAbsent(lhs, a -> new ArrayList<>());
				currentRhsDef.addAll(rhsDef);
			}
		} catch (IOException e) {
			throw new GrammarParserException("Could not parse the grammar: \n" + grammar + "\n\n");
		}

		Grammar.Builder builder = Grammar.builder();

		for (String lhs : nonTerminals) {
			if (!isValidSymbol(lhs)) {
				throw new GrammarParserException("The symbol '" + lhs + "' is not a valid symbol");
			}
			NonTerminal nonTerminal = new NonTerminal(lhs);
			for (RhsDef rhsDef : productionMap.get(lhs)) {
				List<Symbol> symbols = new ArrayList<>();
				for (String symbolName : rhsDef.symbols) {
					symbols.add(convertToSymbol(symbolName, nonTerminals));
				}
				builder.addProduction(nonTerminal, symbols, rhsDef.symbolNames, rhsDef.nodeName, rhsDef.isBinary);
			}
		}

		try {
			return builder.build();
		} catch (GrammarException e) {
			throw new GrammarParserException(e.getMessage(), e);
		}
	}

	private static RhsDef parseRhsDef(String rhs) throws GrammarParserException {
		List<String> rhsParts = RHS_SPLITTER.splitToList(rhs);
		if (rhsParts.size() != 1 && rhsParts.size() != 2) {
			throw new GrammarParserException(EXPECTED_SYNTAX);
		}

		List<String> symbolsDefs = SYMBOL_SPLITTER.splitToList(rhsParts.get(0));

		List<String> symbols = new ArrayList<>();
		List<String> symbolNames = new ArrayList<>();

		for (String symbolDef : symbolsDefs) {
			Matcher matcher = SYMBOL_PATTERN.matcher(symbolDef);
			if (!matcher.matches()) {
				throw new GrammarParserException("The symbol '" + symbolDef + "' is not a valid symbol");
			}
			symbols.add(matcher.group(1));
			symbolNames.add(matcher.group(3));
		}

		if (rhsParts.size() == 1) {
			return new RhsDef(symbols, symbolNames, null, false);
		}

		String nodeName = rhsParts.get(1);
		boolean isBinary = nodeName.contains("binary");
		if (isBinary) {
			nodeName = nodeName.replace("binary", "");
		}
		nodeName = nodeName.strip();

		return new RhsDef(symbols, symbolNames, nodeName, isBinary);
	}

	private static class RhsDef {
		private final List<String> symbols;
		private final List<String> symbolNames;
		private final String nodeName;
		private final boolean isBinary;

		private RhsDef(List<String> symbols, List<String> symbolNames, String nodeName, boolean isBinary) {
			this.symbols = symbols;
			this.symbolNames = symbolNames;
			this.nodeName = nodeName;
			this.isBinary = isBinary;
		}
	}
}
