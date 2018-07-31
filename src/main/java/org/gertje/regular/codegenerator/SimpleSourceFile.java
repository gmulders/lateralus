package org.gertje.regular.codegenerator;

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
