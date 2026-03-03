package com.example.backend.model.engine;

import com.example.backend.model.exception.EvalError;

public interface DirectionAdapter {
    DIRECTION adapt(String input) throws EvalError;
}
