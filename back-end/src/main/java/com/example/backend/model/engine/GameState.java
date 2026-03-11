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

    // ==========================================
    // 1. ข้อมูลพื้นฐานของเกม (Game Data)
    // ==========================================
    private List<Unit> units;
    private Map<String, Long> globalVars;

    private int p1Budget;
    private int p2Budget;

    private int currentTurn;
    private int maxTurns;

    private int boardRows;
    private int boardCols;

    // 🌟 2. ตัวแปรสำหรับระบบ AST (Variables)
    private int interestRate;       // อัตราดอกเบี้ย
    private int maxBudget;          // งบสูงสุดที่เก็บได้
    private int p1RemainingSpawns;  // โควต้าเกิดใหม่ของ P1
    private int p2RemainingSpawns;  // โควต้าเกิดใหม่ของ P2

    // 🌟 3. ระบบโฉนดที่ดิน (0 = ว่าง, 1 = ของ P1, 2 = ของ P2)
    private int[][] hexOwnership;

    // ==========================================
    // 4. Constructor (กำหนดค่าเริ่มต้นตอนเริ่มเกม)
    // ==========================================
    public GameState(int boardRows, int boardCols, int maxTurns, int startingBudget) {
        this.boardRows = boardRows;
        this.boardCols = boardCols;
        this.maxTurns = maxTurns;

        this.p1Budget = startingBudget;
        this.p2Budget = startingBudget;
        this.currentTurn = 1;

        this.units = new ArrayList<>();
        this.globalVars = new HashMap<>();

        // กำหนดค่าเริ่มต้นให้กับตัวแปรพิเศษ
        this.interestRate = 5;
        this.maxBudget = 20000;
        this.p1RemainingSpawns = 10;
        this.p2RemainingSpawns = 10;

        // 🌟 สร้างกระดานและแจกพื้นที่ 5 ช่องแรกตามดีไซน์
        this.hexOwnership = new int[boardRows][boardCols];

        // แจกพื้นที่ Player 1 (ซ้ายบน 5 ช่อง)
        if (boardRows > 1 && boardCols > 2) {
            this.hexOwnership[0][0] = 1;
            this.hexOwnership[0][1] = 1;
            this.hexOwnership[0][2] = 1;
            this.hexOwnership[1][0] = 1;
            this.hexOwnership[1][1] = 1;
        }

        // แจกพื้นที่ Player 2 (ขวาล่าง 5 ช่อง)
        if (boardRows > 1 && boardCols > 2) {
            int lastRow = boardRows - 1;
            int lastCol = boardCols - 1;
            this.hexOwnership[lastRow - 1][lastCol - 2] = 2;
            this.hexOwnership[lastRow - 1][lastCol - 1] = 2;
            this.hexOwnership[lastRow][lastCol - 2] = 2;
            this.hexOwnership[lastRow][lastCol - 1] = 2;
            this.hexOwnership[lastRow][lastCol] = 2;
        }
    }

    // ==========================================
    // 5. ฟังก์ชันจัดการพื้นที่ (Hex)
    // ==========================================

    // ค้นหาช่องว่างที่อยู่ติดกับพื้นที่ของตัวเอง
    public List<int[]> getPurchasableHexes(int player) {
        List<int[]> purchasable = new ArrayList<>();
        boolean[][] visited = new boolean[boardRows][boardCols];

        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                if (hexOwnership[r][c] == player) {

                    // 🌟 ลอจิกแบบ Hexagon (Odd-R Layout: แถวคี่เยื้องขวา)
                    int[][] dirs;
                    if (r % 2 == 0) {
                        // ถ้าเป็นแถวคู่ (0, 2, 4...)
                        dirs = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
                    } else {
                        // ถ้าเป็นแถวคี่ (1, 3, 5...)
                        dirs = new int[][]{{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, 0}, {1, 1}};
                    }

                    for (int[] d : dirs) {
                        int nr = r + d[0];
                        int nc = c + d[1];
                        if (isWithinBounds(nr, nc) && hexOwnership[nr][nc] == 0 && !visited[nr][nc]) {
                            purchasable.add(new int[]{nr, nc});
                            visited[nr][nc] = true;
                        }
                    }
                }
            }
        }
        return purchasable;
    }

    // ฟังก์ชันสำหรับซื้อพื้นที่
    public boolean buyHex(int row, int col, int player, int cost) {
        if (player == 1 && p1Budget >= cost) {
            p1Budget -= cost;
            hexOwnership[row][col] = player;
            return true;
        } else if (player == 2 && p2Budget >= cost) {
            p2Budget -= cost;
            hexOwnership[row][col] = player;
            return true;
        }
        return false; // เงินไม่พอ
    }

    // ==========================================
    // 6. ฟังก์ชันจัดการ Minion และระบบเงิน
    // ==========================================
    public void addUnit(Unit unit) {
        this.units.add(unit);
    }

    public void cleanUpDeadUnits() {
        this.units.removeIf(Unit::isDead);
    }

    public Unit getUnitAt(int row, int col) {
        for (Unit u : units) {
            if (u.isAlive() && u.getRow() == row && u.getCol() == col) {
                return u;
            }
        }
        return null;
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < boardRows && col >= 0 && col < boardCols;
    }

    public boolean pay(Unit unit, int cost) {
        if (unit.getOwner() == 1 && this.p1Budget >= cost) {
            this.p1Budget -= cost;
            return true;
        } else if (unit.getOwner() == 2 && this.p2Budget >= cost) {
            this.p2Budget -= cost;
            return true;
        }
        return false;
    }

    // ==========================================
    // 7. ฟังก์ชันสำหรับ AST (Move, Shoot, Query)
    // ==========================================
    private int[] getDirectionOffset(String direction) {
        return switch (direction.toLowerCase()) {
            case "up" -> new int[]{-1, 0};
            case "down" -> new int[]{1, 0};
            case "left" -> new int[]{0, -1};
            case "right" -> new int[]{0, 1};
            case "upleft" -> new int[]{-1, -1};
            case "upright" -> new int[]{-1, 1};
            case "downleft" -> new int[]{1, -1};
            case "downright" -> new int[]{1, 1};
            default -> new int[]{0, 0};
        };
    }

    public void move(Unit currentUnit, String direction) {
        int[] offset = getDirectionOffset(direction);
        int newRow = currentUnit.getRow() + offset[0];
        int newCol = currentUnit.getCol() + offset[1];

        if (isWithinBounds(newRow, newCol) && getUnitAt(newRow, newCol) == null) {
            currentUnit.setRow(newRow);
            currentUnit.setCol(newCol);
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
        long shortestDistance = Long.MAX_VALUE;
        boolean found = false;

        if (type.equals("ally") || type.equals("opponent")) {
            for (Unit other : units) {
                if (!other.isAlive() || other == currentUnit) continue;

                boolean isAlly = (other.getOwner() == currentUnit.getOwner());
                if ((type.equals("ally") && isAlly) || (type.equals("opponent") && !isAlly)) {
                    long dist = Math.abs(currentUnit.getRow() - other.getRow()) +
                            Math.abs(currentUnit.getCol() - other.getCol());
                    if (dist < shortestDistance) {
                        shortestDistance = dist;
                        found = true;
                    }
                }
            }
            return found ? shortestDistance : 0;

        } else if (type.equals("nearby") && direction != null) {
            int[] offset = getDirectionOffset(direction);
            int checkRow = currentUnit.getRow() + offset[0];
            int checkCol = currentUnit.getCol() + offset[1];

            long distance = 1;
            while (isWithinBounds(checkRow, checkCol)) {
                if (getUnitAt(checkRow, checkCol) != null) {
                    return distance;
                }
                checkRow += offset[0];
                checkCol += offset[1];
                distance++;
            }
            return 0;
        }
        return 0;
    }

    // ==========================================
    // 8. ระบบตัดสินผลแพ้ชนะ (Win Conditions)
    // ==========================================
    public MatchResult checkNormalWin() {
        boolean p1HasUnits = false;
        boolean p2HasUnits = false;

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
        int p1MinionCount = 0;
        int p2MinionCount = 0;
        int p1TotalHp = 0;
        int p2TotalHp = 0;

        for (Unit unit : this.units) {
            if (unit.isAlive()) {
                if (unit.getOwner() == 1) {
                    p1MinionCount++;
                    p1TotalHp += unit.getHP();
                } else if (unit.getOwner() == 2) {
                    p2MinionCount++;
                    p2TotalHp += unit.getHP();
                }
            }
        }

        if (p1MinionCount > p2MinionCount) return MatchResult.PLAYER1_WINS;
        if (p2MinionCount > p1MinionCount) return MatchResult.PLAYER2_WINS;

        if (p1TotalHp > p2TotalHp) return MatchResult.PLAYER1_WINS;
        if (p2TotalHp > p1TotalHp) return MatchResult.PLAYER2_WINS;

        if (this.p1Budget > this.p2Budget) return MatchResult.PLAYER1_WINS;
        if (this.p2Budget > this.p1Budget) return MatchResult.PLAYER2_WINS;

        return MatchResult.DRAW;
    }
}