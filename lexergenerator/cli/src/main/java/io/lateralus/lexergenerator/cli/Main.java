package io.lateralus.lexergenerator.cli;

import io.lateralus.lexergenerator.codegenerator.simple.BasicLexerCodeGenerator;
import io.lateralus.lexergenerator.codegenerator.simple.BasicLexerCodeGenerator.Properties;
import io.lateralus.lexergenerator.main.LexerGenerator;
import io.lateralus.lexergenerator.main.LexerGeneratorException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;

@Command(description = {
			"Generates a lexer from the given lexer DEFINTION and emits Java code representing that lexer into the " +
					"TARGET directory.",
		},
		name = "lexgen")
public class Main implements Runnable {

	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
	private boolean usageHelpRequested;

	@Option(names = {"-f", "--force"}, description = "Overwrite files if they already exist")
	private boolean force;

	@Parameters(index = "0", paramLabel = "DEFINITION", description = "The file holding the lexer definition")
	private File definitionFile;

	@Parameters(index = "1", paramLabel = "NAME", description = "The name of the lexer class")
	private String lexerName;

	@Parameters(index = "2", paramLabel = "PACKAGE",
			description = "The Java-package name for the files. e.g. 'io.lateralus.lex'")
	private String packageName;

	@Parameters(index = "3", paramLabel = "TARGET", description = "Target directory")
	private Path targetDirectory;

	public static void main(String[] args) {
		CommandLine.run(new Main(), args);
	}

	@Override
	public void run() {
		try {
			LexerGenerator.generate(new BasicLexerCodeGenerator(), definitionFile,
					new Properties(lexerName, packageName), targetDirectory, force);
		} catch (LexerGeneratorException e) {
			System.err.println("Error while generating the lexer: " + e.getMessage());
		}
	}
}
