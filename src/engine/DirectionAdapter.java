package engine;

import exception.EvalError;

public interface DirectionAdapter {
    DIRECTION adapt(String input) throws EvalError;
}