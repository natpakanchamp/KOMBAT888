import java.util.Map;

public class GameState {
    // --- สถานะภายในเกม (Internal State) ---
    private static int playerBudget = 1000;
    private static int currentRow = 5; // สมมติค่าเริ่มต้นตำแหน่งผู้เล่น
    private static int currentCol = 5;

    // สมมติสนามขนาด 10x10 โดยเก็บเป็น Object ของ Unit (ศัตรู/เพื่อน)
    // ในที่นี้สมมติเป็นอาเรย์เพื่อความเข้าใจง่าย
    private static Unit[][] field = new Unit[10][10];

    // --- ระบบงบประมาณ (Budgeting) ---
    public static int getPlayerBudget() {
        return playerBudget;
    }

    public static void pay(int amount) {
        // หักเงินโดยไม่ให้ต่ำกว่า 0
        playerBudget = Math.max(0, playerBudget - amount);
    }

    // --- ระบบการกระทำ (Actions) ---
    public static void move(String direction) {
        // หาพิกัดถัดไป
        int[] nextPos = calculateOffset(currentRow, currentCol, direction);
        int nextRow = nextPos[0];
        int nextCol = nextPos[1];

        // ตรวจสอบว่าเดินได้หรือไม่ (ไม่หลุดขอบและไม่มีใครขวาง)
        if (isValidPos(nextRow, nextCol) && field[nextRow][nextCol] == null) {
            currentRow = nextRow;
            currentCol = nextCol;
            System.out.println("Minion moved to: " + direction + " (" + currentRow + "," + currentCol + ")");
        } else {
            System.out.println("Move failed: blocked or out of bounds");
        }
    }

    public static void shoot(String direction, int x) {
        System.out.println("Shooting " + direction + " with power " + x);
        int[] targetPos = calculateOffset(currentRow, currentCol, direction);
        int r = targetPos[0];
        int c = targetPos[1];

        if (isValidPos(r, c) && field[r][c] != null) {
            Unit target = field[r][c];
            // damage = max(0, x - enemyDefense) -> ตามกฎที่คุณระบุ
            int damage = Math.max(0, x - target.getDefense());
            target.takeDamage(damage);
            System.out.println("Hit enemy! Damage dealt: " + damage);
        }
    }

    // --- ระบบข้อมูล (Information Query) ---
    /**
     * รองรับการเรียกจาก NearbyExpr, InfoExpr
     */
    public static int query(String type, String direction) {
        if (type.equals("nearby") && direction != null) {
            // Logic คำนวณพิกัดตามทิศทาง
            int[] pos = calculateOffset(currentRow, currentCol, direction);
            return calculateNearbyValue(pos[0], pos[1]);
        }
        // สำหรับ ally / opponent ที่ไม่ต้องใช้ทิศทาง
        return findClosestUnit(type.equals("ally"));
    }

    // --- Helper Methods (Internal Logic) ---

    private static int calculateNearbyValue(int r, int c) {
        if (!isValidPos(r, c) || field[r][c] == null) {
            return 0; // ช่องว่างหรือนอกขอบเขต
        }
        Unit u = field[r][c];
        // สูตร: 100*hpSize + 10*defSize + distance
        // ในกรณี nearby ช่องติดกัน distance มักจะเป็น 1
        return (100 * u.getHP()) + (10 * u.getDefense()) + 1;
    }

    private static int[] calculateOffset(int r, int c, String direction) {
        int targetR = r;
        int targetC = c;
        switch (direction.toLowerCase()) {
            case "up"        -> targetR--;
            case "down"      -> targetR++;
            case "upleft"    -> { targetR--; targetC--; }
            case "upright"   -> { targetR--; targetC++; }
            case "downleft"  -> { targetR++; targetC--; }
            case "downright" -> { targetR++; targetC++; }
        }
        return new int[]{targetR, targetC};
    }

    private static boolean isValidPos(int r, int c) {
        return r >= 0 && r < 10 && c >= 0 && c < 10;
    }

    private static int findClosestUnit(boolean isAlly) {
        // ตรรกะการหาศัตรู/เพื่อนที่ใกล้ที่สุดเพื่อคืนค่าให้ ally | opponent
        return 0;
    }

    // --- Getter พื้นฐานตามที่คุณระบุ ---
    public static int getCurrentRow() { return currentRow; }
    public static int getCurrentCol() { return currentCol; }
    public static int getInterestRate() { return 5; }
    public static int getMaxBudget() { return 10000; }
    public static int getRemainingSpawns() { return 3; }
}