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

    private int currentPlayer;
    private int p1TurnsPlayed;
    private int p2TurnsPlayed;

    private double p1BudgetExact;
    private double p2BudgetExact;

    private long p1Budget;
    private long p2Budget;

    private int currentTurn;
    private long maxTurns;

    private int boardRows;
    private int boardCols;

    private int[][] hexOwnership;

    private long interestRate;
    private long maxBudget;
    private long p1RemainingSpawns;
    private long p2RemainingSpawns;

    public GameState(int boardRows, int boardCols, GameConfig config) {
        this.boardRows = boardRows;
        this.boardCols = boardCols;
        this.maxTurns = config.getMaxTurns();

        this.currentPlayer = 1;
        this.p1TurnsPlayed = 0;
        this.p2TurnsPlayed = 0;

        this.interestRate = config.getInterestPct();
        this.maxBudget = config.getMaxBudget();
        this.p1RemainingSpawns = config.getMaxSpawns();
        this.p2RemainingSpawns = config.getMaxSpawns();

        this.p1Budget = config.getInitBudget();
        this.p2Budget = config.getInitBudget();
        this.p1BudgetExact = config.getInitBudget();
        this.p2BudgetExact = config.getInitBudget();

        this.currentTurn = 1;

        this.units = new ArrayList<>();
        this.globalVars = new HashMap<>();
        this.hexOwnership = new int[boardRows][boardCols];

        if (boardRows > 1 && boardCols > 2) {
            this.hexOwnership[0][0] = 1;
            this.hexOwnership[0][1] = 1;
            this.hexOwnership[0][2] = 1;
            this.hexOwnership[1][0] = 1;
            this.hexOwnership[1][1] = 1;
        }

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

    private void syncDisplayedBudgets() {
        this.p1Budget = (long) this.p1BudgetExact;
        this.p2Budget = (long) this.p2BudgetExact;
    }

    public double getCurrentInterestPctForPlayer(int player) {
        double budget = (player == 1) ? p1BudgetExact : p2BudgetExact;
        int turnCount = (player == 1) ? (p1TurnsPlayed + 1) : (p2TurnsPlayed + 1);

        if (budget < 1 || turnCount <= 1) {
            return 0.0;
        }

        double base = this.interestRate;
        return base * Math.log10(budget) * Math.log(turnCount);
    }

    public void applyTurnIncome(int player, GameConfig config) {
        if (player == 1) {
            p1BudgetExact += config.getTurnBudget();
            double r = getCurrentInterestPctForPlayer(1);
            p1BudgetExact += p1BudgetExact * r / 100.0;

            if (p1BudgetExact > config.getMaxBudget()) {
                p1BudgetExact = config.getMaxBudget();
            }
        } else {
            p2BudgetExact += config.getTurnBudget();
            double r = getCurrentInterestPctForPlayer(2);
            p2BudgetExact += p2BudgetExact * r / 100.0;

            if (p2BudgetExact > config.getMaxBudget()) {
                p2BudgetExact = config.getMaxBudget();
            }
        }

        syncDisplayedBudgets();
    }

    public List<int[]> getPurchasableHexes(int player) {
        List<int[]> purchasable = new ArrayList<>();
        boolean[][] visited = new boolean[boardRows][boardCols];

        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                if (hexOwnership[r][c] != player) continue;

                boolean evenCol = (c % 2 == 0);
                int[][] dirs = evenCol
                        ? new int[][]{
                        {-1, 0}, {1, 0}, {0, -1}, {1, -1}, {0, 1}, {1, 1}
                }
                        : new int[][]{
                        {-1, 0}, {1, 0}, {-1, -1}, {0, -1}, {-1, 1}, {0, 1}
                };

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

        return purchasable;
    }

    public boolean canSpawnAt(int player, int row, int col) {
        if (!isWithinBounds(row, col)) return false;
        if (getUnitAt(row, col) != null) return false;
        return hexOwnership[row][col] == player;
    }

    public boolean buyHex(int row, int col, int player, long cost) {
        if (!isWithinBounds(row, col)) return false;
        if (hexOwnership[row][col] != 0) return false;

        boolean allowed = getPurchasableHexes(player).stream()
                .anyMatch(pos -> pos[0] == row && pos[1] == col);

        if (!allowed) return false;

        if (player == 1) {
            if (p1Budget < cost) return false;
            p1BudgetExact -= cost;
        } else {
            if (p2Budget < cost) return false;
            p2BudgetExact -= cost;
        }

        hexOwnership[row][col] = player;
        syncDisplayedBudgets();
        return true;
    }

    public void addUnit(Unit unit) {
        this.units.add(unit);
    }

    public void cleanUpDeadUnits() {
        this.units.removeIf(Unit::isDead);
    }

    public Unit getUnitAt(int row, int col) {
        for (Unit u : units) {
            if (u.isAlive() && u.getRow() == row && u.getCol() == col) return u;
        }
        return null;
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < boardRows && col >= 0 && col < boardCols;
    }

    public boolean pay(Unit unit, long cost) {
        if (unit.getOwner() == 1 && this.p1Budget >= cost) {
            this.p1BudgetExact -= cost;
            syncDisplayedBudgets();
            return true;
        } else if (unit.getOwner() == 2 && this.p2Budget >= cost) {
            this.p2BudgetExact -= cost;
            syncDisplayedBudgets();
            return true;
        }
        return false;
    }

    private int[] getDirectionOffsetByCol(int col, String direction) {
        boolean evenCol = (col % 2 == 0);

        if (evenCol) {
            return switch (direction.toLowerCase()) {
                case "up" -> new int[]{-1, 0};
                case "down" -> new int[]{1, 0};
                case "upleft" -> new int[]{0, -1};
                case "upright" -> new int[]{0, 1};
                case "downleft" -> new int[]{1, -1};
                case "downright" -> new int[]{1, 1};
                default -> new int[]{0, 0};
            };
        } else {
            return switch (direction.toLowerCase()) {
                case "up" -> new int[]{-1, 0};
                case "down" -> new int[]{1, 0};
                case "upleft" -> new int[]{-1, -1};
                case "upright" -> new int[]{-1, 1};
                case "downleft" -> new int[]{0, -1};
                case "downright" -> new int[]{0, 1};
                default -> new int[]{0, 0};
            };
        }
    }

    private int directionToNumber(String direction) {
        return switch (direction.toLowerCase()) {
            case "up" -> 1;
            case "upright" -> 2;
            case "downright" -> 3;
            case "down" -> 4;
            case "downleft" -> 5;
            case "upleft" -> 6;
            default -> 0;
        };
    }

    public boolean move(Unit currentUnit, String direction) {
        if (!pay(currentUnit, 1)) {
            return false;
        }

        int[] offset = getDirectionOffsetByCol(currentUnit.getCol(), direction);
        int newRow = currentUnit.getRow() + offset[0];
        int newCol = currentUnit.getCol() + offset[1];

        if (isWithinBounds(newRow, newCol) && getUnitAt(newRow, newCol) == null) {
            currentUnit.setRow(newRow);
            currentUnit.setCol(newCol);
        }

        return true;
    }

    public void shoot(Unit currentUnit, String direction, long expenditure) {
        if (!pay(currentUnit, expenditure + 1)) return;

        int[] offset = getDirectionOffsetByCol(currentUnit.getCol(), direction);
        int targetRow = currentUnit.getRow() + offset[0];
        int targetCol = currentUnit.getCol() + offset[1];

        if (isWithinBounds(targetRow, targetCol)) {
            Unit target = getUnitAt(targetRow, targetCol);

            if (target != null) {
                long damage = Math.max(1, expenditure - target.getDefense());
                target.takeDamage(damage);
            }
        }
    }

    public long query(Unit currentUnit, String type, String direction) {
        if (type.equals("ally") || type.equals("opponent")) {
            long bestValue = Long.MAX_VALUE;
            boolean found = false;

            String[] directions = {"up", "upright", "downright", "down", "downleft", "upleft"};

            for (String direct : directions) {
                int[] offset = getDirectionOffsetByCol(currentUnit.getCol(), direct);
                int checkRow = currentUnit.getRow() + offset[0];
                int checkCol = currentUnit.getCol() + offset[1];
                long dist = 1;

                while (isWithinBounds(checkRow, checkCol)) {
                    Unit other = getUnitAt(checkRow, checkCol);
                    if (other != null && other.isAlive()) {
                        boolean isAlly = other.getOwner() == currentUnit.getOwner();
                        boolean match = (type.equals("ally") && isAlly)
                                || (type.equals("opponent") && !isAlly);

                        if (match) {
                            long val = dist * 10 + directionToNumber(direct);
                            if (val < bestValue) {
                                bestValue = val;
                                found = true;
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
        }

        if (type.equals("nearby") && direction != null) {
            int[] offset = getDirectionOffsetByCol(currentUnit.getCol(), direction);
            int checkRow = currentUnit.getRow() + offset[0];
            int checkCol = currentUnit.getCol() + offset[1];
            long dist = 1;

            while (isWithinBounds(checkRow, checkCol)) {
                Unit target = getUnitAt(checkRow, checkCol);

                if (target != null && target.isAlive()) {
                    int hpDigits = String.valueOf(Math.abs(target.getHP())).length();
                    int defDigits = String.valueOf(Math.abs(target.getDefense())).length();

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
        int p1Count = 0;
        int p2Count = 0;
        int p1Hp = 0;
        int p2Hp = 0;

        for (Unit unit : this.units) {
            if (unit.isAlive()) {
                if (unit.getOwner() == 1) {
                    p1Count++;
                    p1Hp += unit.getHP();
                } else if (unit.getOwner() == 2) {
                    p2Count++;
                    p2Hp += unit.getHP();
                }
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

    public int countActiveUnits(int owner) {
        int activeUnits = 0;
        for (Unit unit : this.units) {
            if (unit.isAlive() && unit.getOwner() == owner) activeUnits++;
        }
        return activeUnits;
    }

    public int sumHP(int owner) {
        int totalHp = 0;
        for (Unit unit : this.units) {
            if (unit.isAlive() && unit.getOwner() == owner) totalHp += unit.getHP();
        }
        return totalHp;
    }

    public int countOwnerHexs(int owner) {
        int countHex = 0;
        for (int r = 0; r < boardRows; r++) {
            for (int c = 0; c < boardCols; c++) {
                if (hexOwnership[r][c] == owner) countHex++;
            }
        }

        return countHex;
    }
}