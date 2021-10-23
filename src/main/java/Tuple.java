import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Tuple<T> {
    public T x;
    public T y;
}
