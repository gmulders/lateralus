package org.gertje.regular.codegenerator;

/**
 * Exception that can be thrown from the code generation.
 */
public class CodeGenerationException extends Exception {

	public CodeGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
