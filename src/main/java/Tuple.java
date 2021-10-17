public class Tuple<T> {
    public T x;
    public T y;

    public Tuple(T x, T y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ')';
    }
}
