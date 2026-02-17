package engine;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public static final int ROWS = 8;
    public static final int COLS = 8;

    private static Unit[][] field;

    // แยกเงินเป็น 2 กระเป๋า (เก็บเป็น double เพื่อความละเอียดตอนคิดดอกเบี้ย)
    private static double p1Budget;
    private static double p2Budget;

    private static int turnCount;

    // ใช้ Array นับจำนวน Spawn ของแต่ละคน (Index 1=P1, 2=P2)
    private static int[] usedSpawns;

    // ตัวแปรบอกว่าตอนนี้ตาใคร (1 หรือ 2)
    private static int currentPlayer = 1;

    // ตำแหน่งปัจจุบันของ Minion ที่กำลังรันคำสั่ง
    private static int currentRow;
    private static int currentCol;

    // --- Initialization ---
    public static void initialize() {
        // [UPDATE] รีเซ็ต ID ของ Unit เพื่อเริ่มนับ 1 ใหม่
        Unit.resetId();

        p1Budget = GameConfig.init_budget;
        p2Budget = GameConfig.init_budget;
        turnCount = 1;

        // Reset Spawn Counts (ใช้ index 1 และ 2)
        usedSpawns = new int[]{0, 0, 0};

        field = new Unit[ROWS][COLS];
        currentPlayer = 1;
        currentRow = 0;
        currentCol = 0;
    }

    // --- Player & Turn Management ---
    public static void setCurrentPlayer(int player) {
        currentPlayer = player;
    }

    public static int getCurrentPlayer() {
        return currentPlayer;
    }

    // คำนวณรายได้และดอกเบี้ย (เรียกตอนเริ่มรัน Script ของแต่ละคน)
    public static void processTurnIncome() {
        // เลือกกระเป๋าเงินของคนปัจจุบัน
        double currentBudget = (currentPlayer == 1) ? p1Budget : p2Budget;

        // 1. เพิ่มงบรายเทิร์น
        currentBudget += GameConfig.turn_budget;

        // 2. คำนวณดอกเบี้ย (ตามสูตรในรูปภาพ)
        // สูตร: rate = b * log10(m) * ln(t)
        if (currentBudget > 0 && turnCount > 0) {
            double b = GameConfig.interest_pct;
            double m = currentBudget;
            double t = turnCount;

            // Math.log10 คือ log ฐาน 10
            // Math.log คือ natural log (ln)
            double rate = b * Math.log10(m) * Math.log(t);

            // ดอกเบี้ยที่ได้รับ = m * (rate / 100)
            double interest = m * rate / 100.0;
            currentBudget += interest;
        }

        // 3. จำกัดงบสูงสุด (Cap)
        if (currentBudget > GameConfig.max_budget) {
            currentBudget = GameConfig.max_budget;
        }

        // บันทึกค่ากลับลงกระเป๋า
        if (currentPlayer == 1) p1Budget = currentBudget;
        else p2Budget = currentBudget;
    }

    // เรียกเมื่อจบครบทั้ง 2 ผู้เล่นแล้ว เพื่อขึ้นเทิร์นใหม่ของเกม
    public static void advanceGlobalTurn() {
        turnCount++;
    }

    // --- Budget Logic ---
    public static long getPlayerBudget() {
        // คืนค่าเงินของคนที่กำลังเล่นอยู่ (ปัดเศษเป็น long)
        return (long) ((currentPlayer == 1) ? p1Budget : p2Budget);
    }

    public static void pay(long amount) {
        // หักเงินคนปัจจุบัน
        if (currentPlayer == 1) {
            p1Budget = Math.max(0, p1Budget - amount);
        } else {
            p2Budget = Math.max(0, p2Budget - amount);
        }
    }

    // --- Actions (Move, Shoot, Spawn) ---
    public static void move(String direction) {
        int[] nextPos = calculateOffset(currentRow, currentCol, direction);
        int r = nextPos[0];
        int c = nextPos[1];

        if (isValidPos(r, c) && field[r][c] == null) {
            // ย้าย Unit
            field[r][c] = field[currentRow][currentCol];
            field[currentRow][currentCol] = null;

            // อัปเดตตำแหน่ง
            currentRow = r;
            currentCol = c;
            System.out.println("P" + currentPlayer + " Moved " + direction + " to (" + r + "," + c + ")");
        } else {
            System.out.println("Move blocked.");
        }
    }

    public static void shoot(String direction, long expenditure) {
        int[] targetPos = calculateOffset(currentRow, currentCol, direction);
        int r = targetPos[0];
        int c = targetPos[1];

        if (isValidPos(r, c) && field[r][c] != null) {
            Unit target = field[r][c];
            long h = target.getHP();
            long d = target.getDefense();

            long damage = Math.max(1, expenditure - d);
            long newHp = Math.max(0, h - damage);

            target.takeDamage(h - newHp);
            System.out.println("P" + currentPlayer + " Shot " + direction + "! Target HP: " + target.getHP());

            if (target.isDead()) {
                field[r][c] = null;
                System.out.println("Target eliminated!");
            }
        } else {
            System.out.println("Shot missed.");
        }
    }

    public static void spawnUnit(int r, int c, long hp, long def) {
        if(isValidPos(r,c)) {
            // [UPDATE] ลบพารามิเตอร์ id ออก ให้ตรงกับ Constructor ใหม่ของ Unit
            field[r][c] = new Unit(hp, def, currentPlayer);

            // เพิ่มยอดการใช้ Spawn ของคนนั้นๆ
            usedSpawns[currentPlayer]++;
        }
    }

    // --- Sensing / Info ---
    public static long query(String type, String direction) {
        if (type.equals("nearby")) return calculateNearby(direction);
        else if (type.equals("opponent")) return findClosest(false); // หาศัตรู
        else if (type.equals("ally")) return findClosest(true);      // หาพวก
        return 0;
    }

    private static int calculateNearby(String direction) {
        int r = currentRow;
        int c = currentCol;
        int dist = 0;
        while (true) {
            int[] next = calculateOffset(r, c, direction);
            r = next[0]; c = next[1]; dist++;

            if (!isValidPos(r, c)) return 0; // สุดขอบกระดาน

            if (field[r][c] != null) {
                Unit u = field[r][c];
                int val = 100 * String.valueOf(u.getHP()).length() + 10 * String.valueOf(u.getDefense()).length() + dist;
                // ถ้า Owner ตรงกับ currentPlayer ให้คืนค่าลบ (เป็นพวกเดียวกัน)
                return (u.getOwner() == currentPlayer) ? -val : val;
            }
        }
    }

    private static int findClosest(boolean wantAlly) {
        int minVal = Integer.MAX_VALUE;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Unit u = field[r][c];
                if (u != null) {
                    boolean isSameTeam = (u.getOwner() == currentPlayer);
                    if (isSameTeam == wantAlly) {
                        int dir = calculateDirNum(currentRow, currentCol, r, c);
                        if (dir != 0) {
                            int dist = getDistance(currentRow, currentCol, r, c);
                            int val = dist * 10 + dir;
                            if (val < minVal) minVal = val;
                        }
                    }
                }
            }
        }
        return (minVal == Integer.MAX_VALUE) ? 0 : minVal;
    }

    // --- Helper Methods ---
    private static int[] calculateOffset(int r, int c, String direction) {
        int targetR = r; int targetC = c; boolean isEvenCol = (c % 2 == 0);
        switch (direction.toLowerCase()) {
            case "up" -> targetR--;
            case "down" -> targetR++;
            case "upleft" -> { targetC--; if (isEvenCol) targetR--; }
            case "upright" -> { targetC++; if (isEvenCol) targetR--; }
            case "downleft" -> { targetC--; if (!isEvenCol) targetR++; }
            case "downright" -> { targetC++; if (!isEvenCol) targetR++; }
        }
        return new int[]{targetR, targetC};
    }

    private static int calculateDirNum(int r1, int c1, int r2, int c2) {
        String[] directions = {"", "up", "upright", "downright", "down", "downleft", "upleft"};
        for (int d = 1; d <= 6; d++) {
            int r = r1; int c = c1;
            for (int dist = 0; dist < 15; dist++) {
                int[] next = calculateOffset(r, c, directions[d]);
                r = next[0]; c = next[1];
                if (!isValidPos(r, c)) break;
                if (r == r2 && c == c2) return d;
            }
        }
        return 0;
    }

    private static int getDistance(int r1, int c1, int r2, int c2) {
        String[] directions = {"", "up", "upright", "downright", "down", "downleft", "upleft"};
        int dir = calculateDirNum(r1, c1, r2, c2);
        if (dir == 0) return 999;
        int steps = 0;
        int r = r1, c = c1;
        while(r != r2 || c != c2) {
            int[] next = calculateOffset(r, c, directions[dir]);
            r = next[0]; c = next[1];
            steps++;
            if (steps > 20) break;
        }
        return steps;
    }

    private static boolean isValidPos(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    // --- Getters / Setters / Info ---
    public static void setMinionPos(int r, int c) { currentRow = r; currentCol = c; }
    public static int getCurrentRow() { return currentRow; }
    public static int getCurrentCol() { return currentCol; }
    public static long getMaxTurns() { return GameConfig.max_turns; }
    public static int getMaxBudget() { return (int) GameConfig.max_budget; }

    // ดึงโควตา Spawn ที่เหลือของคนปัจจุบัน
    public static int getRemainingSpawns() {
        return (int) (GameConfig.max_spawns - usedSpawns[currentPlayer]);
    }

    // คำนวณ Rate เพื่อแสดงผล (ใช้ Logic เดียวกับ processTurnIncome)
    public static int getInterestRate() {
        double budget = getPlayerBudget();
        if (budget <= 0) return 0;
        double b = GameConfig.interest_pct;
        double t = turnCount;
        return (int) (b * Math.log10(budget) * Math.log(t));
    }

    // สำหรับ Main Loop ดึง Unit ไปรัน
    public static Unit getUnitAt(int r, int c) {
        if (!isValidPos(r, c)) return null;
        return field[r][c];
    }

    // หาตำแหน่ง Unit ทั้งหมดของผู้เล่นคนนั้น
    public static List<int[]> getPlayerUnitPositions(int player) {
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] != null && field[r][c].getOwner() == player) {
                    positions.add(new int[]{r, c});
                }
            }
        }
        return positions;
    }
}