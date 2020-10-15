package io.lateralus.lexergenerator.codegenerator;

import io.lateralus.lexergenerator.core.definition.LexerDefinition;

import java.util.Set;

/**
 * Represents a code codegenerator.
 * @param <T> The type of the properties object
 */
public interface CodeGenerator<T, S> {

	void setProperties(T properties);

	Set<SourceFile<S>> generate(LexerDefinition lexerDefinition) throws CodeGenerationException;
}
