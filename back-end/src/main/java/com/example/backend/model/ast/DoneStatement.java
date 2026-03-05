package com.example.backend.model.ast;

import com.example.backend.model.engine.GameState;
import com.example.backend.model.engine.Unit;
import com.example.backend.model.exception.DoneException;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public class DoneStatement implements Statement {
    @Override
    public void execute(GameState state, Unit currentUnit, Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // โยน Exception พิเศษเพื่อให้ระบบประเมินผลหยุดทำงานในเทิร์นนี้
        throw new DoneException();
    }
}
