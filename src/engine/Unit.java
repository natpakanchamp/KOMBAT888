package engine;

public class Unit {
    private long hp;
    private long defense;
    private boolean isAlly;
    private int id;

    // แก้ไข: เปลี่ยน int hp, int defense เป็น long
    public Unit(long hp, long defense, boolean isAlly, int id) {
        this.hp = hp;
        this.defense = defense;
        this.isAlly = isAlly;
        this.id = id;
    }

    public long getHP() { return hp; }
    public long getDefense() { return defense; }
    public boolean isAlly() { return isAlly; }
    public void takeDamage(long d) { hp -= d; }
    public boolean isDead() { return hp <= 0; }
}