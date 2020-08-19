package io.lateralus.parsergenerator.core;

/**
 * Represents a terminal {@link Symbol}
 */
public class Terminal extends Symbol {

    public static final Terminal EPSILON = new Terminal("ε") {
        @Override
        public boolean isVanishable() {
            return true;
        }
    };
    public static final Terminal EOF = new Terminal("$");

    public Terminal(String name) {
        super(true, name);
    }

    @Override
    public boolean isVanishable() {
        return false;
    }
}
