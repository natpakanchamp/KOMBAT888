package engine;

public enum DIRECTION {
    UP, DOWN, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT;

    public static DIRECTION fromString(String text) {
        for (DIRECTION dir : DIRECTION.values()) {
            if (dir.name().equalsIgnoreCase(text)) {
                return dir;
            }
        }
        // เปลี่ยนข้อความเป็นภาษาอังกฤษ
        throw new IllegalArgumentException("No direction found with name: " + text);
    }
}