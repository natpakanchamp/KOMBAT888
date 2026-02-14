package engine;

public class GameState {
    // Spec: Field ขนาด 8x8
    private static final int ROWS = 8;
    private static final int COLS = 8;
    private static Unit[][] field = new Unit[ROWS][COLS];

    // Spec: Budget ต้องเป็น double เพื่อคำนวณดอกเบี้ย
    private static long playerBudget;
    private static int turnCount = 1;
    private static int usedSpawns = 0;

    // ตำแหน่งปัจจุบันของ Minion ที่กำลังรัน Strategy
    private static int currentRow;
    private static int currentCol;

    // เรียกตอนเริ่มเกม
    public static void initialize() {
        playerBudget = GameConfig.init_budget;
        turnCount = 1;
        // เคลียร์กระดาน
        field = new Unit[ROWS][COLS];
    }

    // --- Budget & Interest Logic [Spec: 61, 63] ---
    public static void startNewTurn() {
        // 1. เพิ่มงบรายเทิร์น
        playerBudget += GameConfig.turn_budget;

        // 2. คำนวณดอกเบี้ย: rate = b * log10(m) * ln(t)
        if (playerBudget > 0 && turnCount > 0) {
            double b = GameConfig.interest_pct;
            double m = playerBudget;
            double t = turnCount;
            double rate = b * Math.log10(m) * Math.log(t);
            double interest = m * rate / 100.0;
            playerBudget += interest;
        }

        // 3. จำกัดงบสูงสุด
        if (playerBudget > GameConfig.max_budget) {
            playerBudget = GameConfig.max_budget;
        }

        turnCount++;
    }

    public static long getPlayerBudget() {
        return (int) playerBudget; // Minion เห็นเป็น int
    }

    public static void pay(long amount) {
        playerBudget = Math.max(0, playerBudget - amount);
    }

    // --- Actions ---
    public static void move(String direction) {
        // Spec: Move costs 1 unit regardless of success [cite: 144]
        // (เช็คเงินก่อนเรียก pay ใน Node แล้ว แต่ถ้าจะให้ชัวร์คือหักเลย)
        // ในที่นี้สมมติว่า Node หักเงินมาแล้ว 1 หน่วย

        int[] nextPos = calculateOffset(currentRow, currentCol, direction);
        int r = nextPos[0];
        int c = nextPos[1];

        // Spec: No-op if occupied or out of bounds [cite: 143]
        if (isValidPos(r, c) && field[r][c] == null) {
            // ย้าย Unit ในกระดาน
            field[r][c] = field[currentRow][currentCol];
            field[currentRow][currentCol] = null;

            // อัปเดตตำแหน่งปัจจุบัน
            currentRow = r;
            currentCol = c;
            System.out.println("Moved " + direction + " to (" + r + "," + c + ")");
        } else {
            System.out.println("Move blocked.");
        }
    }

    public static void shoot(String direction, long expenditure) {
        // Spec: Total cost = expenditure + 1 (จ่ายใน Node แล้ว)
        int[] targetPos = calculateOffset(currentRow, currentCol, direction);
        int r = targetPos[0];
        int c = targetPos[1];

        if (isValidPos(r, c) && field[r][c] != null) {
            Unit target = field[r][c];
            long h = target.getHP();
            long d = target.getDefense();

            // Spec Damage Formula: max(0, h - max(1, x - d)) [cite: 154]
            long damage = Math.max(1, expenditure - d);
            long newHp = Math.max(0, h - damage);
            long damageDealt = h - newHp;

            target.takeDamage(damageDealt);
            System.out.println("Shot " + direction + "! HP left: " + target.getHP());

            if (target.isDead()) {
                field[r][c] = null; // ตายแล้วลบออกจากกระดาน
                System.out.println("Target eliminated!");
            }
        } else {
            System.out.println("Shot missed.");
        }
    }

    // --- Info / Sensing [Spec: 131] ---
    public static long query(String type, String direction) {
        if (type.equals("nearby")) {
            return calculateNearby(direction);
        } else if (type.equals("opponent")) {
            return findClosest(false);
        } else if (type.equals("ally")) {
            return findClosest(true);
        }
        return 0;
    }

    private static int calculateNearby(String direction) {
        // Nearby มองไปในทิศทางเดียว หาตัวที่ใกล้ที่สุดในทิศนั้น
        int r = currentRow;
        int c = currentCol;
        int dist = 0;

        // เดินไปเรื่อยๆ จนกว่าจะเจอ Unit หรือหลุดขอบ
        while (true) {
            int[] next = calculateOffset(r, c, direction);
            r = next[0];
            c = next[1];
            dist++;

            if (!isValidPos(r, c)) return 0; // หลุดขอบ ไม่เจอใคร
            if (field[r][c] != null) {
                Unit u = field[r][c];
                // สูตร: 100*len(hp) + 10*len(def) + distance
                int x = String.valueOf(u.getHP()).length();
                int y = String.valueOf(u.getDefense()).length();
                int z = dist;
                int val = 100 * x + 10 * y + z;

                // ถ้าเป็นพวกเดียวกันคืนค่าติดลบ [cite: 133]
                return u.isAlly() ? -val : val;
            }
        }
    }

