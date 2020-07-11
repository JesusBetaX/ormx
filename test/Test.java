
import java.util.Collection;

/**
 *
 * @author jesus
 */
public class Test {

  public static void main(String... args) {
    Var var = Var.of(null, "None");
    System.out.println(var.isNull());
    System.out.println(var.isEmpty());
    System.out.println(var.toString());
  }
  
  
  public static class Var<T> {
    private T value;
    private T defaultValue;
    public Var(T value) {
      this.value = value;
    }
    public static <V> Var<V> of(V value) {
      return new Var<V>(value);
    }
    public static <V> Var<V> of(V value, V defaultVal) {
      return of(value).or(defaultVal);
    }
    public Var<T> or(T defaultVal) {
      defaultValue = defaultVal;
      return this;
    }
    public boolean isString() {
      return value instanceof String;
    }
    public boolean isCollection() {
      return value instanceof Collection;
    }
    public boolean isNull() {
      return value == null;
    }
    public boolean isEmpty() {
      if (isString())
        return ((String)value).isEmpty();
      if (isCollection())
        return ((Collection)value).isEmpty();
      if (value instanceof Object[])
        return ((Object[])value).length == 0;
      
      return false;
    }
    public T valueOr(T defaultVal) {
      return isEmpty() ? defaultVal : value;
    }
    public T value() {
      return valueOr(defaultValue);
    }
    @Override public String toString() {
      if (isNull())
        return defaultValue == null ? null : defaultValue.toString();
      
      return value.toString();
    }
    public int toInt() {
      return Integer.parseInt(toString());
    }
    public int toShort() {
      return Short.parseShort(toString());
    }
  }
}
