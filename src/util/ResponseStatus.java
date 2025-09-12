// util/ResponseStatus.java
package util;

public class ResponseStatus {
  private static final ThreadLocal<Integer> STATUS = new ThreadLocal<>();
  public static void set(int s){ STATUS.set(s); }
  public static int get(){ Integer s = STATUS.get(); return s == null ? 200 : s; }
  public static void clear(){ STATUS.remove(); }
}
