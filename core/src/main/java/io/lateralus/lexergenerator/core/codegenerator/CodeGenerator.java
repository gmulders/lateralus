package io.lateralus.lexergenerator.core.codegenerator;

import io.lateralus.lexergenerator.core.definition.LexerDefinition;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a code generator.
 */
public interface CodeGenerator {
	Collection<SourceFile> generate(LexerDefinition lexerDefinition, Map<String, Object> generatorConfig)
			throws CodeGenerationException;
}
