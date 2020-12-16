package test.parser;

import test.lexer.Lexer;
import test.lexer.LexerReaderImpl;
import test.lexer.SuperLexer;
import test.lexer.Token;
import test.parser.nodes.Node;
import test.parser.nodes.NumberNode;
import test.parser.nodes.ParenNode;
import test.parser.nodes.PlusNode;
import test.parser.nodes.ProductNode;
import test.parser.visitor.NodeVisitor;
import test.parser.visitor.VisitingException;

import java.io.StringReader;

public class TestParser extends Parser {

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
		System.out.println("Done!");
	}

	public TestParser(Lexer lexer) {
		super(lexer);
	}

	@Override
	protected void handleSyntaxError(Token token) throws ParserSyntaxException {
		// TODO: First try some methods of handling errors before making this generic.
		super.handleSyntaxError(token);
	}
}
