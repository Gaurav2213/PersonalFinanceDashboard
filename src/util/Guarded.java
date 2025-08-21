package util;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
public class Guarded {

	  @FunctionalInterface
	  public interface AuthedHandler {
	    void handle(HttpExchange exchange, Claims claims) throws java.io.IOException;
	  }

	  public static HttpHandler protect(AuthedHandler logic) {
	    return exchange -> {
	      // Allow CORS preflight without authentication
	      if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
	        Utils.addCorsHeaders(exchange.getResponseHeaders());
	        Utils.sendResponse(exchange, 204, "");
	        return;
	      }

	      var claims = AuthGuard.verify(exchange);
	      if (claims == null) {
	        AuthGuard.unauthorized(exchange);
	        return;
	      }
	      logic.handle(exchange, claims);
	    };
	  }
	}
