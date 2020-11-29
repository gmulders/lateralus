package io.lateralus.parsergenerator.main;

import com.google.common.io.CharStreams;
import io.lateralus.parsergenerator.core.definition.ParserDefinition;
import io.lateralus.parsergenerator.core.definition.ParserDefinitionBuilder;
import io.lateralus.parsergenerator.core.definition.ParserDefinitionException;
import io.lateralus.parsergenerator.core.definition.closer.Closer;
import io.lateralus.parsergenerator.core.definition.closer.KnuthCloser;
import io.lateralus.parsergenerator.core.grammar.Grammar;
import io.lateralus.parsergenerator.core.grammar.Symbol;
import io.lateralus.parsergenerator.core.grammar.Terminal;
import io.lateralus.parsergenerator.parser.GrammarParser;
import io.lateralus.parsergenerator.parser.GrammarParserException;
import io.lateralus.shared.generator.Generator;
import io.lateralus.shared.generator.GeneratorException;
import io.lateralus.shared.generator.SourceFileSaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParserGenerator extends Generator<ParserDefinition> {

	protected ParserGenerator(SourceFileSaver sourceFileSaver) {
		super(sourceFileSaver);
	}

	@Override
	protected ParserDefinition createDefinition(File definitionFile) throws GeneratorException {
		final Reader reader = createFileReader(definitionFile);

		final Grammar grammar = buildGrammar(reader);

		Closer closer = new KnuthCloser(grammar);
		List<Terminal> orderedTerminalList = grammar.getProductions().stream()
				.flatMap(production -> production.getRhs().stream())
				.filter(Symbol::isTerminal)
				.map(symbol -> (Terminal)symbol)
				.distinct()
				.collect(Collectors.toCollection(() -> new ArrayList<>(Collections.singletonList(Terminal.EOF))));

		try {
			return new ParserDefinitionBuilder(closer).build(grammar, orderedTerminalList);
		} catch (ParserDefinitionException e) {
			throw new GeneratorException("Could not build the parser definition", e);
		}
	}

	private static Reader createFileReader(File definitionFile) throws GeneratorException {
		try {
			return new InputStreamReader(new FileInputStream(definitionFile), StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			throw new GeneratorException("Could not open file " + definitionFile.getName(), e);
		}
	}

	private static Grammar buildGrammar(Reader reader) throws GeneratorException {
		try {
			return GrammarParser.from(CharStreams.toString(reader));
		} catch (GrammarParserException | IOException e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}
}
