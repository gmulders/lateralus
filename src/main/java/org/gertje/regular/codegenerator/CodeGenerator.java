package org.gertje.regular.codegenerator;

import org.gertje.regular.definition.LexerDefinition;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a code generator.
 */
public interface CodeGenerator {
	Collection<SourceFile> generate(LexerDefinition lexerDefinition, Map<String, Object> generatorConfig)
			throws CodeGenerationException;
}
