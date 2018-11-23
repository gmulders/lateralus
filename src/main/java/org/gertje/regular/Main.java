package org.gertje.regular;

import org.gertje.regular.codegenerator.BasicLexerGenerator;
import org.gertje.regular.codegenerator.CodeGenerationException;
import org.gertje.regular.codegenerator.CodeGenerator;
import org.gertje.regular.codegenerator.SourceFile;
import org.gertje.regular.definition.LexerDefinition;
import org.gertje.regular.parser.description.LexerDescriptionParser;
import org.gertje.regular.parser.description.ParserException;
import org.gertje.regular.parser.nodes.LexerDescriptionNode;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
		Reader reader = new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("description.lex"));
		LexerDescriptionNode node = new LexerDescriptionParser(reader).parse();
		return new org.gertje.regular.definition.LexerDefinitionBuilder().build(node);
	}
}
