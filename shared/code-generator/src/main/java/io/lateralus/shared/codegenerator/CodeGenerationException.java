package io.lateralus.shared.codegenerator;

/**
 * Exception that can be thrown from the code generator.
 */
public class CodeGenerationException extends Exception {

	public CodeGenerationException(String message) {
		super(message);
	}

	public CodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
