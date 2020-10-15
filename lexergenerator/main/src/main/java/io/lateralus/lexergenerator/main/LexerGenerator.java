package io.lateralus.lexergenerator.main;

import io.lateralus.lexergenerator.codegenerator.CodeGenerationException;
import io.lateralus.lexergenerator.codegenerator.CodeGenerator;
import io.lateralus.lexergenerator.codegenerator.SourceFile;
import io.lateralus.lexergenerator.core.definition.LexerDefinition;
import io.lateralus.lexergenerator.core.definition.LexerDefinitionBuilder;
import io.lateralus.lexergenerator.core.parser.nodes.LexerDescriptionNode;
import io.lateralus.lexergenerator.parser.LexerDescriptionParser;
import io.lateralus.lexergenerator.parser.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

/**
 * Utility class for generating a lexer from a definition and saving this to source files in the specified target
 * directory.
 */
public class LexerGenerator {

	/**
	 * Private constructor since this class is a utility class.
	 */
	private LexerGenerator() {
	}

	/**
	 * Generates a lexer from a definition and saves the resulting source files to the specified target directory.
	 * @param codeGenerator A {@link CodeGenerator} that is used to generate source code from a lexer definition.
	 * @param definitionFile The {@link File} holding the definition of the lexer.
	 * @param properties An object holding properties specific for the generation of the source files.
	 * @param targetDirectory The {@link Path} to the target directory.
	 * @param overwriteExistingFiles {@code boolean} indicating whether or not to overwrite existing source files.
	 * @param <T> The type of the properties file
	 * @throws LexerGeneratorException when an exception occurs in parsing, generating or writing.
	 */
	public static <T> void generate(final CodeGenerator<T, String> codeGenerator, final File definitionFile,
			final T properties, final Path targetDirectory, final boolean overwriteExistingFiles)
			throws LexerGeneratorException {

		final LexerDefinition lexerDefinition = createLexerDef(definitionFile);

		final Set<SourceFile<String>> files = generateSourceFiles(codeGenerator, lexerDefinition, properties);

		save(files, targetDirectory, overwriteExistingFiles);
	}

	private static LexerDefinition createLexerDef(File definitionFile) throws LexerGeneratorException {
		final Reader reader = createFileReader(definitionFile);

		final LexerDescriptionNode node = buildLexerDescriptionNode(reader);

		return new LexerDefinitionBuilder().build(node);
	}

	private static Reader createFileReader(File definitionFile) throws LexerGeneratorException {
		try {
			return new InputStreamReader(new FileInputStream(definitionFile), StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			throw new LexerGeneratorException("Could not open file " + definitionFile.getName(), e);
		}
	}

	private static LexerDescriptionNode buildLexerDescriptionNode(Reader reader) throws LexerGeneratorException {
		try {
			return new LexerDescriptionParser(reader).parse();
		} catch (ParserException e) {
			throw new LexerGeneratorException(e.getMessage(), e);
		}
	}

	private static <T> Set<SourceFile<String>> generateSourceFiles(final CodeGenerator<T, String> codeGenerator,
			final LexerDefinition lexerDefinition, final T properties) throws LexerGeneratorException {
		codeGenerator.setProperties(properties);

		try {
			return codeGenerator.generate(lexerDefinition);
		} catch (CodeGenerationException e) {
			throw new LexerGeneratorException(e.getMessage(), e);
		}
	}

	private static void save(Set<SourceFile<String>> sourceFiles, final Path targetDirectory,
			final boolean overwriteExistingFiles) throws LexerGeneratorException {
		final OpenOption[] openOptions = overwriteExistingFiles
				? new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING }
				: new OpenOption[] { StandardOpenOption.CREATE_NEW };

		for (SourceFile<String> sourceFile : sourceFiles) {
			save(sourceFile, targetDirectory, openOptions);
		}
	}

	private static void save(final SourceFile<String> sourceFile, final Path targetDirectory,
			final OpenOption[] openOptions) throws LexerGeneratorException {

		final Path path = targetDirectory.resolve(Paths.get(sourceFile.getName()));

		try {
			Files.createDirectories(path.getParent());
		} catch (IOException e) {
			throw new LexerGeneratorException("The path '" + path.toString() + "' could not be created.", e);
		}

		try {
			Files.write(path, sourceFile.getContents().getBytes(StandardCharsets.UTF_8), openOptions);
		} catch (FileAlreadyExistsException e) {
			throw new LexerGeneratorException("The file '" + sourceFile.getName() + "' already exists.", e);
		} catch (IOException e) {
			throw new LexerGeneratorException("The file '" + sourceFile.getName() + "' could not be saved.", e);
		}
	}
}
