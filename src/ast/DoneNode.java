package ast;

import exception.*;
import java.util.Map;

public class DoneNode implements Node {
    @Override
    public void execute(Map<String, Integer> localVars, Map<String, Integer> globalVars) throws EvalError {
        // โยน Exception พิเศษเพื่อให้ระบบประเมินผลหยุดทำงานในเทิร์นนี้
        throw new DoneException();
    }
}
