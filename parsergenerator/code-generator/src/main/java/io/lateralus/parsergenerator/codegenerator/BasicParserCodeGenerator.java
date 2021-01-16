package io.lateralus.parsergenerator.codegenerator;

import com.google.common.base.Strings;
import com.google.common.collect.Table;
import freemarker.cache.ClassTemplateLoader;
import io.lateralus.parsergenerator.codegenerator.model.Node;
import io.lateralus.parsergenerator.codegenerator.model.Parameter;
import io.lateralus.parsergenerator.codegenerator.model.Reduction;
import io.lateralus.parsergenerator.core.definition.Action;
import io.lateralus.parsergenerator.core.definition.ParserDefinition;
import io.lateralus.parsergenerator.core.definition.State;
import io.lateralus.parsergenerator.core.grammar.Grammar;
import io.lateralus.parsergenerator.core.grammar.NonTerminal;
import io.lateralus.parsergenerator.core.grammar.Production;
import io.lateralus.parsergenerator.core.grammar.Symbol;
import io.lateralus.parsergenerator.core.grammar.Terminal;
import io.lateralus.shared.codegenerator.ByteArraySourceFile;
import io.lateralus.shared.codegenerator.CodeGenerationException;
import io.lateralus.shared.codegenerator.SourceFile;
import io.lateralus.shared.codegenerator.freemarker.AbstractFreeMarkerCodeGenerator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static java.util.function.Predicate.not;

/**
 * Generates a basic Java parser from a parser definition and a configuration.
 */
public class BasicParserCodeGenerator extends AbstractFreeMarkerCodeGenerator<ParserDefinition> {

	private final Properties properties;

	public BasicParserCodeGenerator(Properties properties) {
		super(new ClassTemplateLoader(BasicParserCodeGenerator.class, "/templates"));
		this.properties = properties;
	}

	@Override
	public Set<SourceFile> generate(ParserDefinition parserDefinition) throws CodeGenerationException {
		// Create a Set for the source files.
		Set<SourceFile> sourceFiles = new HashSet<>();

		sourceFiles.addAll(createNodes(parserDefinition));
		sourceFiles.addAll(createVisitor(parserDefinition));
		sourceFiles.addAll(createParser(parserDefinition));
		sourceFiles.addAll(createParserTable(parserDefinition));

		return sourceFiles;
	}

	private Set<SourceFile> createParser(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile> result = new HashSet<>();

		Map<String, Object> model = createBaseModel();
		result.add(createSourceFile("parser-exception.ftl", "ParserException.java", "", model));
		result.add(createSourceFile("parser-syntax-exception.ftl", "ParserSyntaxException.java", "", model));

		List<Symbol> symbolList = new ArrayList<>(parserDefinition.getOrderedTerminalList());
		symbolList.addAll(parserDefinition.getGrammar().getNonTerminals());
		Map<Symbol, Integer> symbolIntegerMap = new HashMap<>();
		for (int i = 0; i < symbolList.size(); i++) {
			symbolIntegerMap.put(symbolList.get(i), i);
		}

		String productionSizeJava = createProductionSizeJava(parserDefinition.getGrammar().getProductions());
		model.put("productionSizeJava", productionSizeJava);
		String productionNonTerminalIdJava = createNonTerminalIdJava(parserDefinition.getGrammar().getProductions(), symbolIntegerMap);
		model.put("productionNonTerminalIdJava", productionNonTerminalIdJava);
		model.put("reductionList", createReductions(parserDefinition.getGrammar()));
		model.put("tableWidth", symbolList.size());
		result.add(createSourceFile("parser.ftl", "Parser.java", "", model));

		return result;
	}

