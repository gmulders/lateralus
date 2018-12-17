package io.lateralus.lexergenerator.generator;

import io.lateralus.lexergenerator.core.codegenerator.SourceFile;

/**
 * Straightforward implementation of a {@link SourceFile}.
 */
public class SimpleSourceFile implements SourceFile {

	private String name;

	private String contents;

	public SimpleSourceFile(String name, String contents) {
		this.name = name;
		this.contents = contents;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
}
