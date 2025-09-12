package util;

import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.function.Supplier;

import static net.logstash.logback.argument.StructuredArguments.kv;
 
public class RequestLog {
  private static final Logger log = LoggerFactory.getLogger(RequestLog.class);

  /** Run handler logic with request MDC + latency timing. Return status code for final log. */
  public static int withRequestLogging(HttpExchange ex, Supplier<Integer> logic) {
    String traceId = UUID.randomUUID().toString();
    MDC.put("traceId", traceId);
    MDC.put("path", ex.getRequestURI().getPath());
    MDC.put("method", ex.getRequestMethod());
    MDC.put("ip", clientIp(ex));
    MDC.put("ua", ex.getRequestHeaders().getFirst("User-Agent"));

    long t0 = System.nanoTime();
    int status = 500;
    try {
      // Optional: “request_start” breadcrumb
      log.info("request_start");

      status = logic.get();               // run your handler; you can call setUser/setAction inside
      return status;
    } catch (Exception e) {
      log.error("request_fail message={}", e.getMessage(), e);
      throw e;
    } finally {
      long ms = (System.nanoTime() - t0) / 1_000_000;
      MDC.put("status", String.valueOf(status));     // adds status to final line
      MDC.put("latencyMs", String.valueOf(ms));
      log.info("request_done");                      // final summary line with MDC fields
      MDC.clear();
    }
  }

  /** Call this AFTER you decode JWT so all subsequent logs carry the user. */
  public static void setUser(int userId) {
    MDC.put("userId", String.valueOf(userId));
  }

  /** Optional: call when you know the business action (e.g., ADD_TRANSACTION). */
  public static void setAction(String action) {
    MDC.put("action", action);
  }

  private static String clientIp(HttpExchange ex) {
    String xff = ex.getRequestHeaders().getFirst("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
    InetSocketAddress addr = ex.getRemoteAddress();
    return (addr != null && addr.getAddress() != null) ? addr.getAddress().getHostAddress() : "unknown";
  }
}
