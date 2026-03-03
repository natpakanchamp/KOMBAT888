package com.example.backend.model.engine;

import lombok.Getter;
import lombok.Setter;

@Getter // สร้าง Getter (เช่น getDefense, getOwner, getId, getType, getRole) ให้อัตโนมัติ
public class Unit {
    // 1. ประกาศค่าคงที่อาชีพ
    public static final int TYPE_SABER = 1;
    public static final int TYPE_ARCHER = 2;
    public static final int TYPE_LANCER = 3;
    public static final int TYPE_CASTER = 4;
    public static final int TYPE_BERSERKER = 5;

    private long hp;
    private long defense;
    private final int owner;
    private final int id;
    private final int type;

    @Setter // สร้าง setRole(long role) ให้อัตโนมัติแค่ตัวแปรนี้
    private long role;

    static int nextUnitId = 1;

    // Constructor รับ type เข้ามาด้วย (ต้องเขียนเองเพราะมี Logic ภายใน)
    public Unit(long hp, long defense, int owner, int type) {
        this.owner = (owner % 2 == 0) ? 2 : 1;
        this.type = type;
        this.id = nextUnitId++;
        this.role = 0;

        // 2. ปรับค่าพลังตามอาชีพ (Base Stats + Class Bonus)
        this.hp = hp;
        this.defense = defense;

        switch (type) {
            case TYPE_SABER -> {
                // สมดุล (ค่าเดิม)
            }
            case TYPE_ARCHER -> {
                this.hp = Math.max(1, this.hp - 20);     // เลือดน้อย
            }
            case TYPE_LANCER -> {
                this.hp = Math.max(1, this.hp - 30);     // เลือดน้อยมาก
                this.defense = Math.max(0, this.defense - 1); // เกราะบาง
            }
            case TYPE_CASTER -> {
                this.hp = Math.max(1, this.hp - 40);     // เลือดน้อยที่สุด
            }
            case TYPE_BERSERKER -> {
                this.hp += 50;     // เลือดเยอะ
                this.defense += 5; // เกราะหนา
            }
        }
    }

    // --- Custom Methods ---

    // หมายเหตุ: ลอมบอกจะสร้าง getHp() ให้ แต่เพื่อไม่ให้โค้ดเก่าพัง เราจึงเขียน getHP() ครอบไว้ด้วย
    public long getHP() { return hp; }

    public void takeDamage(long d) { hp -= d; }

    public boolean isDead() { return hp <= 0; }

    public static void resetId() { nextUnitId = 1; }
}