    // Logic หา opponent/ally ที่ใกล้ที่สุด (Breadth-First Search หรือวนลูปหา)
    private static int findClosest(boolean findAlly) {
        int minVal = Integer.MAX_VALUE; // เก็บค่าที่น้อยที่สุด (dist*10 + dir)

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Unit u = field[r][c];
                // เช็คว่าเป็น Unit ที่เราตามหาหรือไม่ (เพื่อน หรือ ศัตรู)
                if (u != null && u.isAlly() == findAlly) {
                    // หาว่าอยู่ทิศไหน
                    int dir = calculateDirNum(currentRow, currentCol, r, c);

                    // ถ้า dir != 0 แสดงว่าอยู่ในแนวเส้นตรงที่มองเห็นได้
                    if (dir != 0) {
                        // คำนวณระยะห่าง (Spec: distance)
                        // เนื่องจากอยู่ในแนวตรง เราใช้วิธีคำนวณระยะแบบไหนก็ได้
                        // หรือจะให้ calculateDirNum คืนระยะมาด้วยก็ได้
                        // แต่วิธีง่ายๆ คือใช้สูตร Hex distance หรือวนลูปนับเอา
                        // ในที่นี้สมมติว่าใช้ Math.max(abs(dr), abs(dc)) คร่าวๆ หรือเรียก helper
                        int dist = getDistance(currentRow, currentCol, r, c);

                        // สร้างค่าผลลัพธ์ตาม Format: dist * 10 + dir
                        // เช่น ระยะ 3 ทิศ 1 -> 31
                        int val = dist * 10 + dir;

                        if (val < minVal) {
                            minVal = val;
                        }
                    }
                }
            }
        }

        return (minVal == Integer.MAX_VALUE) ? 0 : minVal;
    }

    // Helper เพื่อนับระยะทาง (ใช้เฉพาะกรณีที่รู้ว่าอยู่ในแนวเส้นตรงเดียวกันแล้ว)
    private static int getDistance(int r1, int c1, int r2, int c2) {
        // กรณี Hexagon แบบ Odd-q (Skew Vertical) ระยะทางอาจจะซับซ้อน
        // แต่วิธีที่ชัวร์ที่สุดคือเรียก calculateOffset เดินไปหาแล้วนับก้าวครับ
        String[] directions = {"", "up", "upright", "downright", "down", "downleft", "upleft"};
        int dir = calculateDirNum(r1, c1, r2, c2);
        if (dir == 0) return 999;

        int steps = 0;
        int r = r1, c = c1;
        while(r != r2 || c != c2) {
            int[] next = calculateOffset(r, c, directions[dir]);
            r = next[0];
            c = next[1];
            steps++;
        }
        return steps;
    }

    // --- Helper: Hex Grid Logic (Odd-q Vertical Skew) ---
    private static int[] calculateOffset(int r, int c, String direction) {
        int targetR = r;
        int targetC = c;
        boolean isEvenCol = (c % 2 == 0); // เช็คคอลัมน์คู่

        switch (direction.toLowerCase()) {
            case "up"        -> targetR--;
            case "down"      -> targetR++;
            case "upleft"    -> { targetC--; if (isEvenCol) targetR--; }
            case "upright"   -> { targetC++; if (isEvenCol) targetR--; }
            case "downleft"  -> { targetC--; if (!isEvenCol) targetR++; }
            case "downright" -> { targetC++; if (!isEvenCol) targetR++; }
        }
        return new int[]{targetR, targetC};
    }

    // เพิ่มเมธอดนี้ใน GameState
    private static int calculateDirNum(int r1, int c1, int r2, int c2) {
        // อาร์เรย์เก็บชื่อทิศทางตามลำดับ 1-6
        // 1=up, 2=upright, 3=downright, 4=down, 5=downleft, 6=upleft
        String[] directions = {"", "up", "upright", "downright", "down", "downleft", "upleft"};

        // วนหาทั้ง 6 ทิศทาง
        for (int d = 1; d <= 6; d++) {
            int r = r1;
            int c = c1;

            // เดินหน้าไปในทิศ d เรื่อยๆ เพื่อดูว่าจะชนเป้าหมายไหม
            // (จำกัดระยะเดินเผื่อไว้ไม่ให้เกินขนาดกระดาน เช่น 15 ช่อง)
            for (int dist = 0; dist < 15; dist++) {
                // คำนวณตำแหน่งถัดไป
                int[] next = calculateOffset(r, c, directions[d]);
                r = next[0];
                c = next[1];

                // ถ้าออกนอกกระดาน ให้หยุดทิศนี้ แล้วไปดูทิศถัดไป
                if (!isValidPos(r, c)) break;

                // ถ้าเจอเป้าหมาย คืนค่าทิศทางทันที
                if (r == r2 && c == c2) {
                    return d;
                }
            }
        }

        return 0; // ไม่เจอในทิศทางหลักใดๆ เลย
    }

    private static boolean isValidPos(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    // Setters for Testing/Setup
    public static void setMinionPos(int r, int c) { currentRow = r; currentCol = c; }
    // แก้ไข: เปลี่ยน int hp, int def เป็น long
    public static void spawnUnit(int r, int c, long hp, long def, boolean isAlly) {
        if(isValidPos(r,c)) {
            // ส่งค่า long เข้า Unit ได้เลย ไม่ต้องกลัว data loss
            field[r][c] = new Unit(hp, def, isAlly, 0);
        }
        usedSpawns++;
    }

    // Getters for Variables
    public static int getCurrentRow() { return currentRow; }
    public static int getCurrentCol() { return currentCol; }
    public static int getInterestRate() {
        if (playerBudget <= 0) return 0;
        double b = GameConfig.interest_pct;
        double m = playerBudget;
        double t = turnCount;
        return (int) (b * Math.log10(m) * Math.log(t));
    }
    public static int getMaxBudget() { return (int) GameConfig.max_budget; }
    // แก้ไขจาก: return (int) usedSpawns;
    public static int getRemainingSpawns() {
        return (int) (GameConfig.max_spawns - usedSpawns);
    }
    /**
     * ดึงค่าจำนวนเทิร์นสูงสุดจาก GameConfig
     * @return จำนวนเทิร์นสูงสุด (long)
     */
    public static long getMaxTurns() {
        return GameConfig.max_turns;
    }
}