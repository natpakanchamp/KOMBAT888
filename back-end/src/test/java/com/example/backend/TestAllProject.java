package com.example.backend;

import com.example.backend.engine.GameEngine;
import com.example.backend.model.engine.*;
import com.example.backend.model.ast.*;
import com.example.backend.model.exception.EvalError;
import com.example.backend.dto.RoomDtos;
import org.junit.jupiter.api.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TestAllProject {


    private GameState state;
    private GameConfig config;


    @BeforeEach
    void setUp() {
        // โหลด config ค่า default
        config = GameConfig.loadFromFile("config.txt");
        // สร้างกระดาน 8x8 ตาม spec
        state = new GameState(8, 8, config);
        Unit.resetId();
    }

   // test game state
    @Nested
    @DisplayName("1. GameState — Board & HexOwnership")
    class GameStateTest {

        @Test
        @DisplayName("กระดานต้องเป็น 8x8 ตาม spec")
        void boardSizeShouldBe8x8() {
            assertEquals(8, state.getBoardRows());
            assertEquals(8, state.getBoardCols());
        }

        @Test
        @DisplayName("P1 เริ่มมีพื้นที่ 5 ช่องมุมซ้ายบน")
        void p1InitialHexOwnership() {
            int[][] p1Hexes = {{0,0},{0,1},{0,2},{1,0},{1,1}};
             for (int[] hex : p1Hexes) {
                  assertEquals(1, state.getHexOwnership()[hex[0]][hex[1]],
                        "P1 ควรมี hex [" + hex[0] + "," + hex[1] + "]");
            }
        }




        @Test
        @DisplayName("P2 เริ่มมีพื้นที่ 5 ช่องมุมขวาล่าง")
        void p2InitialHexOwnership() {
            int[][] p2Hexes = {{6,5},{6,6},{7,5},{7,6},{7,7}};
            for (int[] hex : p2Hexes) {
                assertEquals(2, state.getHexOwnership()[hex[0]][hex[1]],
                        "P2 ควรมี hex [" + hex[0] + "," + hex[1] + "]");
            }
        }

        @Test
        @DisplayName("Budget เริ่มต้นต้องตรงกับ config")
        void initialBudgetFromConfig() {
            assertEquals(config.getInitBudget(), state.getP1Budget());
            assertEquals(config.getInitBudget(), state.getP2Budget());
        }

        @Test
        @DisplayName("isWithinBounds ต้องทำงานถูกต้อง")
        void isWithinBoundsShouldWork() {
            assertTrue(state.isWithinBounds(0, 0));
            assertTrue(state.isWithinBounds(7, 7));
            assertFalse(state.isWithinBounds(-1, 0));
            assertFalse(state.isWithinBounds(8, 0));
            assertFalse(state.isWithinBounds(0, 8));
        }
    }

  // test Calculate interest rate
    @Nested
    @DisplayName("2. Interest Rate — ตาม spec r = b*log10(m)*ln(t)")
    class InterestRateTest {

        @Test
        @DisplayName("Turn 1 ไม่คิดดอกเบี้ย (ln(1)=0)")
        void noInterestOnTurn1() {
            long budgetBefore = state.getP1Budget();
            // turn 1 → ln(1) = 0 → r = 0 → interest = 0
            state.applyTurnIncome(1,config);
            // ได้แค่ turnBudget ไม่มีดอกเบี้ย
            assertEquals(budgetBefore + config.getTurnBudget(), state.getP1Budget());
        }

        @Test
        @DisplayName("Budget ไม่เกิน maxBudget หลัง applyTurnIncome")
        void budgetShouldNotExceedMax() {
            // ตั้ง budget ใกล้ max
            state.setP1BudgetExact(config.getMaxBudget() - 1);
            state.setP1Budget(config.getMaxBudget() - 1);
            state.setCurrentTurn(10);

            state.applyTurnIncome(1,config);

            assertTrue(state.getP1Budget() <= config.getMaxBudget(),
                    "Budget ต้องไม่เกิน maxBudget");
        }

        @Test
        @DisplayName("Budget < 1 ไม่คิดดอกเบี้ย ตาม spec")
        void noBudgetNoInterest() {
            state.setP1BudgetExact(0);
            state.setP1Budget(0);
            state.setCurrentTurn(5);
            state.applyTurnIncome(1,config);
            // ได้แค่ turnBudget
            assertEquals(config.getTurnBudget(), state.getP1Budget());
        }
    }

   //  test move command
    @Nested
    @DisplayName("3. Move Command — ตาม spec")
    class MoveTest {

        @Test
        @DisplayName("move หักเงิน 1 เสมอ ไม่ว่าจะย้ายได้หรือไม่")
        void moveAlwaysDeductsOneBudget() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            state.addUnit(p1);
            long budgetBefore = state.getP1Budget();

            state.move(p1, "down");

            assertEquals(budgetBefore - 1, state.getP1Budget(),
                    "move ต้องหักเงิน 1 เสมอ");
        }

        @Test
        @DisplayName("move ไปช่องว่างได้สำเร็จ")
        void moveToemptyHex() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            state.addUnit(p1);

            state.move(p1, "down");

            assertEquals(4, p1.getRow());
            assertEquals(3, p1.getCol());
        }



        @Test
        @DisplayName("move ไปช่องที่มี unit อื่น = no-op แต่ยังหักเงิน")
        void moveToOccupiedHexIsNoOp() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);
            long budgetBefore = state.getP1Budget();

            state.move(p1, "down");

            // ไม่ขยับ
            assertEquals(3, p1.getRow());
            // แต่หักเงิน
            assertEquals(budgetBefore - 1, state.getP1Budget());
        }

        @Test
        @DisplayName("move ออกนอกกระดาน = no-op แต่ยังหักเงิน")
        void moveOutOfBoundsIsNoOp() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            state.addUnit(p1);
            long budgetBefore = state.getP1Budget();

            state.move(p1, "up"); // ออกนอกกระดาน

            assertEquals(0, p1.getRow()); // ไม่ขยับ
            assertEquals(budgetBefore - 1, state.getP1Budget()); // หักเงิน
        }

        @Test
        @DisplayName(" move เมื่อเงินไม่พอ = จบ strategy")
        void moveWithNoBudgetEndsTurn() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            state.addUnit(p1);
            state.setP1Budget(0);
            state.setP1BudgetExact(0);

            boolean result = state.move(p1, "down");

            assertFalse(result, "move เมื่อเงินไม่พอต้องคืน false");
            assertEquals(3, p1.getRow()); // ไม่ขยับ
        }
    }

    // Tets Shoot cmd
    @Nested
    @DisplayName("4. Shoot Command — ตาม spec")
    class ShootTest {

        @Test
        @DisplayName("shoot หักเงิน expenditure+1 ตาม spec")
        void shootDeductsExpenditurePlusOne() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);
            long budgetBefore = state.getP1Budget();

            state.shoot(p1, "down", 10L);

            assertEquals(budgetBefore - 11, state.getP1Budget(),
                    "shoot ต้องหักเงิน expenditure+1 = 11");
        }

        @Test
        @DisplayName("shoot damage = max(1, expenditure - defense) ตาม spec")
        void shootDamageFormula() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            // defense = 1 (ส่งใน constructor)
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);
            long hpBefore = p2.getHP();

            // expenditure=10, defense=1 → damage = max(1, 10-1) = 9
            state.shoot(p1, "down", 10L);

            assertEquals(hpBefore - 9, p2.getHP(),
                    "damage ต้องเป็น max(1, expenditure-defense)");
        }

        @Test
        @DisplayName("shoot ยิงแค่ 1 ช่อง ไม่ใช่ expenditure ช่อง")
        void shootOnlyOneHex() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            // p2 อยู่ห่าง 2 ช่อง
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 5, 3);
            state.addUnit(p1);
            state.addUnit(p2);
            long hpBefore = p2.getHP();

            state.shoot(p1, "down", 10L);

            // p2 ไม่โดนเพราะอยู่ห่าง 2 ช่อง
            assertEquals(hpBefore, p2.getHP(),
                    "shoot ยิงแค่ 1 ช่อง unit ที่อยู่ห่าง 2 ช่องไม่โดน");
        }

        @Test
        @DisplayName("shoot เงินไม่พอ = no-op")
        void shootWithNoBudgetIsNoOp() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);
            state.setP1Budget(0);
            state.setP1BudgetExact(0);
            long hpBefore = p2.getHP();

            state.shoot(p1, "down", 10L);

            assertEquals(hpBefore, p2.getHP(), "เงินไม่พอ unit ไม่โดน");
        }

        @Test
        @DisplayName("unit HP เป็น 0 เมื่อถูกยิงจนตาย")
        void unitDiesWhenHpReachesZero() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);

            // ยิงแรงมากพอให้ตาย
            state.shoot(p1, "down", 200L);

            assertEquals(0, p2.getHP());
            assertTrue(p2.isDead());
        }
    }

    // Test query
    @Nested
    @DisplayName("5. Query Expression — ตาม spec")
    class QueryTest {

        @Test
        @DisplayName("opponent คืน distance*10 + directionNumber")
        void opponentReturnsDistanceAndDirection() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            // p2 อยู่ด้านล่าง (down=4) ห่าง 1 ช่อง → ควรคืน 14
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);

            long result = state.query(p1, "opponent", null);

            assertEquals(14, result, "opponent ห่าง 1 ช่อง ทิศ down(4) = 14");
        }

        @Test
        @DisplayName("opponent คืน 0 เมื่อไม่มี opponent")
        void opponentReturns0WhenNone() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            state.addUnit(p1);

            long result = state.query(p1, "opponent", null);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("nearby คืน 100x+10y+z สำหรับ opponent")
        void nearbyReturnsCorrectFormula() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            // p2 defense=1, HP=100, ห่าง 1 ช่อง
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1);
            state.addUnit(p2);

            long result = state.query(p1, "nearby", "down");

            // HP=100(3digits), defense=1(1digit), dist=1
            // 100*3 + 10*1 + 1 = 311
            assertEquals(311, result);
        }

        @Test
        @DisplayName("nearby คืนค่าติดลบสำหรับ ally")
        void nearbyReturnsNegativeForAlly() {
            Unit p1a = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            Unit p1b = new Unit(1L, 1, Unit.TYPE_SABER, 4, 3);
            state.addUnit(p1a);
            state.addUnit(p1b);

            long result = state.query(p1a, "nearby", "down");

            assertTrue(result < 0, "nearby ally ต้องคืนค่าติดลบ");
        }

        @Test
        @DisplayName("nearby คืน 0 เมื่อไม่มี unit ในทิศนั้น")
        void nearbyReturns0WhenNoUnit() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 3, 3);
            state.addUnit(p1);

            long result = state.query(p1, "nearby", "up");

            assertEquals(0, result);
        }
    }

   // Test win condition
    @Nested
    @DisplayName("6. Win Condition — ตาม spec")
    class WinConditionTest {

        @Test
        @DisplayName("ONGOING เมื่อทั้งสองฝั่งยังมี unit")
        void ongoingWhenBothHaveUnits() {
            state.addUnit(new Unit(1L, 1, Unit.TYPE_SABER, 0, 0));
            state.addUnit(new Unit(1L, 2, Unit.TYPE_SABER, 7, 7));

            assertEquals(MatchResult.ONGOING, state.checkNormalWin());
        }

        @Test
        @DisplayName("P1 ชนะเมื่อ P2 ไม่มี unit เหลือ")
        void p1WinsWhenP2HasNoUnits() {
            state.addUnit(new Unit(1L, 1, Unit.TYPE_SABER, 0, 0));

            assertEquals(MatchResult.PLAYER1_WINS, state.checkNormalWin());
        }

        @Test
        @DisplayName("P2 ชนะเมื่อ P1 ไม่มี unit เหลือ")
        void p2WinsWhenP1HasNoUnits() {
            state.addUnit(new Unit(1L, 2, Unit.TYPE_SABER, 7, 7));

            assertEquals(MatchResult.PLAYER2_WINS, state.checkNormalWin());
        }

        @Test
        @DisplayName("DRAW เมื่อทั้งสองฝั่งไม่มี unit")
        void drawWhenNoUnits() {
            assertEquals(MatchResult.DRAW, state.checkNormalWin());
        }

        @Test
        @DisplayName("Timeout: เปรียบเทียบจำนวน unit ก่อน")
        void timeoutWinnerByUnitCount() {
            state.addUnit(new Unit(1L, 1, Unit.TYPE_SABER, 0, 0));
            state.addUnit(new Unit(1L, 1, Unit.TYPE_SABER, 0, 1));
            state.addUnit(new Unit(1L, 2, Unit.TYPE_SABER, 7, 7));

            assertEquals(MatchResult.PLAYER1_WINS, state.evaluateTimeOutWinner());
        }

        @Test
        @DisplayName("Timeout: เปรียบเทียบ HP เมื่อจำนวน unit เท่ากัน")
        void timeoutWinnerByHP() {
            Unit p1 = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            Unit p2 = new Unit(1L, 2, Unit.TYPE_SABER, 7, 7);
            p2.takeDamage(50); // p2 HP = 50
            state.addUnit(p1);
            state.addUnit(p2);

            assertEquals(MatchResult.PLAYER1_WINS, state.evaluateTimeOutWinner());
        }

        @Test
        @DisplayName("Timeout: เปรียบเทียบ budget เมื่อ unit และ HP เท่ากัน")
        void timeoutWinnerByBudget() {
            state.addUnit(new Unit(1L, 1, Unit.TYPE_SABER, 0, 0));
            state.addUnit(new Unit(1L, 2, Unit.TYPE_SABER, 7, 7));
            state.setP1Budget(5000);
            state.setP2Budget(3000);

            assertEquals(MatchResult.PLAYER1_WINS, state.evaluateTimeOutWinner());
        }
    }

   // Test Unit
    @Nested
    @DisplayName("7. Unit — HP, damage, defense")
    class UnitTest {

        @Test
        @DisplayName("Unit เริ่มต้น HP = 100 ตาม spec")
        void unitInitialHp() {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            assertEquals(100, unit.getHP());
        }

        @Test
        @DisplayName("takeDamage ลด HP ไม่ต่ำกว่า 0")
        void takeDamageNotBelowZero() {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            unit.takeDamage(200);
            assertEquals(0, unit.getHP());
        }

        @Test
        @DisplayName("isDead คืน true เมื่อ HP = 0")
        void isDeadWhenHpZero() {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            unit.takeDamage(100);
            assertTrue(unit.isDead());
        }

        @Test
        @DisplayName("cleanUpDeadUnits ลบ unit ที่ตายออก")
        void cleanUpRemovesDeadUnits() {
            Unit alive = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            Unit dead = new Unit(1L, 2, Unit.TYPE_SABER, 7, 7);
            dead.takeDamage(100);
            state.addUnit(alive);
            state.addUnit(dead);

            state.cleanUpDeadUnits();

            assertEquals(1, state.getUnits().size());
            assertTrue(state.getUnits().contains(alive));
        }
    }

   // Test Buy Hex
    @Nested
    @DisplayName("8. BuyHex — ซื้อพื้นที่")
    class BuyHexTest {

        @Test
        @DisplayName("ซื้อ hex ติดกับอาณาเขต P1 ได้สำเร็จ")
        void p1CanBuyAdjacentHex() {
            long budgetBefore = state.getP1Budget();
            long cost = config.getHexPurchaseCost();

            boolean result = state.buyHex(0, 3, 1, cost);

            assertTrue(result);
            assertEquals(1, state.getHexOwnership()[0][3]);
            assertEquals(budgetBefore - cost, state.getP1Budget());
        }

        @Test
        @DisplayName("ซื้อ hex เมื่อเงินไม่พอ ล้มเหลว")
        void cannotBuyHexWithNoBudget() {
            state.setP1Budget(0);
            state.setP1BudgetExact(0);

            boolean result = state.buyHex(0, 3, 1, config.getHexPurchaseCost());

            assertFalse(result);
            assertEquals(0, state.getHexOwnership()[0][3]);
        }
    }

   // test while state 10000 รอบ
    @Nested
    @DisplayName("9. WhileStatement — infinite loop protection")
    class WhileStatementTest {

        @Test
        @DisplayName("while loop หยุดเมื่อรัน 10000 รอบ ตาม spec")
        void whileLoopStopsAt10000() throws EvalError {
            // condition ที่เป็น true เสมอ (1 > 0)
            Expr alwaysTrue = new NumberLit(1);
            // body ที่นับจำนวน iteration
            long[] counter = {0};
            Statement countBody = (s, u, lv, gv) -> counter[0]++;

            WhileStatement whileStmt = new WhileStatement(alwaysTrue, countBody);
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            state.addUnit(unit);

            whileStmt.execute(state, unit, new HashMap<>(), new HashMap<>());

            assertEquals(10000, counter[0],
                    "while loop ต้องหยุดที่ 10000 iteration ตาม spec");
        }
    }

    // Test Variable
    @Nested
    @DisplayName("10. Variable — Special Variables")
    class VariableTest {

        @Test
        @DisplayName("row คืน row ของ unit ปัจจุบัน")
        void rowReturnsCurrentUnitRow() throws EvalError {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 3, 5);
              state.addUnit(unit);

              Variable rowVar = new Variable("row");
            long result = rowVar.eval(state, unit, new HashMap<>(), new HashMap<>());

            assertEquals(3, result);
        }



        @Test
        @DisplayName("col คืน col ของ unit ปัจจุบัน")
        void colReturnsCurrentUnitCol() throws EvalError {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 3, 5);
            state.addUnit(unit);

            Variable colVar = new Variable("col");
            long result = colVar.eval(state, unit, new HashMap<>(), new HashMap<>());

            assertEquals(5, result);
        }

        @Test
        @DisplayName("Budget คืน budget ของ player ที่เป็นเจ้าของ unit")
        void budgetReturnsOwnerBudget() throws EvalError {
            Unit p1Unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            state.addUnit(p1Unit);
              state.setP1Budget(9999);

            Variable budgetVar = new Variable("Budget");
            long result = budgetVar.eval(state, p1Unit, new HashMap<>(), new HashMap<>());

            assertEquals(9999, result);
        }

        @Test
        @DisplayName("random คืนค่าระหว่าง 0-999 ตาม spec")
        void randomReturnsValueBetween0And999() throws EvalError {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
              state.addUnit(unit);

              Variable randomVar = new Variable("random");
            for (int i = 0; i < 100; i++) {
                long result = randomVar.eval(state, unit, new HashMap<>(), new HashMap<>());
                assertTrue(result >= 0 && result <= 999,
                        "random ต้องอยู่ระหว่าง 0-999 แต่ได้ " + result);
            }
        }

        @Test
        @DisplayName("ตัวแปร local เก็บค่าแยกตาม unit")
        void localVarsArePrivatePerUnit() throws EvalError {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            state.addUnit(unit);

            Map<String, Long> localVars = new HashMap<>();
            localVars.put("x", 42L);

            Variable xVar = new Variable("x");
            long result = xVar.eval(state, unit, localVars, new HashMap<>());

            assertEquals(42, result);


        }



        @Test
        @DisplayName("ตัวพิมพ์ใหญ่ = global variable")
        void uppercaseIsGlobal() throws EvalError {
            Unit unit = new Unit(1L, 1, Unit.TYPE_SABER, 0, 0);
            state.addUnit(unit);

            Map<String, Long> globalVars = new HashMap<>();
            globalVars.put("MyVar", 100L);

            Variable myVar = new Variable("MyVar");
            long result = myVar.eval(state, unit, new HashMap<>(), globalVars);

            assertEquals(100, result);
        }
    }
}