	private Set<SourceFile> createParserTable(ParserDefinition parserDefinition) throws CodeGenerationException {
		List<State> stateList = new ArrayList<>(parserDefinition.getCanonicalCollection());
		Map<State, Integer> stateIntegerMap = new HashMap<>();
		for (int i = 0; i < stateList.size(); i++) {
			stateIntegerMap.put(stateList.get(i), i);
		}

		List<Symbol> symbolList = new ArrayList<>(parserDefinition.getOrderedTerminalList());
		symbolList.addAll(parserDefinition.getGrammar().getNonTerminals());

		Map<Production, Integer> productionIntegerMap = new HashMap<>();
		int productionId = 0;
		for (Production production : parserDefinition.getGrammar().getProductions()) {
			productionIntegerMap.put(production, productionId);
			productionId++;
		}

		Table<State, Terminal, Action> actionTable = parserDefinition.getActionTable();
		Table<State, NonTerminal, State> gotoTable = parserDefinition.getGotoTable();

		int height = stateList.size();
		int width = symbolList.size();

		int[] table = new int[height * width];

		for (int i = 0; i < height; i++) {
			State state = stateList.get(i);
			for (int j = 0; j < width; j++) {
				Symbol symbol = symbolList.get(j);
				int value;
				if (symbol.isTerminal()) {
					Action action = actionTable.get(state, symbol);
					value = determineActionValue(action, productionIntegerMap, stateIntegerMap);
				} else {
					State nextState = gotoTable.get(state, symbol);
					value = determineGotoValue(nextState, stateIntegerMap);
				}
				table[i * width + j] = value;
			}
		}

		ByteArrayOutputStream baos = encodeTable(table, height, width);

		String name = properties.generatedResourcesDirectory + File.separator + packageDirectoryName(null) +
				File.separator + "parser.table";
		SourceFile sourceFile = new ByteArraySourceFile(name, baos.toByteArray());

		return Set.of(sourceFile);
	}

