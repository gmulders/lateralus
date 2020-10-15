package io.lateralus.parsergenerator.codegenerator.simple;

import com.google.common.base.Strings;
import com.google.common.collect.Table;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.lateralus.parsergenerator.codegenerator.CodeGenerationException;
import io.lateralus.parsergenerator.codegenerator.SourceFile;
import io.lateralus.parsergenerator.core.definition.Action;
import io.lateralus.parsergenerator.core.definition.ParserDefinition;
import io.lateralus.parsergenerator.core.definition.State;
import io.lateralus.parsergenerator.core.grammar.Grammar;
import io.lateralus.parsergenerator.core.grammar.NonTerminal;
import io.lateralus.parsergenerator.core.grammar.Production;
import io.lateralus.parsergenerator.core.grammar.Symbol;
import io.lateralus.parsergenerator.core.grammar.Terminal;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
public class BasicParserCodeGenerator extends AbstractFreeMarkerCodeGenerator<BasicParserCodeGenerator.Properties, String> {

	private Properties properties;

	public BasicParserCodeGenerator() {
		super(new ClassTemplateLoader(BasicParserCodeGenerator.class, "/templates"));
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public Set<SourceFile<String>> generate(ParserDefinition parserDefinition) throws CodeGenerationException {
		// Create a Set for the source files.
		Set<SourceFile<String>> sourceFiles = new HashSet<>();

		sourceFiles.addAll(createNodes(parserDefinition));
		sourceFiles.addAll(createVisitor(parserDefinition));
		sourceFiles.addAll(createParser(parserDefinition));

		return sourceFiles;
	}

	private Set<SourceFile<String>> createParser(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile<String>> result = new HashSet<>();

		Map<String, Object> model = createBaseModel();
		result.add(createSourceFile("parser-exception.ftl", "ParserException.java", "", model));

		List<State> stateList = new ArrayList<>(parserDefinition.getCanonicalCollection());
		Map<State, Integer> stateIntegerMap = new HashMap<>();
		for (int i = 0; i < stateList.size(); i++) {
			stateIntegerMap.put(stateList.get(i), i);
		}

		List<Symbol> symbolList = new ArrayList<>(parserDefinition.getOrderedTerminalList());
		symbolList.addAll(parserDefinition.getGrammar().getNonTerminals());
		Map<Symbol, Integer> symbolIntegerMap = new HashMap<>();
		for (int i = 0; i < symbolList.size(); i++) {
			symbolIntegerMap.put(symbolList.get(i), i);
		}

		Map<Production, Integer> productionIntegerMap = new HashMap<>();
		int productionId = 0;
		for (Production production : parserDefinition.getGrammar().getProductions()) {
			productionIntegerMap.put(production, productionId);
			productionId++;
		}

		String actionTableJava = createActionTableJava(parserDefinition, stateList, stateIntegerMap, symbolList, productionIntegerMap);
		model.put("actionTableJava", actionTableJava);
		String productionSizeJava = createProductionSizeJava(parserDefinition.getGrammar().getProductions());
		model.put("productionSizeJava", productionSizeJava);
		String productionNonTerminalIdJava = createNonTerminalIdJava(parserDefinition.getGrammar().getProductions(), symbolIntegerMap);
		model.put("productionNonTerminalIdJava", productionNonTerminalIdJava);
		model.put("reductionList", createReductions(parserDefinition.getGrammar()));
		result.add(createSourceFile("parser.ftl", "Parser.java", "", model));

		return result;
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

	private String createActionTableJava(ParserDefinition parserDefinition, List<State> stateList,
			Map<State, Integer> stateIntegerMap, List<Symbol> symbolList, Map<Production, Integer> productionIntegerMap)
			throws CodeGenerationException {

		Table<State, Terminal, Action> actionTable = parserDefinition.getActionTable();
		Table<State, NonTerminal, State> gotoTable = parserDefinition.getGotoTable();

		int[][] table = new int[stateList.size()][symbolList.size()];

		for (int i = 0; i < stateList.size(); i++) {
			State state = stateList.get(i);
			for (int j = 0; j < symbolList.size(); j++) {
				Symbol symbol = symbolList.get(j);
				int value;
				if (symbol.isTerminal()) {
					Action action = actionTable.get(state, symbol);
					value = determineActionValue(action, productionIntegerMap, stateIntegerMap);
				} else {
					State nextState = gotoTable.get(state, symbol);
					value = determineGotoValue(nextState, stateIntegerMap);
				}
				table[i][j] = value;
			}
		}

		StringBuilder builder = new StringBuilder("new int[][] {{");
		String tableContent = Arrays.stream(table)
				.map(row -> Arrays.stream(row)
						.mapToObj(String::valueOf)
						.collect(Collectors.joining(","))
				).collect(Collectors.joining("},{"));
		builder.append(tableContent);
		builder.append("}}");
		return builder.toString();
	}

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
				return Integer.MIN_VALUE;
			case REDUCE:
				return productionIntegerMap.get(action.getProduction()) + 1;
			case SHIFT:
				return ~stateIntegerMap.get(action.getState());
		}
		throw new CodeGenerationException("Unknown action type");
	}

