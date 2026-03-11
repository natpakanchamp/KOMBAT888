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

    private long p1Budget;
    private long p2Budget;

    private int currentTurn;
    private long maxTurns;

    private int boardRows;
    private int boardCols;

    private int[][] hexOwnership;

    // 🌟 ตัวแปรที่ต้องมีไว้ให้ระบบ AST (Variable.java) เรียกใช้งาน
    private long interestRate;
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

        // 🌟 ดึงค่าจาก Config มาเก็บไว้ให้ระบบเกมและ AST ใช้
        this.interestRate = config.getInterestPct();
        this.maxBudget = config.getMaxBudget();
        this.p1RemainingSpawns = config.getMaxSpawns();
        this.p2RemainingSpawns = config.getMaxSpawns();

        // รับเงินตั้งต้นจาก Config
        this.p1Budget = config.getInitBudget();
        this.p2Budget = config.getInitBudget();
        this.currentTurn = 1;

        this.units = new ArrayList<>();
        this.globalVars = new HashMap<>();

        this.hexOwnership = new int[boardRows][boardCols];

        // แจกพื้นที่ P1
        if (boardRows > 1 && boardCols > 2) {
            this.hexOwnership[0][0] = 1; this.hexOwnership[0][1] = 1; this.hexOwnership[0][2] = 1;
            this.hexOwnership[1][0] = 1; this.hexOwnership[1][1] = 1;
        }

        // แจกพื้นที่ P2
        if (boardRows > 1 && boardCols > 2) {
            int lastRow = boardRows - 1; int lastCol = boardCols - 1;
            this.hexOwnership[lastRow - 1][lastCol - 2] = 2; this.hexOwnership[lastRow - 1][lastCol - 1] = 2;
            this.hexOwnership[lastRow][lastCol - 2] = 2;     this.hexOwnership[lastRow][lastCol - 1] = 2;
            this.hexOwnership[lastRow][lastCol] = 2;
        }
    }

    // ==========================================
    // ระบบรับเงินรายเทิร์นและคิดดอกเบี้ย
    // ==========================================
    public void applyTurnIncome(GameConfig config) {
        long p1Interest = (this.p1Budget * config.getInterestPct()) / 100;
        long p2Interest = (this.p2Budget * config.getInterestPct()) / 100;

        this.p1Budget += config.getTurnBudget() + p1Interest;
        this.p2Budget += config.getTurnBudget() + p2Interest;

        // ห้ามเกิน Max Budget
        if (this.p1Budget > config.getMaxBudget()) this.p1Budget = config.getMaxBudget();
        if (this.p2Budget > config.getMaxBudget()) this.p2Budget = config.getMaxBudget();
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
        if (player == 1 && p1Budget >= cost) { p1Budget -= cost; hexOwnership[row][col] = player; return true; }
        else if (player == 2 && p2Budget >= cost) { p2Budget -= cost; hexOwnership[row][col] = player; return true; }
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
        if (unit.getOwner() == 1 && this.p1Budget >= cost) { this.p1Budget -= cost; return true; }
        else if (unit.getOwner() == 2 && this.p2Budget >= cost) { this.p2Budget -= cost; return true; }
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

    public void move(Unit currentUnit, String direction) {
        int[] offset = getDirectionOffset(direction);
        int newRow = currentUnit.getRow() + offset[0]; int newCol = currentUnit.getCol() + offset[1];
        if (isWithinBounds(newRow, newCol) && getUnitAt(newRow, newCol) == null) {
            currentUnit.setRow(newRow); currentUnit.setCol(newCol);
        }
    }

    public void shoot(Unit currentUnit, String direction, long expenditure) {
        int[] offset = getDirectionOffset(direction);
        int targetRow = currentUnit.getRow() + (offset[0] * (int)expenditure);
        int targetCol = currentUnit.getCol() + (offset[1] * (int)expenditure);
        if (isWithinBounds(targetRow, targetCol)) {
            Unit target = getUnitAt(targetRow, targetCol);
            if (target != null) {
                long damage = 10;
                long finalDamage = Math.max(1, damage - target.getDefense());
                target.takeDamage(finalDamage);
            }
        }
    }

    public long query(Unit currentUnit, String type, String direction) {
        long shortestDistance = Long.MAX_VALUE; boolean found = false;
        if (type.equals("ally") || type.equals("opponent")) {
            for (Unit other : units) {
                if (!other.isAlive() || other == currentUnit) continue;
                boolean isAlly = (other.getOwner() == currentUnit.getOwner());
                if ((type.equals("ally") && isAlly) || (type.equals("opponent") && !isAlly)) {
                    long dist = Math.abs(currentUnit.getRow() - other.getRow()) + Math.abs(currentUnit.getCol() - other.getCol());
                    if (dist < shortestDistance) { shortestDistance = dist; found = true; }
                }
            }
            return found ? shortestDistance : 0;
        } else if (type.equals("nearby") && direction != null) {
            int[] offset = getDirectionOffset(direction);
            int checkRow = currentUnit.getRow() + offset[0]; int checkCol = currentUnit.getCol() + offset[1];
            long distance = 1;
            while (isWithinBounds(checkRow, checkCol)) {
                if (getUnitAt(checkRow, checkCol) != null) return distance;
                checkRow += offset[0]; checkCol += offset[1]; distance++;
            }
        }
        return 0;
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