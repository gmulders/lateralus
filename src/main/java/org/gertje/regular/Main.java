package org.gertje.regular;

import org.gertje.regular.codegenerator.BasicLexerGenerator;
import org.gertje.regular.codegenerator.CodeGenerationException;
import org.gertje.regular.codegenerator.CodeGenerator;
import org.gertje.regular.codegenerator.SourceFile;
import org.gertje.regular.definition.LexerDefinition;
import org.gertje.regular.definition.builder.LexerDefinitionBuilder;
import org.gertje.regular.parser.RegExException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) throws IOException, CodeGenerationException {
		new Main().run();
	}

	public void run() throws IOException, CodeGenerationException {

		final LexerDefinition lexerDefinition;
		try {
			lexerDefinition = createLexerDef();
		} catch (RegExException e) {
			throw new CodeGenerationException("Could not build lexer definition:", e);
		}

		CodeGenerator generator = createCodeGenerator();

		Map<String, Object> generatorConfig = new HashMap<>();
		generatorConfig.put("packageName", "org.gertje.regular.lexer");
		generatorConfig.put("lexerName", "LexerDefinition");

		Collection<SourceFile> sourceFiles = generator.generate(lexerDefinition, generatorConfig);

		for (SourceFile sourceFile : sourceFiles) {
			save(sourceFile);
		}
	}

	private CodeGenerator createCodeGenerator() {
		return new BasicLexerGenerator();
	}

	private void save(SourceFile sourceFile) throws IOException {
//		System.out.println(sourceFile.getName());
//		System.out.println();
//		System.out.println();
//		System.out.print(sourceFile.getContents());

		Path path = Paths.get("target/generated-sources/main/" + sourceFile.getName());
		Files.createDirectories(path.getParent());


		Files.write(path,
				sourceFile.getContents().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
	}

	private LexerDefinition createLexerDef() throws RegExException {
		return new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("WHITE_SPACE", "( |\\t)+", "DEFAULT")
					.addLexerToken("LEXER_CLASS", "([a-zA-Z_])[a-zA-Z0-9_]*", "DEFAULT")
					.addLexerToken("TOKEN_NAME", "<([a-zA-Z_])[a-zA-Z0-9_]*>", "DEFAULT")
					.addLexerToken("REGEX_START", "'", "REGEX")
					.addLexerToken("NEXT_STATE", "->", "DEFAULT")
				.end()
				.startLexerClass("REGEX")
					.addLexerToken("REGEX_END", "'", "DEFAULT")
					.addLexerToken("REGEX_PART", "[^'\\\\]*", "REGEX")
					.addLexerToken("REGEX_SINGLE_QUOTE", "\\\\'", "REGEX")
					.addLexerToken("REGEX_TAB", "\\\\t", "REGEX")
					.addLexerToken("REGEX_NEW_LINE", "\\\\n", "REGEX")
					.addLexerToken("REGEX_CARRIAGE_RETURN", "\\\\r", "REGEX")
					.addLexerToken("REGEX_BACKSLASH", "\\\\\\\\", "REGEX")
				.end()
				.build();
	}

	private LexerDefinition createSimpleExpressionLexer() throws RegExException {
		return new LexerDefinitionBuilder()
				.lexerStartStateName("DEFAULT")
				.startLexerClass("DEFAULT")
					.addLexerToken("WHITE_SPACE", "( |\\t)+", "DEFAULT")
					.addLexerToken("END_OF_EXPRESSION", ";", "DEFAULT")
					.addLexerToken("NEW_LINE", "\\r\\n|\\r|\\n", "DEFAULT")
					.addLexerToken("DATE_START", "D'", "DATE")
					.addLexerToken("BOOLEAN", "true|false", "DEFAULT")
					.addLexerToken("IDENTIFIER", "([a-zA-Z_])[a-zA-Z0-9_]*", "DEFAULT")
					.addLexerToken("LEFT_PARENTHESIS", "\\(", "DEFAULT")
					.addLexerToken("RIGHT_PARENTHESIS", ")", "DEFAULT")
					.addLexerToken("LEFT_BRACKET", "{", "DEFAULT")
					.addLexerToken("RIGHT_BRACKET", "}", "DEFAULT")
					.addLexerToken("STRING_START", "'", "STRING")
					.addLexerToken("DECIMAL", "([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)", "DEFAULT")
					.addLexerToken("INTEGER", "[0-9]+", "DEFAULT")
					.addLexerToken("BOOLEAN_AND", "&&", "DEFAULT")
					.addLexerToken("BOOLEAN_OR", "[|][|]", "DEFAULT")
					.addLexerToken("PLUS", "+", "DEFAULT")
					.addLexerToken("MINUS", "-", "DEFAULT")
					.addLexerToken("POWER", "^", "DEFAULT")
					.addLexerToken("MULTIPLY", "*", "DEFAULT")
					.addLexerToken("DIVIDE", "/", "DEFAULT")
					.addLexerToken("PERCENT", "%", "DEFAULT")
					.addLexerToken("EQ", "==", "DEFAULT")
					.addLexerToken("NEQ", "!=", "DEFAULT")
					.addLexerToken("LEQ", "<=", "DEFAULT")
					.addLexerToken("LT", "<", "DEFAULT")
					.addLexerToken("GEQ", ">=", "DEFAULT")
					.addLexerToken("GT", ">", "DEFAULT")
					.addLexerToken("NOT", "!", "DEFAULT")
					.addLexerToken("ASSIGNMENT", "=", "DEFAULT")
					.addLexerToken("IF", "?", "DEFAULT")
					.addLexerToken("COLON", ":", "DEFAULT")
					.addLexerToken("COMMA", ",", "DEFAULT")
				.end()
				.startLexerClass("STRING")
					.addLexerToken("STRING_PART", "[^'\\\\]*", "STRING")
					.addLexerToken("STRING_END", "'", "DEFAULT")
					.addLexerToken("STRING_SINGLE_QUOTE", "\\\\'", "STRING")
					.addLexerToken("STRING_TAB", "\\\\t", "STRING")
					.addLexerToken("STRING_NEW_LINE", "\\\\n", "STRING")
					.addLexerToken("STRING_CARRIAGE_RETURN", "\\\\r", "STRING")
					.addLexerToken("STRING_BACKSLASH", "\\\\\\\\", "STRING")
				.end()
				.startLexerClass("DATE")
					.addLexerToken("DATE_PART", "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]", "DATE")
					.addLexerToken("DATE_END", "'", "DEFAULT")
				.end()
				.build();
	}

}
