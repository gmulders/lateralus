package io.lateralus.lexergenerator.core.parser.visitors;

import io.lateralus.lexergenerator.core.parser.RegExException;

public class VisitingException extends RegExException {
	public VisitingException(String message) {
		super(message);
	}
}
