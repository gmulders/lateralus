package io.lateralus.shared.codegenerator;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Straightforward implementation of a {@link SourceFile}.
 */
public class SimpleSourceFile implements SourceFile {

	private final String name;

	private final String contents;

	private final Charset encoding;

	public SimpleSourceFile(final String name, final String contents) {
		this(name, contents, UTF_8);
	}

	public SimpleSourceFile(final String name, final String contents, Charset encoding) {
		this.name = name;
		this.contents = contents;
		this.encoding = encoding;
	}

	@Override
	public String toString() {
		return "SimpleSourceFile{" +
				"name='" + name + '\'' +
				", contents='" + contents + '\'' +
				", encoding=" + encoding +
				'}';
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getContents() {
		return contents.getBytes(encoding);
	}
}
