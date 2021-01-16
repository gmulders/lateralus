package ${parserPackageName};

import ${lexerPackageName}.Lexer;
import ${lexerPackageName}.LexerException;
import ${lexerPackageName}.Token;
import ${parserPackageName}.nodes.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import test.lexer.LexerReaderImpl;
import test.lexer.SuperLexer;
import java.io.StringReader;
import test.parser.visitor.NodeVisitor;
import test.parser.visitor.VisitingException;

public class Parser {

	protected static final int[] PRODUCTION_SIZE = ${productionSizeJava};
	protected static final int[] PRODUCTION_NON_TERMINAL_ID = ${productionNonTerminalIdJava};
	protected static final int TABLE_WIDTH = ${tableWidth};
	protected static final int[] TABLE = readTable();
	protected static final int PARSER_ACTION_ACCEPT = 1;
	protected static final int PARSER_ACTION_SKIP = 2;
	protected static final int ACTION_PRODUCTION_OFFSET = 3;

	protected final Lexer lexer;

	protected int state;
	protected Deque<Integer> stateStack;
	protected Deque<Object> nodeStack;

	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	public Node parse() throws ParserException {
		stateStack = new ArrayDeque<>();
		stateStack.push(0);
		nodeStack = new ArrayDeque<>();

		Token token = nextToken();

		while (true) {
			state = stateStack.peek();
			int action = TABLE[state * TABLE_WIDTH + token.getTokenType().ordinal()];

			if (action == PARSER_ACTION_SKIP) {
				token = nextToken();
			} else if (action == PARSER_ACTION_ACCEPT) {
				return (Node)nodeStack.pop();
			} else if (action < 0) {
				nodeStack.push(token);
				stateStack.push(~action);
				token = nextToken();
			} else if (action > 0) {
				int productionId = action - ACTION_PRODUCTION_OFFSET;
				int productionSize = PRODUCTION_SIZE[productionId];
				Object[] nodes = popNodes(nodeStack, productionSize);
				nodeStack.push(reduce(productionId, nodes));
				state = popStates(stateStack, productionSize);
				stateStack.push(TABLE[state * TABLE_WIDTH + PRODUCTION_NON_TERMINAL_ID[productionId]]);
			} else {
				handleSyntaxError(token);
			}
		}
	}

	protected void handleSyntaxError(Token token) throws ParserSyntaxException {
		throw new ParserSyntaxException("Syntax error", token);
	}

	private Token nextToken() throws ParserException {
		try {
			return lexer.nextToken();
		} catch (LexerException e) {
			throw new ParserException("LexerException during parse: ", e);
		}
	}

	private static Object[] popNodes(Deque<Object> stack, int size) {
		Object[] nodes = new Object[size];
		for (int i = size - 1; i >= 0; i--) {
			nodes[i] = stack.pop();
		}
		return nodes;
	}

	private static int popStates(Deque<Integer> stack, int size) {
		for (int i = 0; i < size; i++) {
			stack.pop();
		}
		return stack.peek();
	}

	private Node reduce(int productionId, Object[] nodes) throws ParserException {
		switch (productionId) {
<#list reductionList as reduction>
			case ${reduction.productionId}:
				return <#if reduction.isCast><#--
       					-->(${reduction.nodeType})nodes[0];<#--
       				--><#else><#--
       					-->new ${reduction.nodeType}(<#--
       						--><#list reduction.parameterList as parameter><#--
       							-->(${parameter.type})nodes[${parameter_index}]<#--
       							--><#if parameter_has_next>, </#if><#--
       						--></#list><#--
       					-->);<#--
       				--></#if>

</#list>
			default:
				throw new ParserException("Unknown production...");
		}
	}

	private static int[] readTable() {
		try {
			// Open the parser.table so we can compare the results with the TABLE static property.
			InputStream is = Parser.class.getResourceAsStream("parser.table");
			int height = 0;
			height = readInt(is);
			int width = readInt(is);
			int[] table = new int[height * width];
			int index = 0;
			while (index < height * width) {
				int value = readInt(is);
				if (value == Integer.MIN_VALUE) {
					int count = readInt(is);
					Arrays.fill(table, index, index += count, 0);
				} else {
					table[index++] = value;
				}
			}
			// Read the number of skip columns.
			int count = readInt(is);
			for (int i = 0; i < count; i++) {
				int column = readInt(is);
				for (int row = 0; row < height; row++) {
					table[row * width + column] = PARSER_ACTION_SKIP;
				}
			}
			return table;
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not load parser table", e);
		}
	}

	protected static int readInt(InputStream in) throws IOException {
		byte b = (byte)in.read();

		// This is a special value that is never used in a variable length int.
		if (b == (byte) 0x80) {
			return Integer.MIN_VALUE;
		}

		int value = b & 0x7F;
		while ((b & 0x80) != 0) {
			b = (byte)in.read();
			value <<= 7;
			value |= (b & 0x7F);
		}

		return (value >>> 1) ^ -(value & 1);
	}
}
