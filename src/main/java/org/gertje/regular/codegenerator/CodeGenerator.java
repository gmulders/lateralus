package org.gertje.regular.codegenerator;

import org.gertje.regular.definition.LexerDefinition;

import java.util.Collection;

public interface CodeGenerator {

	Collection<SourceFile> generate(LexerDefinition lexerDefinition) throws CodeGenerationException;
}
