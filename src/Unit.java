// คลาสเสริมสำหรับเก็บข้อมูล Unit ในสนาม
class Unit {
    private int hp;
    private int defense;
    public int getHP() { return hp; }
    public int getDefense() { return defense; }
    public void takeDamage(int d) { hp -= d; }
}