	private Set<SourceFile<String>> createNodes(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile<String>> result = new HashSet<>();

		for (Node node : createNodeDefinitions(parserDefinition)) {
			Map<String, Object> model = createBaseModel();
			model.put("node", node);
			String fileName = node.className + ".java";
			if (!node.isAbstract) {
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
			reductionList.add(new Reduction(index, isCast, getNodeTypeName(production), determineParameters(production)));
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

	private Set<SourceFile<String>> createVisitor(ParserDefinition parserDefinition) throws CodeGenerationException {
		Set<SourceFile<String>> result = new HashSet<>();
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

	private SourceFile<String> createSourceFile(String templateName, String sourceFileName, String subPackage, Map<String, Object> model)
			throws CodeGenerationException {

		Template template = determineTemplate(templateName);

		StringWriter writer = new StringWriter();

		try {
			template.process(model, writer);
		} catch (TemplateException | IOException e) {
			throw new CodeGenerationException("Could not process the template:", e);
		}

		String packageName = properties.getParserPackageName();
		if (subPackage != null) {
			packageName += "." + subPackage;
		}
		String dirName = packageName.replaceAll("\\.", File.separator);

		return new SimpleSourceFile(dirName + File.separator + sourceFileName, writer.toString());
	}

	private Set<Node> createNodeDefinitions(ParserDefinition parserDefinition) {
		Grammar grammar = parserDefinition.getGrammar();
		Collection<Production> productions = grammar.getProductions();

		Set<Node> nodes = new HashSet<>();

		for (NonTerminal nonTerminal : grammar.getNonTerminals()) {

			String baseName = "BaseNode";
			for (Production production : productions) {
				if (production.getRhs().size() == 1
						&& production.getRhs().get(0) == nonTerminal) {
					baseName = getNodeTypeName(production);
					break;
				}
			}

			// Voeg eerst een abstracte node toe met de naam van de NonTerminal.
			nodes.add(new Node(nonTerminal.getName() + "Node", baseName, true, false, null, null));
			baseName = nonTerminal.getName() + "Node";

			Set<Production> nonTerminalProductions = grammar.getProductions(nonTerminal);
			// Voeg voor elke een productie bij deze non-terminal een node toe.
			for (Production production : nonTerminalProductions) {
				// Skip producties waarvoor geldt dat de rhs == 1 en rhs(0).isNonTerminal
				if (production.getRhs().size() == 1 && !production.getRhs().get(0).isTerminal()) {
					continue;
				}

				nodes.add(new Node(
						getNodeTypeName(production),
						baseName,
						false,
						production.isBinary(),
						determineParameters(production),
						determineFirstTokenName(production)));
			}
		}

		return nodes;
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
			return upperUnderscoreToLowerCamel(name);
		}
		return upperCamelToLowerCamel(name);
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
				String rhsName = production.getRhsNames().get(i);
				return upperUnderscoreToLowerCamel(rhsName != null ? rhsName : symbol.getName());
			}
		}
		return production.getRhsNames().get(0) + "Node.getToken()";
	}

	private static String upperCamelToLowerCamel(String name) {
		return UPPER_CAMEL.to(LOWER_CAMEL, name);
	}

	private static String upperUnderscoreToLowerCamel(String name) {
		return UPPER_UNDERSCORE.to(LOWER_CAMEL, name);
	}

	private static String toUpperCamel(String name) {
		return LOWER_CAMEL.to(UPPER_CAMEL, name);
	}

	public static class Properties {
		final private String parserName;
		final private String parserPackageName;
		final private String lexerPackageName;

		public Properties(String parserName, String parserPackageName, String lexerPackageName) {
			this.parserName = parserName;
			this.parserPackageName = parserPackageName;
			this.lexerPackageName = lexerPackageName;
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

	public static class Node {
		private final String className;
		private final String baseClassName;
		private final boolean isAbstract;
		private final boolean isBinaryNode;
		private final List<Parameter> parameterList;
		private final String firstTokenName;

		public Node(String className, String baseClassName, boolean isAbstract, boolean isBinaryNode,
				List<Parameter> parameterList, String firstTokenName) {
			this.className = className;
			this.baseClassName = baseClassName;
			this.isAbstract = isAbstract;
			this.isBinaryNode = isBinaryNode;
			this.parameterList = parameterList;
			this.firstTokenName = firstTokenName;
		}

		public String getClassName() {
			return className;
		}

		public String getBaseClassName() {
			return baseClassName;
		}

		public boolean getIsAbstract() {
			return isAbstract;
		}

		public boolean getIsBinaryNode() {
			return isBinaryNode;
		}

		public List<Parameter> getParameterList() {
			return parameterList;
		}

		public String getFirstTokenName() {
			return firstTokenName;
		}
	}

	public static class Parameter {
		private final String name;
		private final String type;

		public Parameter(String name, String type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getNameUpperCase() {
			return toUpperCamel(name);
		}

		public String getType() {
			return type;
		}
	}

	public static class Reduction {
		private final int productionId;
		private final boolean isCast;
		private final String nodeType;
		private final List<Parameter> parameterList;

		public Reduction(int productionId, boolean isCast, String nodeType, List<Parameter> parameterList) {
			this.productionId = productionId;
			this.isCast = isCast;
			this.nodeType = nodeType;
			this.parameterList = parameterList;
		}

		public int getProductionId() {
			return productionId;
		}

		public boolean getIsCast() {
			return isCast;
		}

		public String getNodeType() {
			return nodeType;
		}

		public List<Parameter> getParameterList() {
			return parameterList;
		}
	}
}
