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
    private int currentPlayer ;
    private int p1TurnsPlayed ;
    private int p2TurnsPlayed ;

    private double p1BudgetExact ;
    private double p2BudgetExact ;

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

        this.currentPlayer  = 1  ; // start on player1
        this.p1TurnsPlayed = 0;
        this.p2TurnsPlayed = 0;

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

    private double calculateInterest (double budget , GameConfig config) {
        if (budget < 1) return 0 ;
        double b = config.getInterestPct(); // interest value from config file
        double m = budget ; // ! redundant
        double t = currentTurn ;

        // สูตรคำณวน interest
        double r = b * Math.log10(m) * Math.log(t) ;
        return m * r /100 ;
    }
    // ==========================================
    // ระบบรับเงินรายเทิร์นและคิดดอกเบี้ย
    // ==========================================
    public void applyTurnIncome(GameConfig config) {
        // old code
//        long p1Interest = (this.p1Budget * config.getInterestPct()) / 100;
//        long p2Interest = (this.p2Budget * config.getInterestPct()) / 100;
//
//        this.p1Budget += config.getTurnBudget() + p1Interest;
//        this.p2Budget += config.getTurnBudget() + p2Interest;
        // turnBudget + cal interest rate
        p1BudgetExact += config.getTurnBudget() ;
        p1BudgetExact += calculateInterest(p1BudgetExact, config);

        p2BudgetExact += config.getTurnBudget() ;
        p2BudgetExact += calculateInterest(p2BudgetExact, config);

        // ห้ามเกิน Max Budget
        if (this.p1BudgetExact > config.getMaxBudget()) this.p1BudgetExact = config.getMaxBudget();
        if (this.p2BudgetExact > config.getMaxBudget()) this.p2BudgetExact = config.getMaxBudget();

        // update for UI (แบบที่ตัดทศนิยมออกแล้ว )
        this.p1Budget = (long) p1BudgetExact ;
        this.p2Budget = (long) p2BudgetExact ;

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

    private int directionToNumber (String  direction) {
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
    // move หักเงินเสมอ ไม่ว่า จะ move ได้หรือไม่
    // เงินไม่พอ -> จบ strategy
    public boolean move(Unit currentUnit, String direction) {
        // หักเงืนก่อนเสมอ
        if(!pay(currentUnit , 1 )) {
            return false;
        }
        // คำณวนใหม่
        int[] offset = getDirectionOffset(direction);

        int newRow = currentUnit.getRow() + offset[0];
        int newCol = currentUnit.getCol() + offset[1];

        if (isWithinBounds(newRow, newCol) && getUnitAt(newRow, newCol) == null) {
            currentUnit.setRow(newRow);
            currentUnit.setCol(newCol);

        }
        // move ได้ไหมไม่สน ถือว่า command execute แล้ว
        return true ;
    }

    public void shoot(Unit currentUnit, String direction, long expenditure) {
        int[] offset = getDirectionOffset(direction);
//        int targetRow = currentUnit.getRow() + (offset[0] * (int)expenditure);
        int targetRow = currentUnit.getRow() + offset[0];
        int targetCol = currentUnit.getCol() + offset[1];

        // หักเงิน
        if (!pay(currentUnit, expenditure + 1)) return;

        if (isWithinBounds(targetRow , targetCol )) {
            Unit target  = getUnitAt(targetRow , targetCol ) ;

            if (target != null ) {
                // damage = max(1, expenditure - defense) ตาม spec
                long damage  = Math.max(1, expenditure - target.getDefense()) ;
                //  HP ใหม่ = max(0, h - damage) ตาม spec
                target.takeDamage(damage);
            }

        }


    }



    public long query(Unit currentUnit, String type, String direction) {
        // spec :  return value distance*10 + directionNumber
        // EX : เจอ opponent ห่าง2 ช่อง ด้านบน  ->  21

        if (type.equals("ally") || type.equals("opponent")) {

            long bestValue = Long.MAX_VALUE; // เก็บค่าดีสุด (น้อบที่สุด)
            boolean found = false;

            // วนหา 6 ทิศ
            String[] directions = {"up", "upright", "downright", "down", "downleft", "upleft"};

            for (String direct : directions) {
                int[] offset = getDirectionOffset(direct);
                int checkRow = currentUnit.getRow() + offset[0];
                int checkCol = currentUnit.getCol() + offset[1];
                long dist = 1;  // ระยะห่าง จาก cur unit

                while (isWithinBounds(checkRow, checkCol)) {
                    Unit other = getUnitAt(checkRow, checkCol);
                    if (other != null && other.isAlive()) {
                        boolean isAlly = other.getOwner() == currentUnit.getOwner();
                        boolean match = (type.equals("ally") && isAlly) || (type.equals("opponent") && !isAlly);
                        if (match) {
                            long val = dist * 10 + directionToNumber(direct);
                            // เก็บค่าน้อยสุด
                            if (val < bestValue) {
                                bestValue = val;
                                found = false;

                            }
                        }
                        break;
                    }
                    // ถ้ายังไม่เจอ เดินต่อในทิศเดิม
                    checkRow += offset[0];
                    checkCol += offset[1];
                    dist++;
                }
            }
            return found ? bestValue : 0;
            // case nearby
            // spec : return 100x + 10y +z
            // x: HP , y :defense , z : distance
            // ถ้าเป็น ally (ฝั่งเรา) -> ค่าติดลบ
        } else if (type.equals("nearby") && direction != null ){
            int[] offset =  getDirectionOffset(direction);
            int checkRow = currentUnit.getRow() + offset[0];
            int checkCol = currentUnit.getCol() + offset[1];
            long dist  = 1 ;
            // เดินตลอด ในทิศที่ระบุ
            while (isWithinBounds(checkRow, checkCol)) {
             Unit target = getUnitAt(checkRow, checkCol);

             if (target != null && target.isAlive()) {
                 int hpDigits  = String.valueOf(target.getHP()).length();
                 int defDigits  =  String.valueOf(target.getDefense()).length();

                 // คำนวณค่าตาม spec: 100x + 10y + z
                 long result = 100L * hpDigits + 10L * defDigits + dist ;

                 // ally -> -(value)
                 if (target.getOwner() == currentUnit.getOwner()) {
                     return -result;
                 }
                 return result ;
             }
             // เดินไม่เจอ Unit เดินต่อไป
                checkRow += offset[0];
             checkCol += offset[1];
             dist++ ;
            }
        }
        return 0 ;
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

    // ======== add  method for summary Game =========
    // นับจำนวน minion ที่เหลือของแต่ละฝั่ง
    public int countActiveUnits(int owner ) {
        int activeUnits = 0;
        for (Unit unit : this.units) {
            if (unit.isAlive() && unit.getOwner() == owner )   activeUnits ++;

        }
        return activeUnits;
    }

    // total Hp of All minion
    public int sumHP(int owner ) {
        int totalHp = 0 ;
        for (Unit  unit : this.units) {
            if (unit.isAlive()  && unit.getOwner() == owner )   totalHp += unit.getHP();
        }
        return totalHp;
    }
    // total Hex ที่แต่ละผู้เล่น เป็น เจ้าของ
    public int countOwnerHexs (int owner ) {
        int countHex = 0;
        for (int r = 0; r < boardRows; r++)
            for (int c = 0; c < boardCols; c++) {
                if (hexOwnership[r][c] == owner) countHex++;

            }
        return countHex;
    }



}