package io.lateralus.shared.generator;

import io.lateralus.shared.codegenerator.CodeGenerationException;
import io.lateralus.shared.codegenerator.CodeGenerator;
import io.lateralus.shared.codegenerator.SourceFile;

import java.io.File;
import java.util.Set;

/**
 * Utility class for generating a lexer from a definition and saving this to source files in the specified target
 * directory.
 */
public abstract class Generator<Definition> {

	private final SourceFileSaver sourceFileSaver;

	protected Generator(final SourceFileSaver sourceFileSaver) {
		this.sourceFileSaver = sourceFileSaver;
	}

	/**
	 * Generates something from a definition and saves the resulting source files to the specified target directory.
	 * @param codeGenerator A {@link CodeGenerator} that is used to generate source code from a lexer definition.
	 * @param definitionFile The {@link File} holding the definition of the lexer.
	 * @throws GeneratorException when an exception occurs in parsing, generating or writing.
	 */
	public void generate(final CodeGenerator<Definition> codeGenerator, final File definitionFile)
			throws GeneratorException {

		final Set<SourceFile> files = generateSourceFiles(codeGenerator, createDefinition(definitionFile));

		sourceFileSaver.save(files);
	}

	protected abstract Definition createDefinition(File definitionFile) throws GeneratorException;

	private Set<SourceFile> generateSourceFiles(final CodeGenerator<Definition> codeGenerator,
			final Definition definition) throws GeneratorException {

		try {
			return codeGenerator.generate(definition);
		} catch (CodeGenerationException e) {
			throw new GeneratorException(e.getMessage(), e);
		}
	}
}
