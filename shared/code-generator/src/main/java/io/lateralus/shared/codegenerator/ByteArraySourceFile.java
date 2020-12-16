package io.lateralus.shared.codegenerator;

public class ByteArraySourceFile implements SourceFile {

	private final String name;
	private final byte[] contents;

	public ByteArraySourceFile(String name, byte[] contents) {
		this.name = name;
		this.contents = contents;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getContents() {
		return contents;
	}
}
