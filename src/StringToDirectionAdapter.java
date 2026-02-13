public class StringToDirectionAdapter implements DirectionAdapter {
    @Override
    public DIRECTION adapt(String input) throws EvalError {
        try {
            return DIRECTION.fromString(input);
        } catch (IllegalArgumentException e) {
            // แปลง Exception เป็น EvalError ตามที่ Node ต้องการ
            throw new EvalError("Execution failed: " + e.getMessage());
        }
    }
}