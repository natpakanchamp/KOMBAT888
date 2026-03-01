package engine;

public class Unit {
    // 1. ประกาศค่าคงที่อาชีพ (เพื่อให้ Main เรียกใช้ได้)
    public static final int TYPE_SWORDMAN = 1;
    public static final int TYPE_ARCHER = 2;
    public static final int TYPE_ASSASSIN = 3;
    public static final int TYPE_MAGE = 4;
    public static final int TYPE_SHIELDER = 5;

    private long hp;
    private long defense;
    private int owner;
    private int id;
    private int type;
    private long role; // เก็บ role (เช่น ตัวบุก/ตัวรับ)

    static int nextUnitId = 1;

    // Constructor รับ type เข้ามาด้วย
    public Unit(long hp, long defense, int owner, int type) {
        this.owner = (owner % 2 == 0) ? 2 : 1;
        this.type = type;
        this.id = nextUnitId++;
        this.role = 0;

        // 2. ปรับค่าพลังตามอาชีพ (Base Stats + Class Bonus)
        this.hp = hp;
        this.defense = defense;

        switch (type) {
            case TYPE_SWORDMAN -> {
                // สมดุล (ค่าเดิม)
            }
            case TYPE_ARCHER -> {
                this.hp = Math.max(1, this.hp - 20);     // เลือดน้อย
            }
            case TYPE_ASSASSIN -> {
                this.hp = Math.max(1, this.hp - 30);     // เลือดน้อยมาก
                this.defense = Math.max(0, this.defense - 1); // เกราะบาง
            }
            case TYPE_MAGE -> {
                this.hp = Math.max(1, this.hp - 40);     // เลือดน้อยที่สุด
            }
            case TYPE_SHIELDER -> {
                this.hp += 50;     // เลือดเยอะ
                this.defense += 5; // เกราะหนา
            }
        }
    }

    // --- Getters / Setters ---
    public long getHP() { return hp; }
    public long getDefense() { return defense; }
    public int getOwner() { return owner; }
    public int getType() { return type; }

    public void takeDamage(long d) { hp -= d; }
    public boolean isDead() { return hp <= 0; }

    public int getId() { return id; }
    public static void resetId() { nextUnitId = 1; }

    public long getRole() { return role; }
    public void setRole(long role) { this.role = role; }
}