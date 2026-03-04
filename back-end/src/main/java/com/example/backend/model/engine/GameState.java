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
    private List<Unit> units;           // เก็บ Minion ทุกตัวบนกระดาน
    private Map<String, Long> globalVars; // เก็บตัวแปร Global สำหรับระบบ AST Parser

    private int p1Budget;               // เงินของ Player 1
    private int p2Budget;               // เงินของ Player 2

    private int currentTurn;            // เทิร์นปัจจุบัน
    private int maxTurns;               // จำนวนเทิร์นสูงสุดก่อนหมดเวลา (Time Out)

    // ข้อมูลขนาดกระดาน (ถ้าเกมคุณเป็น Hexagon หรือ Grid)
    private int boardRows;
    private int boardCols;

    // ==========================================
    // 2. Constructor (กำหนดค่าเริ่มต้นตอนเริ่มเกม)
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
    }

    // ==========================================
    // 3. ฟังก์ชันจัดการ Minion (Helper Methods)
    // ==========================================

    // เพิ่ม Minion ลงกระดาน
    public void addUnit(Unit unit) {
        this.units.add(unit);
    }

    // ลบ Minion ที่ตายแล้วออกจากกระดาน (เอาไว้เรียกใช้ตอนจบรอบ)
    public void cleanUpDeadUnits() {
        this.units.removeIf(Unit::isDead);
    }

    // ค้นหาว่าในช่อง (row, col) มี Minion ยืนอยู่ไหม (มีประโยชน์มากเวลาทำคำสั่ง Move หรือ Shoot)
    public Unit getUnitAt(int row, int col) {
        for (Unit u : units) {
            if (u.isAlive() && u.getRow() == row && u.getCol() == col) {
                return u;
            }
        }
        return null; // ถ้าไม่มีใครยืนอยู่ คืนค่า null
    }

    // ==========================================
    // 4. ระบบตัดสินผลแพ้ชนะ (Win Conditions)
    // ==========================================

    /**
     * ฟังก์ชันที่ 1: ใช้เช็คทุกครั้งหลังจบเทิร์นปกติ (หาคนตายยกแผง)
     */
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

        return MatchResult.ONGOING; // ยังรอดทั้งคู่ เล่นต่อไป
    }

    /**
     * ฟังก์ชันที่ 2: ใช้เช็ค "เฉพาะตอนเทิร์นหมด (Time Out)"
     * ลำดับการตัดสิน: จำนวน Minion -> HP รวม -> Budget
     */
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

        // ด่านที่ 1: ใครเหลือ Minion น้อยกว่า... แพ้
        if (p1MinionCount > p2MinionCount) return MatchResult.PLAYER1_WINS;
        if (p2MinionCount > p1MinionCount) return MatchResult.PLAYER2_WINS;

        // ด่านที่ 2: (ถ้าจำนวนเท่ากัน) HP รวมใครน้อยกว่า... แพ้
        if (p1TotalHp > p2TotalHp) return MatchResult.PLAYER1_WINS;
        if (p2TotalHp > p1TotalHp) return MatchResult.PLAYER2_WINS;

        // ด่านที่ 3: (ถ้า HP รวมเท่ากันอีก) Budget ใครน้อยกว่า... แพ้
        if (this.p1Budget > this.p2Budget) return MatchResult.PLAYER1_WINS;
        if (this.p2Budget > this.p1Budget) return MatchResult.PLAYER2_WINS;

        // ถ้าเท่ากันหมดให้เสมอ
        return MatchResult.DRAW;
    }
}