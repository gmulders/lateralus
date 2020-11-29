package io.lateralus.shared.codegenerator;

/**
 * Represents a source file that can be generated.
 */
public interface SourceFile {

	String getName();

	byte[] getContents();
}
