package org.gertje.regular.codegenerator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.gertje.regular.automaton.Automaton;
import org.gertje.regular.definition.LexerDefinition;

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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicLexerGenerator extends AbstractFreeMarkerCodeGenerator {

	private Map<String, Object> generatorConfig;

	public BasicLexerGenerator(Map<String, Object> generatorConfig) {
		this.generatorConfig = generatorConfig;
	}

	public BasicLexerGenerator(Map<String, Object> generatorConfig, Configuration configuration) {
		super(configuration);
		this.generatorConfig = generatorConfig;
	}

	@Override
	public Collection<SourceFile> generate(LexerDefinition lexerDefinition) throws CodeGenerationException {

		// Create a Set for the source files.
		Set<SourceFile> sourceFiles = new HashSet<>();

		sourceFiles.add(createBasicLexer(lexerDefinition));
		sourceFiles.add(createTokenTypes(lexerDefinition));
		sourceFiles.add(createSourceFile("lexer-exception.ftl", "LexerException.java", createBaseModel()));
		sourceFiles.add(createSourceFile("lexer-reader.ftl", "LexerReader.java", createBaseModel()));
		sourceFiles.add(createSourceFile("lexer-reader-impl.ftl", "LexerReaderImpl.java", createBaseModel()));
		sourceFiles.add(createSourceFile("lexer.ftl", "Lexer.java", createBaseModel()));
		sourceFiles.add(createSourceFile("token.ftl", "Token.java", createBaseModel()));
		return sourceFiles;
	}

	private SourceFile createBasicLexer(LexerDefinition lexerDefinition) throws CodeGenerationException {
		Map<String, Object> model = createBaseModel();
		model.put("startState", lexerDefinition.getDfa().getStartState());
		model.put("errorState", lexerDefinition.getErrorState());
		model.put("transitions", createTransitionsString(lexerDefinition));
		model.put("intervals", createIntervalsString(lexerDefinition));
		model.put("startLexerState", lexerDefinition.getStartLexerState() + 1);
		model.put("stateCount", lexerDefinition.getDfa().getStateCount());
		model.put("alphabetSize", lexerDefinition.getDfa().getAlphabetSize() + 1);
		model.put("isEndState", createIsEndStateString(lexerDefinition));
		model.put("tokenTypes", createTokenTypesString(lexerDefinition));

		return createSourceFile("basic-lexer.ftl", "BasicLexer.java", model);
	}

	private String createTokenTypesString(LexerDefinition lexerDefinition) {
		return IntStream.range(0, lexerDefinition.getDfa().getStateCount())
				.mapToObj(i -> lexerDefinition.getAcceptingStateTokenTypes().get(i))
				.map(t -> t == null ? "null" : "TokenType." + t.getName())
				.collect(Collectors.joining(", "));
	}

	private String createIsEndStateString(LexerDefinition lexerDefinition) {
		return IntStream.range(0, lexerDefinition.getDfa().getStateCount())
				.mapToObj(i -> lexerDefinition.getDfa().getAcceptingStates().contains(i))
				.map(b -> b ? "true" : "false")
				.collect(Collectors.joining(", "));
	}

	private String createTransitionsString(LexerDefinition lexerDefinition) {
		int stateCount = lexerDefinition.getDfa().getStateCount();
		int alphabetSize = lexerDefinition.getDfa().getAlphabetSize();
		int[] resultStateArray = new int[stateCount * (alphabetSize + 1)];

		// Initialize the table here with the error state. Because an input of 0 should lead to the error state.
		for (int i = 0; i < stateCount; i++) {
			resultStateArray[i] = lexerDefinition.getErrorState();
		}

		// Since the automaton is complete, we fill the rest of the table here.
		for (Automaton.Transition t : lexerDefinition.getDfa().getTransitions()) {
			resultStateArray[t.fromState + stateCount * (t.input + 1)] = t.toState;
		}

		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(resultStateArray[0]);

		int last = resultStateArray[0];
		for (int i = 1; i < resultStateArray.length; i++) {
			if (resultStateArray[i] != last) {
				last = resultStateArray[i];
				list.add(1);
				list.add(last);
			} else {
				int j = list.size() - 2;
				list.set(j, list.get(j) + 1);
			}
		}

		return list.stream()
				.map(Objects::toString)
				.collect(Collectors.joining(", "));
	}

	private Object createIntervalsString(LexerDefinition lexerDefinition) {
		int[] intervals = lexerDefinition.getAlphabetIntervals();

		// Fill the array that translates unicode codepoints.
		for (int i = 0; i < intervals.length; i+=2) {
			// Add one to the end, because the value is now the end of the range including, but will be used as a non-
			// including end of the range.
			intervals[i + 1] += 1;
		}

		return Arrays.stream(intervals)
				.mapToObj(Objects::toString)
				.collect(Collectors.joining(", "));
	}

	private SourceFile createTokenTypes(LexerDefinition lexerDefinition) throws CodeGenerationException {
		Map<String, Object> model = createBaseModel();
		model.put("tokenTypes", determineTokenTypes(lexerDefinition));

		return createSourceFile("token-type.ftl", "TokenType.java", model);
	}

	private List<LexerDefinition.TokenType> determineTokenTypes(LexerDefinition lexerDefinition) {
		return lexerDefinition.getTokenTypeList();
	}

	private Map<String, Object> createBaseModel() {
		Map<String, Object> model = new HashMap<>();
		model.put("packageName", generatorConfig.get("packageName"));
		model.put("lexerName", generatorConfig.get("lexerName"));
		return model;
	}

	private SourceFile createSourceFile(String templateName, String sourceFileName, Map<String, Object> model)
			throws CodeGenerationException {

		Template template = determineTemplate(templateName);

		StringWriter writer = new StringWriter();

		try {
			template.process(model, writer);
		} catch (TemplateException | IOException e) {
			throw new CodeGenerationException("Could not process the template:", e);
		}

		String dirName = ((String)generatorConfig.get("packageName")).replaceAll("\\.", File.separator);

		return new SimpleSourceFile(dirName + File.separator + sourceFileName, writer.toString());
	}
}