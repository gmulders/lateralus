package io.lateralus.shared.codegenerator;

import java.util.Set;

/**
 * Represents a code codegenerator.
 * @param <Definition> The type of the definition to build the generator from
 */
public interface CodeGenerator<Definition> {
	Set<SourceFile> generate(Definition parserDefinition) throws CodeGenerationException;
}
