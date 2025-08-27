package util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;

public class Guarded {

  @FunctionalInterface
  public interface AuthedHandler {
    void handle(HttpExchange exchange, Claims claims) throws java.io.IOException;
  }

  /** Wrap a handler with CORS + JWT guard + optional JSON Content-Type enforcement + safe error handling */
  public static HttpHandler protect(AuthedHandler logic) {
	  
    return exchange -> {
      // Always add CORS headers
      Utils.addCorsHeaders(exchange.getResponseHeaders());

      // Allow CORS preflight without authentication and without a body
      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
        Utils.sendNoContent(exchange);
        return;
      }

      // Verify token (signature/exp/blacklist) via AuthGuard
      Claims claims = AuthGuard.verify(exchange);
      if (claims == null) {
        AuthGuard.unauthorized(exchange);
        return;
      }

      try {
        // ---- Request-side JSON Content-Type enforcement (only for methods that normally carry a body) ----
        String method = exchange.getRequestMethod();
        boolean expectsBody =
            "POST".equalsIgnoreCase(method)
         || "PUT".equalsIgnoreCase(method)
         || "PATCH".equalsIgnoreCase(method)
         // include DELETE below only if your API accepts JSON bodies on DELETE:
         // || "DELETE".equalsIgnoreCase(method)
         ;

        if (expectsBody) {
          String ct = exchange.getRequestHeaders().getFirst("Content-Type");
          if (ct == null || !ct.toLowerCase().contains("application/json")) {
            Utils.sendResponse(exchange, 400,
              "{\"success\":false,\"message\":\"Content-Type must be application/json\",\"data\":null}");
            return;
          }
        }
        
    
       

        // ---- Make auth context available to downstream handlers ----
        String sub = claims.getSubject(); // userId as string
        if (sub != null) {
          try {
            exchange.setAttribute("authUserId", Integer.parseInt(sub));
          } catch (NumberFormatException ignored) {
            exchange.setAttribute("authUserIdStr", sub);
          }
        }
        exchange.setAttribute("authEmail", claims.get("email", String.class));
        exchange.setAttribute("authJti",   claims.getId());
        // Optional:
        // exchange.setAttribute("authRole", claims.get("role", String.class));

        // ---- Run the protected business logic ----
        logic.handle(exchange, claims);

      } catch (Exception e) {
        e.printStackTrace();
        Utils.sendResponse(exchange, 500,
          "{\"success\":false,\"message\":\"Internal server error\",\"data\":null}");
      }
    };
  }
  
  
  public static HttpHandler protectJsonRequireBody(AuthedHandler logic) {
	  return exchange -> {
	    Utils.addCorsHeaders(exchange.getResponseHeaders());

	    if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
	      Utils.sendNoContent(exchange);
	      return;
	    }

	    Claims claims = AuthGuard.verify(exchange);
	    if (claims == null) { AuthGuard.unauthorized(exchange); return; }

	    try {
	      // Require JSON body for POST/PUT/PATCH (DELETE optional)
	      String method = exchange.getRequestMethod();
	      boolean expectsBody =
	             "POST".equalsIgnoreCase(method)
	          || "PUT".equalsIgnoreCase(method)
	          || "PATCH".equalsIgnoreCase(method);
	          // || "DELETE".equalsIgnoreCase(method); // if your API uses JSON on DELETE

	      if (expectsBody) {
	        String ct = exchange.getRequestHeaders().getFirst("Content-Type");
	        if (ct == null || !ct.toLowerCase().contains("application/json")) {
	          Utils.sendResponse(exchange, 400,
	            "{\"success\":false,\"message\":\"Content-Type must be application/json\",\"data\":null}");
	          return;
	        }
	        String cl = exchange.getRequestHeaders().getFirst("Content-Length");
	        if (cl != null) {
	          try {
	            if (Long.parseLong(cl.trim()) == 0L) {
	              Utils.sendResponse(exchange, 400,
	                "{\"success\":false,\"message\":\"Empty request body\",\"data\":null}");
	              return;
	            }
	          } catch (NumberFormatException ignored) {}
	        }
	      }

	      // expose auth context
	      String sub = claims.getSubject();
	      if (sub != null) {
	        try { exchange.setAttribute("authUserId", Integer.parseInt(sub)); }
	        catch (NumberFormatException ignored) { exchange.setAttribute("authUserIdStr", sub); }
	      }
	      exchange.setAttribute("authEmail", claims.get("email", String.class));
	      exchange.setAttribute("authJti",   claims.getId());

	      logic.handle(exchange, claims);

	    } catch (Exception e) {
	      e.printStackTrace();
	      Utils.sendResponse(exchange, 500,
	        "{\"success\":false,\"message\":\"Internal server error\",\"data\":null}");
	    }
	  };
	}

}
