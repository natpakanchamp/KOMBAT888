package engine;

public class Unit {
    private long hp;
    private long defense;
    private int owner;
    private int id;

    // แก้ไข: เปลี่ยน int hp, int defense เป็น long
    public Unit(long hp, long defense, int owner, int id) {
        this.hp = hp;
        this.defense = defense;
        this.owner = (owner%2 == 0) ? 2 : 1;
        this.id = id;
    }

    public long getHP() { return hp; }
    public long getDefense() { return defense; }
    public int getOwner() { return owner; }
    public boolean isAlly(Unit other) { return owner == other.owner; }
    public void takeDamage(long d) { hp -= d; }
    public boolean isDead() { return hp <= 0; }
}