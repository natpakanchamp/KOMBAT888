package com.example.backend.model.engine;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class GameState {
    public final int ROWS = 8;
    public final int COLS = 8;

    private Unit[][] field;
    private double p1Budget;
    private double p2Budget;
    private int turnCount;
    private int[] usedSpawns;
    private int currentPlayer = 1;
    private int currentRow;
    private int currentCol;

    private Set<String> p1SpawnableHexes;
    private Set<String> p2SpawnableHexes;

    // Constructor สร้างกระดานใหม่ของแต่ละห้อง
    public GameState() {
        p1Budget = GameConfig.init_budget;
        p2Budget = GameConfig.init_budget;
        turnCount = 1;
        usedSpawns = new int[]{0, 0, 0};
        field = new Unit[ROWS][COLS];
        currentPlayer = 1;

        p1SpawnableHexes = new HashSet<>(Arrays.asList("0,0", "0,1", "0,2", "1,0", "1,1"));
        p2SpawnableHexes = new HashSet<>(Arrays.asList("7,7", "7,6", "7,5", "6,7", "6,6"));
    }

    public void advanceGlobalTurn() { turnCount++; }

    public long getPlayerBudget() {
        return (long) ((currentPlayer == 1) ? p1Budget : p2Budget);
    }

    public void setMinionPos(int r, int c) {
        currentRow = r;
        currentCol = c;
    }

    public long getMaxTurns() { return GameConfig.max_turns; }
    public int getMaxBudget() { return (int) GameConfig.max_budget; }
    public int getInterestRate() { return (int) GameConfig.interest_pct; }

    public int getRemainingSpawns() {
        return (int) (GameConfig.max_spawns - usedSpawns[currentPlayer]);
    }

    public void pay(long amount) {
        if (currentPlayer == 1) p1Budget = Math.max(0, p1Budget - amount);
        else p2Budget = Math.max(0, p2Budget - amount);
    }

    public void processTurnIncome() {
        if (turnCount == 1) return;

        double currentB = (currentPlayer == 1) ? p1Budget : p2Budget;
        double m = GameConfig.max_budget;
        double t = turnCount;
        double r = GameConfig.interest_pct;

        double interest = currentB * (r / 100.0) * Math.log10(m) * Math.log(t);

        if (currentPlayer == 1) {
            p1Budget = Math.min(GameConfig.max_budget, p1Budget + interest);
        } else {
            p2Budget = Math.min(GameConfig.max_budget, p2Budget + interest);
        }
    }

    public boolean buyHex(int r, int c) {
        if (!isValidPos(r, c)) return false;
        if (getPlayerBudget() < GameConfig.hex_purchase_cost) return false;

        Set<String> currentHexes = (currentPlayer == 1) ? p1SpawnableHexes : p2SpawnableHexes;
        String key = r + "," + c;

        if (currentHexes.contains(key)) return false;

        boolean isAdjacent = false;
        String[] directions = {"up", "down", "upleft", "upright", "downleft", "downright"};
        for (String dir : directions) {
            int[] neighbor = calculateOffset(r, c, dir);
            if (neighbor != null && currentHexes.contains(neighbor[0] + "," + neighbor[1])) {
                isAdjacent = true;
                break;
            }
        }

        if (isAdjacent) {
            pay(GameConfig.hex_purchase_cost);
            currentHexes.add(key);
            System.out.println("Player " + currentPlayer + " successfully bought hex (" + r + "," + c + ")");
            return true;
        }
        return false;
    }

    public void spawnUnit(int r, int c, long hp, long def, int type) {
        if (!isValidPos(r, c)) return;

        Set<String> myHexes = (currentPlayer == 1) ? p1SpawnableHexes : p2SpawnableHexes;
        if (!myHexes.contains(r + "," + c)) {
            System.out.println("Cannot spawn at (" + r + "," + c + ") - Not your designated area.");
            return;
        }

        if (field[r][c] != null) return;
        if (getPlayerBudget() < GameConfig.spawn_cost) return;

        pay(GameConfig.spawn_cost);
        field[r][c] = new Unit(hp, def, currentPlayer, type);
        usedSpawns[currentPlayer]++;
    }

    public List<Unit> getPlayerUnitsSortedById(int player) {
        List<Unit> units = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] != null && field[r][c].getOwner() == player) {
                    units.add(field[r][c]);
                }
            }
        }
        units.sort(Comparator.comparingInt(Unit::getId));
        return units;
    }

    public int[] getUnitPosition(Unit u) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (field[r][c] == u) return new int[]{r, c};
            }
        }
        return null;
    }

    public Unit getUnitAt(int r, int c) {
        if(!isValidPos(r, c)) return null;
        return field[r][c];
    }

    public void move(String direction) {
        pay(1);
        int[] nextPos = calculateOffset(currentRow, currentCol, direction);
        if (nextPos != null) {
            int nr = nextPos[0];
            int nc = nextPos[1];
            if (field[nr][nc] == null) {
                field[nr][nc] = field[currentRow][currentCol];
                field[currentRow][currentCol] = null;
                currentRow = nr;
                currentCol = nc;
            }
        }
    }

    public void shoot(String direction, long expenditure) {
        long cost = 1 + expenditure;
        pay(cost);

        int[] targetPos = findClosest(currentRow, currentCol, direction);
        if (targetPos != null) {
            Unit target = field[targetPos[0]][targetPos[1]];
            long damage = expenditure;
            target.takeDamage(damage);

            if (target.isDead()) {
                field[targetPos[0]][targetPos[1]] = null;
            }
        }
    }

    public long query(String type, String direction) {
        if (type.equals("nearby")) {
            return calculateNearby(currentRow, currentCol, direction);
        }
        return 0;
    }

    private int calculateNearby(int r, int c, String dir) {
        int dist = 1;
        int[] currentPos = {r, c};

        while (dist < Math.max(ROWS, COLS)) {
            int[] nextPos = calculateOffset(currentPos[0], currentPos[1], dir);
            if (nextPos == null) break;

            Unit found = field[nextPos[0]][nextPos[1]];
            if (found != null) {
                if (found.getOwner() == currentPlayer) return -dist;
                else return dist;
            }
            currentPos = nextPos;
            dist++;
        }
        return 0;
    }

    private int[] findClosest(int r, int c, String dir) {
        int[] currentPos = {r, c};
        while (true) {
            int[] nextPos = calculateOffset(currentPos[0], currentPos[1], dir);
            if (nextPos == null) return null;
            if (field[nextPos[0]][nextPos[1]] != null) return nextPos;
            currentPos = nextPos;
        }
    }

    public int[] calculateOffset(int r, int c, String dir) {
        int nr = r;
        int nc = c;
        boolean evenRow = (r % 2 == 0);

        switch (dir) {
            case "up" -> nr--;
            case "down" -> nr++;
            case "upleft" -> { if(evenRow) { nr--; nc--; } else { nr--; } }
            case "upright" -> { if(evenRow) { nr--; } else { nr--; nc++; } }
            case "downleft" -> { if(evenRow) { nr++; nc--; } else { nr++; } }
            case "downright" -> { if(evenRow) { nr++; } else { nr++; nc++; } }
            default -> { return null; }
        }

        if (isValidPos(nr, nc)) return new int[]{nr, nc};
        return null;
    }

    private boolean isValidPos(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }
}