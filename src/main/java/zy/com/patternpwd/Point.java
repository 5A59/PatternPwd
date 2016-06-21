package zy.com.patternpwd;

/**
 * Created by zy on 16-6-21.
 */
public class Point {
    int x;
    int y;
    int num;

    public Point() {
        this(0, 0);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.num = -1;
    }

    public Point(int x, int y, int num) {
        this.x = x;
        this.y = y;
        this.num = num;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
