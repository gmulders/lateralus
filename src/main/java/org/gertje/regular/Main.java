package org.gertje.regular;

import org.gertje.regular.codegenerator.BasicLexerGenerator;
import org.gertje.regular.codegenerator.CodeGenerationException;
import org.gertje.regular.codegenerator.CodeGenerator;
import org.gertje.regular.codegenerator.SourceFile;
import org.gertje.regular.definition.LexerDefinition;
import org.gertje.regular.definition.builder.LexerDefinitionBuilder;
import org.gertje.regular.parser.RegExException;
import org.gertje.regular.parser.description.ParserException;

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

	public static void main(String[] args) throws CodeGenerationException, ParserException {
		new Main().run();
	}

	public void run() throws CodeGenerationException, ParserException {

		final LexerDefinition lexerDefinition = createLexerDef();

		CodeGenerator generator = createCodeGenerator();

		Map<String, Object> generatorConfig = new HashMap<>();
		generatorConfig.put("packageName", "org.gertje.regular.parser.description.lexer");
		generatorConfig.put("lexerName", "BaseLexerDescription");

		Collection<SourceFile> sourceFiles = generator.generate(lexerDefinition, generatorConfig);

		for (SourceFile sourceFile : sourceFiles) {
			try {
				save(sourceFile);
			} catch (IOException e) {
				throw new CodeGenerationException("The file could not be saved: " + sourceFile.getName(), e);
			}
		}
	}

	private CodeGenerator createCodeGenerator() {
		return new BasicLexerGenerator();
	}

	private void save(SourceFile sourceFile) throws IOException {
		Path path = Paths.get("target/generated-sources/main/" + sourceFile.getName());
		Files.createDirectories(path.getParent());
		Files.write(path, sourceFile.getContents().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
	}

	private LexerDefinition createLexerDef() throws ParserException {
		try {
			return new LexerDefinitionBuilder()
					.lexerStartStateName("DEFAULT")
					.startLexerClass("DEFAULT")
						.addLexerToken("NEW_LINE", "\\r\\n|\\r|\\n", "DEFAULT")
						.addLexerToken("WHITE_SPACE", "( |\\t)+", "DEFAULT")
						.addLexerToken("LEXER_CLASS", "([a-zA-Z_])[a-zA-Z0-9_]*", "DEFAULT")
						.addLexerToken("TOKEN_NAME", "<([a-zA-Z_])[a-zA-Z0-9_]*>", "DEFAULT")
						.addLexerToken("REGEX_START", "'", "REGEX")
						.addLexerToken("NEXT_STATE", "->", "DEFAULT")
					.end()
					.startLexerClass("REGEX")
						.addLexerToken("REGEX_END", "'", "DEFAULT")
						.addLexerToken("REGEX_PART", "[^'\\\\]*", "REGEX")
						.addLexerToken("REGEX_SINGLE_QUOTE", "\\\\\\'", "REGEX")
						.addLexerToken("REGEX_DOUBLE_BACKSLASH", "\\\\\\\\", "REGEX")
						.addLexerToken("REGEX_BACKSLASH", "\\\\", "REGEX")
					.end()
					.build();
		} catch (RegExException e) {
			throw new ParserException("An exception occurred during parsing.", e);
		}
	}
}
