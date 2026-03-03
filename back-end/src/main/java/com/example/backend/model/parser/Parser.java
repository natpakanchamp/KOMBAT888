package com.example.backend.model.parser;

import com.example.backend.model.ast.Statement;
import com.example.backend.model.exception.*;

public interface Parser {
    /** Attempts to parse the token stream
     *  given to this parser.
     *  throws: exception.SyntaxError if the token
     *          stream cannot be parsed */
    Statement parse() throws CheckException, SyntaxError;
}
