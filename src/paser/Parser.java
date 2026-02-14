package paser;

import ast.Node;
import exception.*;

public interface Parser {
    /** Attempts to parse the token stream
     *  given to this parser.
     *  throws: exception.SyntaxError if the token
     *          stream cannot be parsed */
     Node parse() throws CheckException, SyntaxError;
}