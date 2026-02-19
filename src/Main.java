import ast.*;
import exception.*;
import parser.*;
import engine.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        String configPath = "config.txt";

        // --- Player 1 Strategies (เดินลง) ---
        Map<Integer, String> p1Strategies = new HashMap<>();

        // Type 1: Swordman
        // ความหมาย: "ถ้ามีศัตรูข้างล่าง ให้ยิง, ถ้าไม่มี ให้เดิน"
        // (nearby down จะคืนค่าบวกถ้าเป็นศัตรู ซึ่ง if มองว่าเป็น True)
        p1Strategies.put(Unit.TYPE_SWORDMAN, """
            if (nearby down) then shoot down 10
            else move down
            """);

        // Type 2: Archer
        p1Strategies.put(Unit.TYPE_ARCHER, """
            if (nearby down) then shoot down 8
            else move down
            """);

        // Type 3: Assassin (เดินอย่างเดียว)
        p1Strategies.put(Unit.TYPE_ASSASSIN, """
             move down
            """);

        // Type 4: Mage
        p1Strategies.put(Unit.TYPE_MAGE, """
            if (nearby down) then shoot down 15
            else move down
            """);

        // Type 5: Shielder
        // แก้จาก move 0 (Error) เป็น done (ยืนนิ่งๆ จบเทิร์น)
        p1Strategies.put(Unit.TYPE_SHIELDER, """
            if (nearby down) then shoot down 5
            else done
            """);

        // --- Player 2 Strategies (เดินขึ้น) ---
        Map<Integer, String> p2Strategies = new HashMap<>();

        // Swordman P2
        p2Strategies.put(Unit.TYPE_SWORDMAN, """
            if (nearby up) then shoot up 10
            else move up
            """);

        // เริ่มเกม
        startGameWithMultiStrategies(configPath, p1Strategies, p2Strategies);
    }

    // --- ส่วนอื่นๆ เหมือนเดิม (Game Loop) ---
    public static void startGameWithMultiStrategies(
            String configPath,
            Map<Integer, String> sourceCodeP1,
            Map<Integer, String> sourceCodeP2
    ) {
        try {
            GameConfig.loadConfig(configPath);
            GameState.initialize();

            System.out.println("Compiling P1 Strategies...");
            Map<Integer, Node> strategiesP1 = parseAll(sourceCodeP1);

            System.out.println("Compiling P2 Strategies...");
            Map<Integer, Node> strategiesP2 = parseAll(sourceCodeP2);

            System.out.println("Spawning initial units...");

            // P1: เกิดมุมซ้ายบน (0,0) -> เดินลง
            GameState.setCurrentPlayer(1);
            GameState.spawnUnit(0, 0, GameConfig.init_hp, 0, Unit.TYPE_SWORDMAN);

            // P2: เกิดมุมขวาล่าง (7,7) -> เดินขึ้น
            GameState.setCurrentPlayer(2);
            GameState.spawnUnit(7, 7, GameConfig.init_hp, 0, Unit.TYPE_SWORDMAN);

            Map<String, Long> globalVarsP1 = new HashMap<>();
            Map<String, Long> globalVarsP2 = new HashMap<>();

            long maxTurns = GameState.getMaxTurns();

            for (int turn = 1; turn <= maxTurns; turn++) {
                System.out.println("\n========== TURN " + turn + " ==========");
                executePlayerTurn(1, strategiesP1, globalVarsP1);
                executePlayerTurn(2, strategiesP2, globalVarsP2);
                GameState.advanceGlobalTurn();
            }

            System.out.println("\n=== GAME OVER ===");
            GameState.setCurrentPlayer(1);
            System.out.println("Player 1 Final Budget: " + GameState.getPlayerBudget());
            GameState.setCurrentPlayer(2);
            System.out.println("Player 2 Final Budget: " + GameState.getPlayerBudget());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executePlayerTurn(
            int playerNum,
            Map<Integer, Node> strategies,
            Map<String, Long> globalVars
    ) {
        System.out.println("--- Player " + playerNum + "'s Turn ---");
        GameState.setCurrentPlayer(playerNum);
        GameState.processTurnIncome();

        List<Unit> units = GameState.getPlayerUnitsSortedById(playerNum);
        System.out.println("Commanding " + units.size() + " minions");

        for (Unit u : units) {
            int[] pos = GameState.getUnitPosition(u);
            if (pos != null && u.getOwner() == playerNum) {
                int r = pos[0];
                int c = pos[1];
                int type = u.getType();

                Node strategy = strategies.get(type);
                if (strategy != null) {
                    GameState.setMinionPos(r, c);
                    Map<String, Long> localVars = new HashMap<>();
                    try {
                        strategy.execute(localVars, globalVars);
                    } catch (DoneException e) {
                    } catch (EvalError e) {
                        System.err.println("Error executing Unit " + u.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private static Map<Integer, Node> parseAll(Map<Integer, String> sources) throws Exception {
        Map<Integer, Node> compiled = new HashMap<>();
        for (var entry : sources.entrySet()) {
            Tokenizer tokenizer = new ExprTokenizer(entry.getValue());
            Parser parser = new ExprParser(tokenizer);
            compiled.put(entry.getKey(), parser.parse());
        }
        return compiled;
    }
}