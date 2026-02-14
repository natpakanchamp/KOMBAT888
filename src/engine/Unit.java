package engine;

public class Unit {
    private long hp;
    private long defense;
    private boolean isAlly; // True = ของเรา, False = ของศัตรู
    private int id; // เอาไว้เช็คลำดับการ spawn

    public Unit(int hp, int defense, boolean isAlly, int id) {
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