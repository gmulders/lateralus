package io.lateralus.parsergenerator.core.grammar;

/**
 * Represents a terminal {@link Symbol}
 */
public class Terminal extends Symbol {

    public static final Terminal EPSILON = new Terminal("ε");
    public static final Terminal EOF = new Terminal("EOF");

    public Terminal(String name) {
        super(true, name);
    }
}
