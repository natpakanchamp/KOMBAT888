package com.example.backend.model.engine;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GameState {

    private List<Unit> units;
    private Map<String, Long> globalVars;
    // budget จริงแบบ ไม่ได้ตัด ทศนิยมออก
    private double p1BudgetExact;
    private double p2BudgetExact;

    private long p1Budget;
    private long p2Budget;

    private int currentTurn;
    private long maxTurns;

    private int boardRows;
    private int boardCols;

    private int[][] hexOwnership;

    // 🌟 ตัวแปรที่ต้องมีไว้ให้ระบบ AST (Variable.java) เรียกใช้งาน
    private long interestRate; // from config
    private long maxBudget;
    private long p1RemainingSpawns;
    private long p2RemainingSpawns;

    // ==========================================
    // Constructor รับ GameConfig
    // ==========================================
    public GameState(int boardRows, int boardCols, GameConfig config) {
        this.boardRows = boardRows;
        this.boardCols = boardCols;
        this.maxTurns = config.getMaxTurns();

        //  ดึงค่าจาก Config มาเก็บไว้ให้ระบบเกมและ AST ใช้
        this.interestRate = config.getInterestPct();
        this.maxBudget = config.getMaxBudget();
        this.p1RemainingSpawns = config.getMaxSpawns();
        this.p2RemainingSpawns = config.getMaxSpawns();

        // รับเงินตั้งต้นจาก Config
        this.p1Budget = config.getInitBudget();
        this.p2Budget = config.getInitBudget();
        this.p1BudgetExact = config.getInitBudget();
        this.p2BudgetExact = config.getInitBudget();

        this.currentTurn = 1;

        this.units = new ArrayList<>();
        this.globalVars = new HashMap<>();

        this.hexOwnership = new int[boardRows][boardCols];

        // แจกพื้นที่ P1
        if (boardRows > 1 && boardCols > 2) {
            this.hexOwnership[0][0] = 1; this.hexOwnership[0][1] = 1; this.hexOwnership[0][2] = 1;
            this.hexOwnership[1][0] = 1; this.hexOwnership[1][1] = 1;
        }

        // แจกพื้นที่เริ่มต้น  P2
        if (boardRows > 1 && boardCols > 2) {
            int lastRow = boardRows - 1; int lastCol = boardCols - 1;
            this.hexOwnership[lastRow - 1][lastCol - 2] = 2; this.hexOwnership[lastRow - 1][lastCol - 1] = 2;
            this.hexOwnership[lastRow][lastCol - 2] = 2;     this.hexOwnership[lastRow][lastCol - 1] = 2;
            this.hexOwnership[lastRow][lastCol] = 2;
        }
    }

    // Helper Method  : คำณวนดอกเบี้ยตาม Spec
    private double calculateInterest(double budget, GameConfig config) {
        if (budget < 1) return 0;
        double b = config.getInterestPct(); // interest value from config file
        double m = budget; // ! redundant
        double t = currentTurn;

        // สูตรคำณวน interest
        double r = b * Math.log10(m) * Math.log(t);
        return m * r / 100;
    }

    // ==========================================
    // ระบบรับเงินรายเทิร์นและคิดดอกเบี้ย
    // ==========================================
    public void applyTurnIncome(GameConfig config) {
        p1BudgetExact += config.getTurnBudget();
        p1BudgetExact += calculateInterest(p1BudgetExact, config);

        p2BudgetExact += config.getTurnBudget();
        p2BudgetExact += calculateInterest(p2BudgetExact, config);

        // ห้ามเกิน Max Budget
        if (this.p1BudgetExact > config.getMaxBudget()) this.p1BudgetExact = config.getMaxBudget();
        if (this.p2BudgetExact > config.getMaxBudget()) this.p2BudgetExact = config.getMaxBudget();

        // update for UI (แบบที่ตัดทศนิยมออกแล้ว )
        this.p1Budget = (long) p1BudgetExact;
        this.p2Budget = (long) p2BudgetExact;
    }

    public List<int[]> getPurchasableHexes(int player) {
        List<int[]> purchasable = new ArrayList<>();
        boolean[][] visited = new boolean[boardRows][boardCols];
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                if (hexOwnership[r][c] == player) {
                    int[][] dirs = (r % 2 == 0) ? new int[][]{{-1,-1},{-1,0},{0,-1},{0,1},{1,-1},{1,0}} : new int[][]{{-1,0},{-1,1},{0,-1},{0,1},{1,0},{1,1}};
                    for (int[] d : dirs) {
                        int nr = r + d[0]; int nc = c + d[1];
                        if (isWithinBounds(nr, nc) && hexOwnership[nr][nc] == 0 && !visited[nr][nc]) {
                            purchasable.add(new int[]{nr, nc}); visited[nr][nc] = true;
                        }
                    }
                }
            }
        }
        return purchasable;
    }

    public boolean buyHex(int row, int col, int player, long cost) {
        if (player == 1 && p1Budget >= cost) { p1BudgetExact -= cost; hexOwnership[row][col] = player; return true; }
        else if (player == 2 && p2Budget >= cost) { p2BudgetExact -= cost; hexOwnership[row][col] = player; return true; }
        return false;
    }

    public void addUnit(Unit unit) { this.units.add(unit); }
    public void cleanUpDeadUnits() { this.units.removeIf(Unit::isDead); }
    public Unit getUnitAt(int row, int col) {
        for (Unit u : units) if (u.isAlive() && u.getRow() == row && u.getCol() == col) return u;
        return null;
    }
    public boolean isWithinBounds(int row, int col) { return row >= 0 && row < boardRows && col >= 0 && col < boardCols; }

    public boolean pay(Unit unit, long cost) {
        if (unit.getOwner() == 1 && this.p1Budget >= cost) { this.p1BudgetExact -= cost; return true; }
        else if (unit.getOwner() == 2 && this.p2Budget >= cost) { this.p2BudgetExact -= cost; return true; }
        return false;
    }

    private int[] getDirectionOffset(String direction) {
        return switch (direction.toLowerCase()) {
            case "up" -> new int[]{-1, 0}; case "down" -> new int[]{1, 0};
            case "left" -> new int[]{0, -1}; case "right" -> new int[]{0, 1};
            case "upleft" -> new int[]{-1, -1}; case "upright" -> new int[]{-1, 1};
            case "downleft" -> new int[]{1, -1}; case "downright" -> new int[]{1, 1};
            default -> new int[]{0, 0};
        };
    }

    private int directionToNumber(String direction) {
        return switch (direction.toLowerCase()){
            case "up"        -> 1;
            case "upright"   -> 2;
            case "downright" -> 3;
            case "down"      -> 4;
            case "downleft"  -> 5;
            case "upleft"    -> 6;
            default          -> 0;
        };
    }

    public boolean move(Unit currentUnit, String direction) {
        if(!pay(currentUnit, 1)) {
            return false;
        }
        int[] offset = getDirectionOffset(direction);

        int newRow = currentUnit.getRow() + offset[0];
        int newCol = currentUnit.getCol() + offset[1];

        if (isWithinBounds(newRow, newCol) && getUnitAt(newRow, newCol) == null) {
            currentUnit.setRow(newRow);
            currentUnit.setCol(newCol);
        }
        return true;
    }

    // 👇 ระบบยิง ให้ค้นหาเป้าหมายทะลุแมพตามทิศทาง
    public void shoot(Unit currentUnit, String direction, long expenditure) {
        int[] offset = getDirectionOffset(direction);
        int targetRow = currentUnit.getRow() + offset[0];
        int targetCol = currentUnit.getCol() + offset[1];

        // หักเงิน
        if (!pay(currentUnit, expenditure + 1)) return;

        // ให้กระสุนพุ่งไปตามทิศทางจนกว่าจะเจอ Unit หรือหลุดขอบกระดาน
        while (isWithinBounds(targetRow, targetCol)) {
            Unit target = getUnitAt(targetRow, targetCol);

            if (target != null && target.isAlive()) {
                // คำนวณดาเมจ
                long damage = Math.max(1, expenditure - target.getDefense());
                target.takeDamage(damage);
                // พอโดนเป้าหมายแล้ว กระสุนหายไปเลย
                break;
            }

            // ถ้าช่องว่าง ให้กระสุนพุ่งต่อไป
            targetRow += offset[0];
            targetCol += offset[1];
        }
    }

    public long query(Unit currentUnit, String type, String direction) {
        if (type.equals("ally") || type.equals("opponent")) {
            long bestValue = Long.MAX_VALUE;
            boolean found = false;

            String[] directions = {"up", "upright", "downright", "down", "downleft", "upleft"};

            for (String direct : directions) {
                int[] offset = getDirectionOffset(direct);
                int checkRow = currentUnit.getRow() + offset[0];
                int checkCol = currentUnit.getCol() + offset[1];
                long dist = 1;

                while (isWithinBounds(checkRow, checkCol)) {
                    Unit other = getUnitAt(checkRow, checkCol);
                    if (other != null && other.isAlive()) {
                        boolean isAlly = other.getOwner() == currentUnit.getOwner();
                        boolean match = (type.equals("ally") && isAlly) || (type.equals("opponent") && !isAlly);
                        if (match) {
                            long val = dist * 10 + directionToNumber(direct);
                            if (val < bestValue) {
                                bestValue = val;
                                found = true; // หาเจอแล้ว
                            }
                        }
                        break;
                    }
                    checkRow += offset[0];
                    checkCol += offset[1];
                    dist++;
                }
            }
            return found ? bestValue : 0;

        } else if (type.equals("nearby") && direction != null) {
            int[] offset = getDirectionOffset(direction);
            int checkRow = currentUnit.getRow() + offset[0];
            int checkCol = currentUnit.getCol() + offset[1];
            long dist = 1;

            while (isWithinBounds(checkRow, checkCol)) {
                Unit target = getUnitAt(checkRow, checkCol);

                if (target != null && target.isAlive()) {
                    int hpDigits = String.valueOf(target.getHP()).length();
                    int defDigits = String.valueOf(target.getDefense()).length();

                    long result = 100L * hpDigits + 10L * defDigits + dist;

                    if (target.getOwner() == currentUnit.getOwner()) {
                        return -result;
                    }
                    return result;
                }
                checkRow += offset[0];
                checkCol += offset[1];
                dist++;
            }
        }
        return 0;
    }

    // 👇 นับจำนวน Unit ที่ยังมีชีวิตอยู่
    public int countActiveUnits(int player) {
        int count = 0;
        for (Unit unit : this.units) {
            if (unit.getOwner() == player && unit.isAlive()) {
                count++;
            }
        }
        return count;
    }

    // 👇 หาผลรวม HP ของ Unit ที่ยังมีชีวิตอยู่
    public int sumHP(int player) {
        int totalHP = 0;
        for (Unit unit : this.units) {
            if (unit.getOwner() == player && unit.isAlive()) {
                totalHP += unit.getHP();
            }
        }
        return totalHP;
    }

    // 👇 นับจำนวนพื้นที่ (Hex) ที่ผู้เล่นครอบครองอยู่
    public int countOwnerHexs(int player) {
        int count = 0;
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                if (this.hexOwnership[r][c] == player) {
                    count++;
                }
            }
        }
        return count;
    }

    public MatchResult checkNormalWin() {
        boolean p1HasUnits = false; boolean p2HasUnits = false;
        for (Unit unit : this.units) {
            if (unit.isAlive()) {
                if (unit.getOwner() == 1) p1HasUnits = true;
                if (unit.getOwner() == 2) p2HasUnits = true;
            }
        }
        if (p1HasUnits && !p2HasUnits) return MatchResult.PLAYER1_WINS;
        if (!p1HasUnits && p2HasUnits) return MatchResult.PLAYER2_WINS;
        if (!p1HasUnits && !p2HasUnits) return MatchResult.DRAW;
        return MatchResult.ONGOING;
    }

    public MatchResult evaluateTimeOutWinner() {
        int p1Count = 0; int p2Count = 0; int p1Hp = 0; int p2Hp = 0;
        for (Unit unit : this.units) {
            if (unit.isAlive()) {
                if (unit.getOwner() == 1) { p1Count++; p1Hp += unit.getHP(); }
                else if (unit.getOwner() == 2) { p2Count++; p2Hp += unit.getHP(); }
            }
        }
        if (p1Count > p2Count) return MatchResult.PLAYER1_WINS;
        if (p2Count > p1Count) return MatchResult.PLAYER2_WINS;
        if (p1Hp > p2Hp) return MatchResult.PLAYER1_WINS;
        if (p2Hp > p1Hp) return MatchResult.PLAYER2_WINS;
        if (this.p1Budget > this.p2Budget) return MatchResult.PLAYER1_WINS;
        if (this.p2Budget > this.p1Budget) return MatchResult.PLAYER2_WINS;
        return MatchResult.DRAW;
    }
}