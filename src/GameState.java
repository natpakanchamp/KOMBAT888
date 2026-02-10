// สร้างไว้เพื่อไม่ให้มี error เดี๋ยวลบออกทีหลัง

public class GameState {
    public static int getCurrentRow() { return 0; }
    public static int getCurrentCol() { return 0; }
    public static int getPlayerBudget() { return 1000; }
    public static int getInterestRate() { return 5; }
    public static int getMaxBudget() { return 10000; }
    public static int getRemainingSpawns() { return 3; }

    /**
     * ปรับให้รับพารามิเตอร์เพื่อให้ InfoExprNode เรียกใช้ได้
     */
    public static int query(String type, String direction) {
        // ในช่วงทดสอบ คุณอาจจะให้คืนค่าคงที่ไปก่อน
        // เช่น ถ้าหาศัตรู (opponent) ให้คืนค่าระยะทางสมมติ
        if ("opponent".equals(type)) return 5;
        return 0;
    }
}