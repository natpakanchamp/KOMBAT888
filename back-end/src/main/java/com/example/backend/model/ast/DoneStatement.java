package com.example.backend.model.ast;

import com.example.backend.model.exception.DoneException;
import com.example.backend.model.exception.EvalError;

import java.util.Map;

public class DoneStatement implements Statement {
    @Override
    public void execute(Map<String, Long> localVars, Map<String, Long> globalVars) throws EvalError {
        // โยน Exception พิเศษเพื่อให้ระบบประเมินผลหยุดทำงานในเทิร์นนี้
        throw new DoneException();
    }
}
