package io.lateralus.lexergenerator.maven;

import io.lateralus.lexergenerator.codegenerator.simple.BasicLexerCodeGenerator;
import io.lateralus.lexergenerator.codegenerator.simple.BasicLexerCodeGenerator.Properties;
import io.lateralus.lexergenerator.main.LexerGenerator;
import io.lateralus.lexergenerator.main.LexerGeneratorException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "generate-lexer", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class LexerGeneratorMojo extends AbstractMojo {

	/**
	 * Boolean indicating whether existing sources should be overwritten. Defaults to false.
	 */
	@Parameter(property = "overwrite-existing", defaultValue = "false")
	private boolean overwriteExisting;

	/**
	 * The file that contains the definition.
	 */
	@Parameter(property = "definition", required = true)
	private File definitionFile;

	/**
	 * The name for the lexer.
	 */
	@Parameter(property = "name", required = true)
	private String lexerName;

	/**
	 * The package to place the lexer in.
	 */
	@Parameter(property = "package", required = true)
	private String packageName;

	/**
	 * The target directory. Defaults to "${project.build.directory}/generated-sources/lexer".
	 */
	@Parameter(property = "target", defaultValue = "${project.build.directory}/generated-sources/lexer", required = true)
	private File targetDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		getLog().info("Generating sources from: '" + definitionFile + "' to '" + targetDirectory + "'.");

		try {
			LexerGenerator.generate(new BasicLexerCodeGenerator(), definitionFile,
					new Properties(lexerName, packageName), targetDirectory.toPath(), overwriteExisting);
		} catch (LexerGeneratorException e) {
			throw new MojoFailureException("The lexer could not be generated.", e);
		}
	}
}
