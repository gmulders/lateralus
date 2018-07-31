package ${packageName};

import java.io.IOException;
import java.io.Reader;

public class LexerReaderImpl implements LexerReader {

	private Reader delegate;

	private char[] buffer = new char[8096];
	private int mark;
	private int index;
	private int end;
	private int endRead;

	private boolean skipLF;

	private int currentLineNumber = 1;
	private int currentColumnNumber = 1;

	public LexerReaderImpl(Reader delegate) {
		this.delegate = delegate;
	}

	@Override
	public int peek() throws IOException {
		if (index >= end) {
			fillBuffer();

			if (index >= end) {
				return -1;
			}
		}

		return Character.codePointAt(buffer, index);
	}

	@Override
	public void eat() throws IOException {
		int codePoint = peek();
		index += Character.charCount(codePoint);

		currentColumnNumber++;
		switch (codePoint) {
			case -1:
				currentColumnNumber--;
				break;
			case '\r':
				skipLF = true;
				currentLineNumber++;
				currentColumnNumber = 1;
				break;
			case '\n':
				if (!skipLF) {
					currentLineNumber++;
				}
				skipLF = false;
				break;
		}
	}

	@Override
	public int getCurrentLineNumber() {
		return currentLineNumber;
	}

	@Override
	public int getCurrentColumnNumber() {
		return currentColumnNumber;
	}

	@Override
	public String readLexeme() {
		return new String(buffer, mark, index - mark);
	}

	@Override
	public void startLexeme() {
		mark = index;
	}

	private void fillBuffer() throws IOException {

		ensureSpace();

		int len;
		do {
			len = delegate.read(buffer, endRead, buffer.length - endRead);
		} while (len == 0);

		if (len < 0) {
			return;
		}

		end = endRead += len;
		if (Character.isHighSurrogate(buffer[endRead - 1])) {
			end -= 1;
		}
	}

	private void ensureSpace() {
		// Make sure there is space for at least two chars.
		if (endRead <= buffer.length - 2) {
			return;
		}

		// If the mark is higher then 0, we can shift the contents of the array towards 0.
		if (mark > 0) {
			System.arraycopy(buffer, mark, buffer, 0, endRead - mark);
			index -= mark;
			end -= mark;
			endRead -= mark;
			mark = 0; // mark -= mark;

			// Check if we have space for at least two chars now.
			if (endRead <= buffer.length - 2) {
				return;
			}
		}

		// If we get here we still do not have enough space.

		// Create a buffer that is twice the size of the previous buffer.
		char[] buffer = new char[this.buffer.length * 2];
		System.arraycopy(this.buffer, mark, buffer, 0, this.buffer.length);
		this.buffer = buffer;
	}
}
