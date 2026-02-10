interface Parser {
    /** Attempts to parse the token stream
     *  given to this parser.
     *  throws: SyntaxError if the token
     *          stream cannot be parsed */
    Expr parse() throws CheckException, SyntaxError;
}