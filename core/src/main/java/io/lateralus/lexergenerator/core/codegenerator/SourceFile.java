package io.lateralus.lexergenerator.core.codegenerator;

/**
 * Represents a source file that can be generated.
 */
public interface SourceFile {

	String getName();

	String getContents();
}
