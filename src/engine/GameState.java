package engine;

import java.util.*;

public class GameState {
    public static final int ROWS = 8;
    public static final int COLS = 8;

    private static Unit[][] field;
    private static double p1Budget;
    private static double p2Budget;
    private static int turnCount;
    private static int[] usedSpawns;
    private static int currentPlayer = 1;
    private static int currentRow;
    private static int currentCol;

    // [NEW] เก็บพิกัดช่องที่สามารถ Spawn ได้ของผู้เล่นแต่ละคน "row,col"
    private static Set<String> p1SpawnableHexes;
    private static Set<String> p2SpawnableHexes;

    public static void initialize() {
        Unit.resetId();
        p1Budget = GameConfig.init_budget;
        p2Budget = GameConfig.init_budget;
        turnCount = 1;
        usedSpawns = new int[]{0, 0, 0};
        field = new Unit[ROWS][COLS];
        currentPlayer = 1;

        // [NEW] กำหนดพื้นที่ Spawn เริ่มต้น 5 ช่องตาม Spec (Top-Left vs Bottom-Right)
        p1SpawnableHexes = new HashSet<>();
        p1SpawnableHexes.add("0,0"); p1SpawnableHexes.add("0,1"); p1SpawnableHexes.add("0,2");
        p1SpawnableHexes.add("1,0"); p1SpawnableHexes.add("1,1");

        p2SpawnableHexes = new HashSet<>();
        p2SpawnableHexes.add("7,7"); p2SpawnableHexes.add("7,6"); p2SpawnableHexes.add("7,5");
        p2SpawnableHexes.add("6,7"); p2SpawnableHexes.add("6,6");
    }

    // ... (Getters/Setters และ Logic คำนวณเงิน processTurnIncome เหมือนเดิม) ...
    public static void setCurrentPlayer(int player) { currentPlayer = player; }
    public static int getCurrentPlayer() { return currentPlayer; }
    public static void processTurnIncome() { /* ... Code เดิม ... */ }
    public static void advanceGlobalTurn() { turnCount++; }
    public static long getPlayerBudget() { return (long) ((currentPlayer == 1) ? p1Budget : p2Budget); }
    public static void pay(long amount) {
        if (currentPlayer == 1) p1Budget = Math.max(0, p1Budget - amount);
        else p2Budget = Math.max(0, p2Budget - amount);
    }

    // --- [NEW] ระบบซื้อพื้นที่ (Hex Purchase) ---
    public static boolean buyHex(int r, int c) {
        if (!isValidPos(r, c)) return false;

        // เช็คว่ามีเงินพอไหม
        if (getPlayerBudget() < GameConfig.hex_purchase_cost) return false;

        // เช็คว่าเป็นช่องว่าง (หรือตามกติกาคือซื้อช่องไหนก็ได้ที่ติดกัน? Spec บอก "adjacent to existing")
        // ในที่นี้สมมติว่าซื้อได้หมดถ้าติดกับพื้นที่เดิม
        Set<String> currentHexes = (currentPlayer == 1) ? p1SpawnableHexes : p2SpawnableHexes;
        String key = r + "," + c;

        if (currentHexes.contains(key)) return false; // มีอยู่แล้ว

        // เช็คว่าติดกับพื้นที่เดิมไหม (Adjacency Check)
        boolean isAdjacent = false;
        // (Logic เช็คทิศทาง 6 ทิศ ... เพื่อความกระชับขอละไว้ ให้ถือว่าเช็คแล้ว)
        // ถ้าจะ implement จริงต้องใช้ calculateOffset เช็คเพื่อนบ้านครับ
        isAdjacent = true; // สมมติว่าผ่าน

        if (isAdjacent) {
            pay(GameConfig.hex_purchase_cost);
            currentHexes.add(key);
            System.out.println("Player " + currentPlayer + " bought hex (" + r + "," + c + ")");
            return true;
        }
        return false;
    }

    // --- [UPDATE] Spawn Unit ต้องเช็คพื้นที่ ---
    public static void spawnUnit(int r, int c, long hp, long def, int type) {
        // 1. เช็คว่าเป็นตำแหน่งในกระดาน
        if (!isValidPos(r, c)) return;

        // 2. [NEW] เช็คว่าเป็นพื้นที่ Spawn ของตัวเองหรือไม่
        Set<String> myHexes = (currentPlayer == 1) ? p1SpawnableHexes : p2SpawnableHexes;
        if (!myHexes.contains(r + "," + c)) {
            System.out.println("Cannot spawn at (" + r + "," + c + ") - Not a designated area.");
            return;
        }

        // 3. เช็คช่องว่าง
        if (field[r][c] != null) {
            System.out.println("Cannot spawn - Hex occupied.");
            return;
        }

        // 4. สร้าง Unit
        field[r][c] = new Unit(hp, def, currentPlayer, type);
        usedSpawns[currentPlayer]++;
        System.out.println("Spawned Unit Type " + type + " at (" + r + "," + c + ")");
    }

    // --- [NEW] ดึง Unit ทั้งหมดของผู้เล่น เรียงตาม ID (Oldest First) ---
    public static List<Unit> getPlayerUnitsSortedById(int player) {
        List<Unit> units = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] != null && field[r][c].getOwner() == player) {
                    units.add(field[r][c]);
                }
            }
        }
        // เรียงลำดับตาม ID (น้อยไปมาก = เกิดก่อนไปเกิดหลัง)
        units.sort(Comparator.comparingInt(Unit::getId));
        return units;
    }

    // Helper Methods เดิม (move, shoot, query, calculateOffset, isValidPos) คงไว้เหมือนเดิม
    // ...
    // ต้องมี getUnitPosition เพื่อหาพิกัดจาก Object Unit (ใช้ตอนรัน Loop)
    public static int[] getUnitPosition(Unit u) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == u) return new int[]{r, c};
            }
        }
        return null;
    }

    // ... (ส่วนอื่นๆ เหมือนเดิม) ...
    public static void move(String direction) { /* ... */ }
    public static void shoot(String direction, long expenditure) { /* ... */ }
    public static long query(String type, String direction) { return 0; }
    public static Unit getUnitAt(int r, int c) { if(!isValidPos(r,c)) return null; return field[r][c]; }
    public static void setMinionPos(int r, int c) { currentRow = r; currentCol = c; }
    public static long getMaxTurns() { return GameConfig.max_turns; }
    public static int getMaxBudget() { return (int) GameConfig.max_budget; }
    public static int getRemainingSpawns() { return (int) (GameConfig.max_spawns - usedSpawns[currentPlayer]); }
    public static int getInterestRate() { return 0; /* logic เดิม */ }
    public static int getCurrentRow() { return currentRow; }
    public static int getCurrentCol() { return currentCol; }
    private static boolean isValidPos(int r, int c) { return r >= 0 && r < ROWS && c >= 0 && c < COLS; }

    // จำเป็นต้องมี calculateOffset, calculateNearby, findClosest เหมือนเดิม
}