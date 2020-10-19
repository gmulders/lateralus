package io.lateralus.parsergenerator.main;

import io.lateralus.parsergenerator.codegenerator.CodeGenerationException;
import io.lateralus.parsergenerator.codegenerator.CodeGenerator;
import io.lateralus.parsergenerator.codegenerator.simple.BasicParserCodeGenerator;
import io.lateralus.parsergenerator.core.definition.ParserDefinition;
import io.lateralus.parsergenerator.core.definition.ParserDefinitionBuilder;
import io.lateralus.parsergenerator.core.definition.ParserDefinitionException;
import io.lateralus.parsergenerator.core.definition.State;
import io.lateralus.parsergenerator.core.definition.closer.ChenxCloser;
import io.lateralus.parsergenerator.core.definition.closer.Closer;
import io.lateralus.parsergenerator.core.grammar.Grammar;
import io.lateralus.parsergenerator.core.grammar.Symbol;
import io.lateralus.parsergenerator.core.grammar.Terminal;
import io.lateralus.parsergenerator.parser.GrammarParser;
import io.lateralus.parsergenerator.parser.GrammarParserException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

	public static void main(String[] args) throws GrammarParserException, CodeGenerationException, ParserDefinitionException {

		// https://zaa.ch/jison/try/usf/index.html
		// %%
		//
		//E
		//    : T X
		//    ;
		//X
		//    : plus T X
		//    | %empty
		//    ;
		//
		//T
		//    : F Y
		//    ;
		//
		//Y
		//    : times F Y
		//    | %empty
		//    ;
		//
		//F   : left E right
		//    | id
		//    ;

		// 0
		//$accept -> .E $end #lookaheads= $end
		//E -> .T X #lookaheads= $end
		//T -> .F Y #lookaheads= $end plus
		//F -> .left E right #lookaheads= $end plus times
		//F -> .id #lookaheads= $end plus times
		// 1
		//$accept -> E .$end #lookaheads= $end
		// 2
		//E -> T .X #lookaheads= $end
		//X -> .plus T X #lookaheads= $end
		//X -> . #lookaheads= $end
		// 3
		//T -> F .Y #lookaheads= $end plus
		//Y -> .times F Y #lookaheads= $end plus
		//Y -> . #lookaheads= $end plus
		// 4
		//F -> left .E right #lookaheads= $end plus times
		//E -> .T X #lookaheads= right
		//T -> .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 5
		//F -> id . #lookaheads= $end plus times
		// 6
		//E -> T X . #lookaheads= $end
		// 7
		//X -> plus .T X #lookaheads= $end
		//T -> .F Y #lookaheads= $end plus
		//F -> .left E right #lookaheads= $end plus times
		//F -> .id #lookaheads= $end plus times
		// 8
		//T -> F Y . #lookaheads= $end plus
		// 9
		//Y -> times .F Y #lookaheads= $end plus
		//F -> .left E right #lookaheads= $end plus times
		//F -> .id #lookaheads= $end plus times
		// 10
		//F -> left E .right #lookaheads= $end plus times
		// 11
		//E -> T .X #lookaheads= right
		//X -> .plus T X #lookaheads= right
		//X -> . #lookaheads= right
		// 12
		//T -> F .Y #lookaheads= plus right
		//Y -> .times F Y #lookaheads= plus right
		//Y -> . #lookaheads= plus right
		// 13
		//F -> left .E right #lookaheads= plus right times
		//E -> .T X #lookaheads= right
		//T -> .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 14
		//F -> id . #lookaheads= plus right times
		// 15
		//X -> plus T .X #lookaheads= $end
		//X -> .plus T X #lookaheads= $end
		//X -> . #lookaheads= $end
		// 16
		//Y -> times F .Y #lookaheads= $end plus
		//Y -> .times F Y #lookaheads= $end plus
		//Y -> . #lookaheads= $end plus
		// 17
		//F -> left E right . #lookaheads= $end plus times
		// 18
		//E -> T X . #lookaheads= right
		// 19
		//X -> plus .T X #lookaheads= right
		//T -> .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 20
		//T -> F Y . #lookaheads= plus right
		// 21
		//Y -> times .F Y #lookaheads= plus right
		//F -> .left E right #lookaheads= plus right times
		//F -> .id #lookaheads= plus right times
		// 22
		//F -> left E .right #lookaheads= plus right times
		// 23
		//X -> plus T X . #lookaheads= $end
		// 24
		//Y -> times F Y . #lookaheads= $end plus
		// 25
		//X -> plus T .X #lookaheads= right
		//X -> .plus T X #lookaheads= right
		//X -> . #lookaheads= right
		// 26
		//Y -> times F .Y #lookaheads= plus right
		//Y -> .times F Y #lookaheads= plus right
		//Y -> . #lookaheads= plus right
		// 27
		//F -> left E right . #lookaheads= plus right times
		// 28
		//X -> plus T X . #lookaheads= right
		// 29
		//Y -> times F Y . #lookaheads= plus right

		// %%
		//E
		//    : E plus T
		//    | T
		//    ;
		//T
		//    : T times F
		//    | F
		//    ;
		//F
		//    : left E right
		//    | id
		//    ;

//		String grammarString =
//				"E -> T plus E\n" +
//				"E -> T\n" +
//				"T -> F times T\n" +
//				"T -> F\n" +
//				"F -> left E right\n" +
//				"F -> id";

//		String grammarString =
//				"E -> E plus T\n" +
//				"E -> T\n" +
//				"T -> T times F\n" +
//				"T -> F\n" +
//				"F -> left E right\n" +
//				"F -> id\n";

		// This grammar string does not work yet; we need to add some sort of annotation so that we know what the name
		// for the node that we will generate should be.
		String grammarString =
				"Expression -> Term\n" +
				"Expression -> Expression(lhs) PLUS Term(rhs) : Plus binary\n" +
				"Term -> Factor\n" +
				"Term -> Term(lhs) TIMES Factor(rhs) : Product binary\n" +
				"Factor -> LEFT_PAREN Expression RIGHT_PAREN : Paren\n" +
				"Factor -> NUMBER : Number\n";

//		String grammarString =
//				"E -> T X\n" +
//				"X -> plus T X | ε\n" +
//				"T -> F Y\n" +
//				"Y -> times F Y | ε\n" +
//				"F -> left E right | id";

		// First parse the input grammar into an internal representation
		Grammar grammar = GrammarParser.from(grammarString);

//		Closer closer = new KnuthCloser(grammar);
		Closer closer = ChenxCloser.builder(grammar).build();

		List<Terminal> orderedTerminalList = List.of(Terminal.EOF, new Terminal("PLUS"), new Terminal("TIMES"),
				new Terminal("LEFT_PAREN"), new Terminal("RIGHT_PAREN"), new Terminal("NUMBER"));

		ParserDefinition parserDefinition = new ParserDefinitionBuilder(closer).build(grammar, orderedTerminalList);

		printDefinition(parserDefinition);

		CodeGenerator<BasicParserCodeGenerator.Properties, String> codeGenerator = new BasicParserCodeGenerator();
		codeGenerator.setProperties(new BasicParserCodeGenerator.Properties("Super", "test.parser", "test.lexer"));
		codeGenerator.generate(parserDefinition)
				.forEach(f -> {
					System.out.println("=============== " + f.getName() + " ===============");
					System.out.println(f.getContents());
					try {
						Path path = Path.of("parsergenerator/main/src/test-out/java/", f.getName()).toAbsolutePath();
						Files.createDirectories(path.getParent());
						Files.write(path, f.getContents().getBytes(StandardCharsets.UTF_8));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
	}

	private static void printDefinition(ParserDefinition parserDefinition) {
		Grammar grammar = parserDefinition.getGrammar();
		for (Symbol symbol : grammar.getNonTerminals()) {
			System.out.println(symbol + " -> " + grammar.firstSet(symbol));
		}
		System.out.println();

		int i = 0;
		for (State state : parserDefinition.getCanonicalCollection()) {
			System.out.println(String.format("%02d", i++) + " " + state);
		}

		System.out.println();
		parserDefinition.getGotoTable().cellSet().forEach(cell ->
				System.out.println(cell.getRowKey() + " + " + cell.getColumnKey() + " --> " + cell.getValue())
		);

		System.out.println();
		parserDefinition.getActionTable().cellSet().forEach(cell ->
				System.out.println(cell.getRowKey() + " + " + cell.getColumnKey() + " --> " + cell.getValue())
		);
	}
}
