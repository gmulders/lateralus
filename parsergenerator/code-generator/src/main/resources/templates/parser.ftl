package ${parserPackageName};

import ${lexerPackageName}.Lexer;
import ${lexerPackageName}.LexerException;
import ${lexerPackageName}.Token;
import ${parserPackageName}.nodes.*;

import java.util.ArrayDeque;
import java.util.Deque;

import test.lexer.LexerReaderImpl;
import test.lexer.SuperLexer;
import java.io.StringReader;
import test.parser.visitor.NodeVisitor;
import test.parser.visitor.VisitingException;

public class Parser {

	/**
	 * For ease of testing we temporarily have this method.
	 */
	public static void main(String[] args) throws ParserException, VisitingException {
		Lexer lexer = new SuperLexer(new LexerReaderImpl(new StringReader("3*3+9")));
		Parser parser = new Parser(lexer);

		Node node = parser.parse();

		NodeVisitor<String, VisitingException> visitor = new NodeVisitor<>() {
			@Override
			public String visit(PlusNode node) throws VisitingException {
				return "\"" + node + "\" [label=\"+\"]\n" +
						"\"" + node + "\" -> {\"" + node.getLhs() + "\" \"" + node.getRhs() + "\"}\n" +
						node.getLhs().accept(this) +
						node.getRhs().accept(this);
			}

			@Override
			public String visit(ParenNode node) throws VisitingException {
				return "\"" + node + "\" [label=\"paren\"]\n" +
						"\"" + node + "\" -> {\"" + node.getExpression() + "\"}\n" +
						node.getExpression().accept(this);
			}

			@Override
			public String visit(NumberNode node) throws VisitingException {
				return "\"" + node + "\" [label=\"" + node.getNumber().getValue() + "\"]\n";
			}

			@Override
			public String visit(ProductNode node) throws VisitingException {
				return "\"" + node + "\" [label=\"*\"]\n" +
						"\"" + node + "\" -> {\"" + node.getLhs() + "\" \"" + node.getRhs() + "\"}\n" +
						node.getLhs().accept(this) +
						node.getRhs().accept(this);

			}
		};

		System.out.println(node.accept(visitor));
	}

	private static final int[] PRODUCTION_SIZE = ${productionSizeJava};
	private static final int[] PRODUCTION_NON_TERMINAL_ID = ${productionNonTerminalIdJava};
	private static final int[][] TABLE = ${actionTableJava};

	private final Lexer lexer;

	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}

	public Node parse() throws ParserException {
		Deque<Integer> stateStack = new ArrayDeque<>();
		stateStack.push(0);
		Deque<Object> nodeStack = new ArrayDeque<>();

		Token token = nextToken();

		while (true) {
			int state = stateStack.peek();
			int action = TABLE[state][token.getTokenType().ordinal()];

			if (action == Integer.MIN_VALUE) {
				return (Node)nodeStack.pop();
			} else if (action < 0) {
				nodeStack.push(token);
				stateStack.push(~action);
				token = nextToken();
			} else if (action > 0) {
				int productionId = action - 1;
				int productionSize = PRODUCTION_SIZE[productionId];
				Object[] nodes = popNodes(nodeStack, productionSize);
				nodeStack.push(reduce(productionId, nodes));
				state = popStates(stateStack, productionSize);
				stateStack.push(TABLE[state][PRODUCTION_NON_TERMINAL_ID[productionId]]);
			} else {
				throw new ParserException("");
			}
		}
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
}
