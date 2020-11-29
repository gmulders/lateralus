package io.lateralus.lexergenerator.main;

import io.lateralus.lexergenerator.core.definition.LexerDefinition;
import io.lateralus.lexergenerator.core.definition.LexerDefinitionBuilder;
import io.lateralus.lexergenerator.core.parser.nodes.LexerDescriptionNode;
import io.lateralus.lexergenerator.parser.LexerDescriptionParser;
import io.lateralus.lexergenerator.parser.ParserException;
import io.lateralus.shared.generator.Generator;
import io.lateralus.shared.generator.GeneratorException;
import io.lateralus.shared.generator.SourceFileSaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for generating a lexer from a definition and saving this to source files in the specified target
 * directory.
 */
public class LexerGenerator extends Generator<LexerDefinition> {

	public LexerGenerator(SourceFileSaver sourceFileSaver) {
		super(sourceFileSaver);
	}

	@Override
	protected LexerDefinition createDefinition(File definitionFile) throws GeneratorException {
		final Reader reader = createFileReader(definitionFile);

		final LexerDescriptionNode node = buildLexerDescriptionNode(reader);

		return new LexerDefinitionBuilder().build(node);
	}

	private static Reader createFileReader(File definitionFile) throws GeneratorException {
		try {
			return new InputStreamReader(new FileInputStream(definitionFile), StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			throw new GeneratorException("Could not open file " + definitionFile.getName(), e);
		}
	}

	private static LexerDescriptionNode buildLexerDescriptionNode(Reader reader) throws GeneratorException {
		try {
			return new LexerDescriptionParser(reader).parse();
		} catch (ParserException e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}
}
