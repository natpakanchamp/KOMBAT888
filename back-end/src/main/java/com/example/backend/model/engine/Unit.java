package com.example.backend.model.engine;

import com.example.backend.model.ast.Statement;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Unit {
    public static final int TYPE_SABER = 1;
    public static final int TYPE_ARCHER = 2;
    public static final int TYPE_LANCER = 3;
    public static final int TYPE_CASTER = 4;
    public static final int TYPE_BERSERKER = 5;

    private long hp;
    private final long maxHp; // เป็น final ไปเลยเพราะล็อกไว้ที่ 100
    private long defense;
    private final int owner;
    private final int id;
    private final int type;

    @Setter
    private long role;

    @Setter
    private int row;
    @Setter
    private int col;

    @Getter
    @Setter
    private Statement strategy;

    static int nextUnitId = 1;

    // 💡 ปรับปรุง: ถอด parameter 'hp' ออกจาก Constructor เพราะเราจะล็อกเป็น 100 เสมอ
    public Unit(long defense, int owner, int type) {
        this(defense, owner, type, -1, -1);
    }

    public Unit(long defense, int owner, int type, int row, int col) {
        this.owner = (owner % 2 == 0) ? 2 : 1;
        this.type = type;
        this.id = nextUnitId++;
        this.role = 0;
        this.row = row;
        this.col = col;

        // 🌟 ล็อกเลือดเริ่มต้นและเลือดสูงสุดไว้ที่ 100 เท่ากันทุกอาชีพ
        this.maxHp = 100;
        this.hp = 100;

        this.defense = defense;

        // 🌟 ปรับปรุง: switch-case จะเหลือแค่การจัดการค่าเกราะ (defense) หรือค่าอื่นๆ แทน
        switch (type) {
            case TYPE_SABER -> {
                // สมดุล
            }
            case TYPE_ARCHER -> {
                // อาชีพยิงไกล อาจจะไม่มีโบนัสเกราะ
            }
            case TYPE_LANCER -> {
                this.defense = Math.max(0, this.defense - 1); // เกราะบางลงนิดหน่อย
            }
            case TYPE_CASTER -> {
                this.defense = Math.max(0, this.defense - 2); // นักเวทย์เกราะบางสุด
            }
            case TYPE_BERSERKER -> {
                this.defense += 5; // เกราะหนามาก (ชดเชยที่ไม่ได้บวกเลือดแล้ว)
            }
        }
    }

    public long getHP() { return hp; }

    // โดนตี: เลือดลดแต่ไม่ต่ำกว่า 0
    public void takeDamage(long d) {
        this.hp = Math.max(0, this.hp - d);
    }

    // 🌟 เพิ่มฟังก์ชันฮีล: เผื่อมีคำสั่งบัฟเลือด เลือดจะได้ไม่ทะลุ 100
    public void heal(long amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
    }

    public boolean isDead() { return hp <= 0; }
    public boolean isAlive() { return hp > 0; }

    public static void resetId() { nextUnitId = 1; }
}