	private ByteArrayOutputStream encodeTable(int[] table, int height, int width) {
		// To have a minimal form of compression of the table, we will encode repeating zero's as a special character
		// followed by the number of repetitions. Skip cells are repeated over entire columns. This is not helping for
		// the compression, since we store the table per row. Especially for large tables with many zero's the
		// likelihood that such a column interrupts a range of zero's is large. So storing the skip cells costs per
		// state 1 byte for the skip value plus 2 bytes to start a new zero range. Especially for large tables with
		// thousands of states this adds up!
		// To fix this problem we will set these to 0 and add extra information at the back of the byte array to encode
		// for these cells, but since entire columns have the same value we only need to store the column numbers.
		List<Integer> skipColumns = new ArrayList<>();
		for (int i = 0; i < width; i++) {
			if (table[i] == 2) {
				skipColumns.add(i);
				for (int j = 0; j < height; j++) {
					table[j * width + i] = 0;
				}
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encode(height, baos);
		encode(width, baos);
		ArraySplitter arraySplitter = new ArraySplitter(table);
		while (arraySplitter.hasNext()) {
			ArraySplitter.Item item = arraySplitter.next();
			if (item.count == 1) {
				encode(item.value, baos);
			} else {
				baos.write(0x80); // Gaat dit goed?
				encode(item.count, baos);
			}
		}
		encode(skipColumns.size(), baos);
		for (int skipColumn : skipColumns) {
			encode(skipColumn, baos);
		}
		return baos;
	}

	public static void encode(int value, ByteArrayOutputStream out) {
		value = (value << 1) ^ (value >> 31);
		if(value > 0x0FFFFFFF || value < 0) out.write((byte)(0x80 | ((value >>> 28))));
		if(value > 0x1FFFFF || value < 0)   out.write((byte)(0x80 | ((value >>> 21) & 0x7F)));
		if(value > 0x3FFF || value < 0)     out.write((byte)(0x80 | ((value >>> 14) & 0x7F)));
		if(value > 0x7F || value < 0)       out.write((byte)(0x80 | ((value >>>  7) & 0x7F)));
		out.write((byte)(value & 0x7F));
	}

	private String createProductionSizeJava(Collection<Production> productions) {
		String sizes = productions.stream()
				.map(Production::getRhs)
				.map(List::size)
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		return "new int[] {" + sizes + "}";
	}

	private String createNonTerminalIdJava(Collection<Production> productions, Map<Symbol, Integer> symbolIntegerMap) {
		String ids = productions.stream()
				.map(Production::getLhs)
				.map(symbolIntegerMap::get)
				.map(String::valueOf)
				.collect(Collectors.joining(","));

		return "new int[] {" + ids + "}";
	}

//	private String createActionTableJava(ParserDefinition parserDefinition, List<State> stateList,
//			Map<State, Integer> stateIntegerMap, List<Symbol> symbolList, Map<Production, Integer> productionIntegerMap)
//			throws CodeGenerationException {
//
//		Table<State, Terminal, Action> actionTable = parserDefinition.getActionTable();
//		Table<State, NonTerminal, State> gotoTable = parserDefinition.getGotoTable();
//
//		int[][] table = new int[stateList.size()][symbolList.size()];
//
//		for (int i = 0; i < stateList.size(); i++) {
//			State state = stateList.get(i);
//			for (int j = 0; j < symbolList.size(); j++) {
//				Symbol symbol = symbolList.get(j);
//				int value;
//				if (symbol.isTerminal()) {
//					Action action = actionTable.get(state, symbol);
//					value = determineActionValue(action, productionIntegerMap, stateIntegerMap);
//				} else {
//					State nextState = gotoTable.get(state, symbol);
//					value = determineGotoValue(nextState, stateIntegerMap);
//				}
////				System.out.println(i + ": " + symbol + " --> " + value);
//				table[i][j] = value;
//			}
//		}
//
//		StringBuilder builder = new StringBuilder("new int[][] {{");
//		String tableContent = Arrays.stream(table)
//				.map(row -> Arrays.stream(row)
//						.mapToObj(String::valueOf)
//						.collect(Collectors.joining(","))
//				).collect(Collectors.joining("},{"));
//		builder.append(tableContent);
//		builder.append("}}");
//		return builder.toString();
//	}

	private int determineGotoValue(State nextState, Map<State, Integer> stateIntegerMap) {
		if (nextState == null) {
			return 0;
		}
		return stateIntegerMap.get(nextState);
	}

	private int determineActionValue(Action action, Map<Production, Integer> productionIntegerMap,
			Map<State, Integer> stateIntegerMap) throws CodeGenerationException {

		if (action == null) {
			return 0;
		}
		switch (action.getActionType()) {
			case ACCEPT:
				return 1;
			case SKIP:
				return 2;
			case REDUCE:
				return productionIntegerMap.get(action.getProduction()) + 3;
			case SHIFT:
				return ~stateIntegerMap.get(action.getState());
		}
		throw new CodeGenerationException("Unknown action type");
	}

	private Set<SourceFile> createNodes(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile> result = new HashSet<>();

		for (Node node : createNodeDefinitions(parserDefinition)) {
			Map<String, Object> model = createBaseModel();
			model.put("node", node);
			String fileName = node.getClassName() + ".java";
			if (!node.getIsAbstract()) {
				result.add(createSourceFile("type-node.ftl", fileName, "nodes", model));
			} else {
				result.add(createSourceFile("abstract-node.ftl", fileName, "nodes", model));
			}
		}

		result.add(createSourceFile("node.ftl", "Node.java", "nodes", createBaseModel()));
		result.add(createSourceFile("base-node.ftl", "BaseNode.java", "nodes", createBaseModel()));
		result.add(createSourceFile("binary-operation-node.ftl", "BinaryOperationNode.java", "nodes", createBaseModel()));
		return result;
	}

	private List<Reduction> createReductions(Grammar grammar) {
		List<Reduction> reductionList = new ArrayList<>();
		int index = 0;
		for (Production production : grammar.getProductions()) {
			boolean isCast = production.getRhs().size() == 1 && !production.getRhs().get(0).isTerminal();
			reductionList.add(new Reduction(index, isCast, getNodeTypeName(production), isCast ? null : determineParameters(production)));
			index++;
		}

		return reductionList;
	}

	private String getNodeTypeName(Production production) {
		String nodeType = production.getNodeName();
		if (nodeType == null) {
			nodeType = production.getLhs().getName();
		}
		return nodeType + "Node";
	}

	private Set<SourceFile> createVisitor(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile> result = new HashSet<>();
		result.add(createSourceFile("visiting-exception.ftl", "VisitingException.java", "visitor", createBaseModel()));

		Map<String, Object> model = createBaseModel();
		model.put("nodeTypes", createNodeTypeList(parserDefinition));
		result.add(createSourceFile("node-visitor.ftl", "NodeVisitor.java", "visitor", model));

		return result;
	}

	private List<String> createNodeTypeList(ParserDefinition parserDefinition) {
		return createNodeDefinitions(parserDefinition).stream()
				.filter(not(Node::getIsAbstract))
				.map(Node::getClassName)
				.collect(Collectors.toList());
	}

	private Map<String, Object> createBaseModel() {
		Map<String, Object> model = new HashMap<>();
		model.put("parserPackageName", properties.getParserPackageName());
		model.put("lexerPackageName", properties.getLexerPackageName());
		model.put("parserName", properties.getParserName());
		return model;
	}

	@Override
	protected SourceFile createSourceFile(String templateName, String sourceFileName, String subPackage,
			Map<String, Object> model) throws CodeGenerationException {
		String dirName = properties.generatedSourcesDirectory + File.separator + packageDirectoryName(subPackage);
		return super.createSourceFile(templateName, sourceFileName, dirName, model);
	}

	private String packageDirectoryName(String subPackage) {
		return packageName(subPackage).replaceAll("\\.", File.separator);
	}

	private String packageName(String subPackage) {
		if (subPackage == null) {
			return properties.getParserPackageName();
		}
		return properties.getParserPackageName() + "." + subPackage;
	}

	private Set<Node> createNodeDefinitions(ParserDefinition parserDefinition) {
		Grammar grammar = parserDefinition.getGrammar();
		Set<Node> nodes = new HashSet<>();

		for (NonTerminal nonTerminal : grammar.getNonTerminals()) {
			String nodeName = nonTerminal.getName() + "Node";

			// Voeg eerst een abstracte node toe met de naam van de NonTerminal.
			nodes.add(new Node(nodeName, findParentNodeName(parserDefinition, nonTerminal), true, false, null, null));

			Set<Production> nonTerminalProductions = grammar.getProductions(nonTerminal);
			// Voeg voor elke een productie bij deze non-terminal een node toe.
			for (Production production : nonTerminalProductions) {
				// Skip producties waarvoor geldt dat de rhs == 1 en rhs(0).isNonTerminal
				if (production.getRhs().size() == 1 && !production.getRhs().get(0).isTerminal()) {
					continue;
				}

				nodes.add(new Node(
						getNodeTypeName(production),
						nodeName,
						false,
						production.isBinary(),
						determineParameters(production),
						determineFirstTokenName(production)));
			}
		}

		return nodes;
	}

	private String findParentNodeName(ParserDefinition parserDefinition, NonTerminal nonTerminal) {
		Collection<Production> productions = parserDefinition.getGrammar().getProductions();
		for (Production production : productions) {
			if (production.getRhs().size() == 1
					&& production.getRhs().get(0) == nonTerminal) {
				return getNodeTypeName(production);
			}
		}
		return "BaseNode";
	}

	private List<Parameter> determineParameters(Production production) {
		List<Parameter> result = new ArrayList<>();
		for (int i = 0; i < production.getRhs().size(); i++) {
			Symbol symbol = production.getRhs().get(i);
			String rhsName = production.getRhsNames().get(i);
			result.add(new Parameter(determineParamName(symbol, rhsName), determineTypeName(symbol)));
		}
		return result;
	}

	private String determineParamName(Symbol symbol, String overriddenName) {
		String name = overriddenName;
		if (Strings.isNullOrEmpty(name)) {
			name = symbol.getName();
		}
		if (symbol.isTerminal()) {
			return UPPER_UNDERSCORE.to(LOWER_CAMEL, name);
		}
		return UPPER_CAMEL.to(LOWER_CAMEL, name);
	}

	private String determineTypeName(Symbol symbol) {
		if (symbol.isTerminal()) {
			return "Token";
		}
		return symbol.getName() + "Node";
	}

	private String determineFirstTokenName(Production production) {
		for (int i = 0; i < production.getRhs().size(); i++) {
			Symbol symbol = production.getRhs().get(i);
			if (symbol.isTerminal()) {
				return determineParamName(symbol, production.getRhsNames().get(i));
			}
		}
		return determineParamName(production.getRhs().get(0), production.getRhsNames().get(0)) + ".getToken()";
	}

	public static class Properties {
		final private String parserName;
		final private String parserPackageName;
		final private String lexerPackageName;
		final private String generatedSourcesDirectory;
		final private String generatedResourcesDirectory;

		public Properties(String parserName, String parserPackageName, String lexerPackageName,
				String generatedSourcesDirectory, String generatedResourcesDirectory) {
			this.parserName = parserName;
			this.parserPackageName = parserPackageName;
			this.lexerPackageName = lexerPackageName;
			this.generatedSourcesDirectory = generatedSourcesDirectory;
			this.generatedResourcesDirectory = generatedResourcesDirectory;
		}

		public String getParserName() {
			return parserName;
		}

		public String getParserPackageName() {
			return parserPackageName;
		}

		public String getLexerPackageName() {
			return lexerPackageName;
		}
	}


	private static class ArraySplitter implements Iterator<ArraySplitter.Item> {
		private final int[] array;
		private int index = 0;

		public ArraySplitter(int[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return array.length > index;
		}

		@Override
		public Item next() {
			if (array[index] != 0) {
				return new Item(array[index++], 1);
			}
			int start = index;
			while (array.length > index && array[index] == 0) {
				index++;
			}
			return new Item(0, index - start);
		}

		private static class Item {
			private final int value;
			private final int count;

			private Item(int value, int count) {
				this.value = value;
				this.count = count;
			}
		}
	}
}
