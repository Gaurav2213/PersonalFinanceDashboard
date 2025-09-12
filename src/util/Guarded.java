// util/Guarded.java
package util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.UUID;

public class Guarded {

  private static final Logger log = LoggerFactory.getLogger(Guarded.class);

  @FunctionalInterface
  public interface AuthedHandler {
    void handle(HttpExchange exchange, Claims claims) throws java.io.IOException;
  }
  
  @FunctionalInterface
  public interface OpenHandler {
    void handle(HttpExchange exchange) throws java.io.IOException;
  }


  /** CORS + JWT + MDC + latency + action-from-path (handlers unchanged) */
  public static HttpHandler protect(AuthedHandler logic) {
    return raw -> {
      // Always add CORS
      Utils.addCorsHeaders(raw.getResponseHeaders());

      // ---- Handle OPTIONS early but LOG it (new) ----
      if ("OPTIONS".equalsIgnoreCase(raw.getRequestMethod())) {
        // Minimal MDC so preflights are visible
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("path", raw.getRequestURI().getPath());
        MDC.put("method", "OPTIONS");
        MDC.put("action", "CORS_PREFLIGHT");
        MDC.put("ip", clientIp(raw));
        MDC.put("ua", safeUA(raw));
        long t0 = System.nanoTime();
        try {
          ResponseStatus.set(204); // ensure status lands in final log
          Utils.sendNoContent(raw);
          long ms = (System.nanoTime() - t0) / 1_000_000;
          MDC.put("status", String.valueOf(ResponseStatus.get()));
          MDC.put("latencyMs", String.valueOf(ms));
          log.info("{\"event\":\"request_completed\"}");
        } finally {
          ResponseStatus.clear();
          MDC.clear();
        }
        return;
      }

      // Wrap to capture status codes transparently
      HttpExchange ex = new ExchangeWrapper(raw);

      // ---- MDC (request context) ----
      MDC.put("traceId", UUID.randomUUID().toString());
      MDC.put("path", ex.getRequestURI().getPath());
      MDC.put("method", ex.getRequestMethod());
      MDC.put("ip", clientIp(ex));
      MDC.put("ua", safeUA(ex));
      MDC.put("action", actionFromPath(ex.getRequestURI().getPath()));

      long t0 = System.nanoTime();
      try {
        // ---- Auth ----
        Claims claims = AuthGuard.verify(ex);
        if (claims == null) {
          MDC.put("action", "AUTH_DENY");
          ResponseStatus.set(401);             // (new) capture 401 in final log
          AuthGuard.unauthorized(ex);
          return;
        }

        // User context from token (great for Splunk filters)
        String sub = claims.getSubject(); // user id (string)
        if (sub != null) MDC.put("userId", sub);
        String email = claims.get("email", String.class);
        if (email != null) MDC.put("email", email.toLowerCase(Locale.ROOT));
        String jti = claims.getId(); if (jti != null) MDC.put("jti", jti);

        // expose to handlers exactly like your current code
        if (sub != null) {
          try { ex.setAttribute("authUserId", Integer.parseInt(sub)); }
          catch (NumberFormatException ignored) { ex.setAttribute("authUserIdStr", sub); }
        }
        ex.setAttribute("authEmail", email);
        ex.setAttribute("authJti",   jti);

        log.info("{\"event\":\"request_received\"}");

        // ---- run original business logic (no changes) ----
        logic.handle(ex, claims);

      } catch (Exception e) {
        // Mark error path; send 500 just like you do
        log.error("{\"event\":\"request_error\",\"message\":\"{}\"}", e.toString(), e);
        try {
          ResponseStatus.set(500);
          Utils.sendResponse(ex, 500,
            "{\"success\":false,\"message\":\"Internal server error\",\"data\":null}");
        } catch (Exception ignored) {}
      } finally {
        long ms = (System.nanoTime() - t0) / 1_000_000;
        MDC.put("status", String.valueOf(ResponseStatus.get()));
        MDC.put("latencyMs", String.valueOf(ms));
        log.info("{\"event\":\"request_completed\"}");
        ResponseStatus.clear();
        MDC.clear();
      }
    };
  }

  
  /** CORS + MDC + latency + action-from-path (NO auth check) */
  public static HttpHandler open(OpenHandler logic) {
    return raw -> {
      // Always add CORS
      Utils.addCorsHeaders(raw.getResponseHeaders());

      // ---- Handle OPTIONS early but LOG it like protect() ----
      if ("OPTIONS".equalsIgnoreCase(raw.getRequestMethod())) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("path", raw.getRequestURI().getPath());
        MDC.put("method", "OPTIONS");
        MDC.put("action", "CORS_PREFLIGHT");
        MDC.put("ip", clientIp(raw));
        MDC.put("ua", safeUA(raw));
        long t0 = System.nanoTime();
        try {
          ResponseStatus.set(204);
          Utils.sendNoContent(raw);
          long ms = (System.nanoTime() - t0) / 1_000_000;
          MDC.put("status", String.valueOf(ResponseStatus.get()));
          MDC.put("latencyMs", String.valueOf(ms));
          log.info("{\"event\":\"request_completed\"}");
        } finally {
          ResponseStatus.clear();
          MDC.clear();
        }
        return;
      }

      // Wrap to capture status transparently
      HttpExchange ex = new ExchangeWrapper(raw);

      // ---- MDC (request context) ----
      MDC.put("traceId", UUID.randomUUID().toString());
      MDC.put("path", ex.getRequestURI().getPath());
      MDC.put("method", ex.getRequestMethod());
      MDC.put("ip", clientIp(ex));
      MDC.put("ua", safeUA(ex));
      MDC.put("action", actionFromPath(ex.getRequestURI().getPath()));

      long t0 = System.nanoTime();
      try {
        log.info("{\"event\":\"request_received\"}");
        // run original business logic (no auth needed)
        logic.handle(ex);
      } catch (Exception e) {
        log.error("{\"event\":\"request_error\",\"message\":\"{}\"}", e.toString(), e);
        try {
          ResponseStatus.set(500);
          Utils.sendResponse(ex, 500,
            "{\"success\":false,\"message\":\"Internal server error\",\"data\":null}");
        } catch (Exception ignored) {}
      } finally {
        long ms = (System.nanoTime() - t0) / 1_000_000;
        MDC.put("status", String.valueOf(ResponseStatus.get()));
        MDC.put("latencyMs", String.valueOf(ms));
        log.info("{\"event\":\"request_completed\"}");
        ResponseStatus.clear();
        MDC.clear();
      }
    };
  }

  // --- URL→Action mapping (unchanged) ---
  private static String actionFromPath(String p) {
    // PUBLIC
    if (p.equals("/test"))                 return "TEST";
    if (p.equals("/register"))             return "REGISTER";
    if (p.equals("/login"))                return "LOGIN";
    if (p.equals("/verify-email"))         return "VERIFY_EMAIL";
    if (p.equals("/auth/resend-verification")) return "RESEND_VERIFICATION";
    if (p.equals("/auth/refresh"))         return "REFRESH_TOKEN";
    if (p.equals("/auth/forgot-password")) return "FORGOT_PASSWORD";
    if (p.equals("/auth/reset-password"))  return "RESET_PASSWORD";
    // PROTECTED: users
    if (p.equals("/logout"))               return "LOGOUT";
    // PROTECTED: single transaction ops
    if (p.equals("/transaction/add"))      return "ADD_TRANSACTION";
    if (p.equals("/transaction/all"))      return "GET_ALL_TRANSACTIONS";
    if (p.equals("/transaction/category")) return "GET_TRANSACTIONS_BY_CATEGORY";
    if (p.equals("/transaction/update"))   return "UPDATE_TRANSACTION";
    if (p.equals("/transaction/delete"))   return "DELETE_TRANSACTION";
    // PROTECTED: batch transactions
    if (p.equals("/transactions/batch-add"))    return "BATCH_ADD_TRANSACTIONS";
    if (p.equals("/transactions/batch-update")) return "BATCH_UPDATE_TRANSACTIONS";
    if (p.equals("/transactions/batch-delete")) return "BATCH_DELETE_TRANSACTIONS";
    // PROTECTED: analytics
    if (p.equals("/analytics/top-categories"))      return "AN_TOP_CATEGORIES";
    if (p.equals("/analytics/summary"))             return "AN_SUMMARY";
    if (p.equals("/analytics/budget-utilization"))  return "AN_BUDGET_UTILIZATION";
    if (p.equals("/analytics/prefix-sum"))          return "AN_PREFIX_SUM";
    if (p.equals("/analytics/max-min-days"))        return "AN_MAX_MIN_DAYS";
    if (p.equals("/analytics/category-trend"))      return "AN_CATEGORY_TREND";
    if (p.equals("/analytics/recurring"))           return "AN_RECURRING";
    if (p.equals("/analytics/overspend-warnings"))  return "AN_OVERSPEND_WARNINGS";
    if (p.equals("/analytics/filter"))              return "AN_FILTER";
    if (p.equals("/analytics/spending-distribution")) return "AN_SPENDING_DISTRIBUTION";
    if (p.equals("/analytics/categories"))          return "AN_VALID_CATEGORIES";
    // PROTECTED: budgets
    if (p.equals("/budget/add"))            return "ADD_BUDGET";
    if (p.equals("/budget/update"))         return "UPDATE_BUDGET";
    if (p.equals("/budget/delete"))         return "DELETE_BUDGET";
    if (p.equals("/budget/user"))           return "GET_BUDGETS_BY_USER";
    if (p.equals("/budget/all"))            return "GET_BUDGETS_ALL";
    if (p.equals("/budget/batch-add"))      return "BATCH_ADD_BUDGETS";
    if (p.equals("/budget/batch-update"))   return "BATCH_UPDATE_BUDGETS";
    if (p.equals("/budget/batch-delete"))   return "BATCH_DELETE_BUDGETS";

    return p; // fallback keeps some clue in Splunk
  }

  private static String clientIp(HttpExchange ex) {
    String xff = ex.getRequestHeaders().getFirst("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
    InetSocketAddress a = ex.getRemoteAddress();
    return (a != null && a.getAddress() != null) ? a.getAddress().getHostAddress() : "unknown";
  }

  // (new) null-safe UA, trims very long strings to avoid bloating events
  private static String safeUA(HttpExchange ex) {
    String ua = ex.getRequestHeaders().getFirst("User-Agent");
    if (ua == null) return "unknown";
    return ua.length() > 256 ? ua.substring(0, 256) : ua;
  }
}
