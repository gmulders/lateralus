package io.lateralus.parsergenerator.main;

import io.lateralus.parsergenerator.codegenerator.BasicParserCodeGenerator;
import io.lateralus.parsergenerator.codegenerator.BasicParserCodeGenerator.Properties;
import io.lateralus.shared.generator.GeneratorException;
import io.lateralus.shared.generator.SourceFileSaver;

import java.nio.file.Path;

public class Main {

	public static void main(String[] args) throws GeneratorException {
		SourceFileSaver sourceFileSaver = new SourceFileSaver(Path.of("parsergenerator/main/src/test-out/java/"), true);
		ParserGenerator parserGenerator = new ParserGenerator(sourceFileSaver);

		// This grammar string does not work yet; we need to add some sort of annotation so that we know what the name
		// for the node that we will generate should be.
		Properties properties = new Properties("Simple", "test.parser", "test.lexer");
		parserGenerator.generate(new BasicParserCodeGenerator(properties), Path.of("test.grammar").toFile());
	}
}
