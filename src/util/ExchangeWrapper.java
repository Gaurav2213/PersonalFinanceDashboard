// util/ExchangeWrapper.java
package util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.Headers;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;


public class ExchangeWrapper extends HttpExchange {
  private final HttpExchange d;
  public ExchangeWrapper(HttpExchange delegate){ this.d = delegate; }

  @Override public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
    ResponseStatus.set(rCode);        // <-- record real status
    d.sendResponseHeaders(rCode, responseLength);
  }

  // Delegate everything else 1:1
  @Override public Headers getRequestHeaders(){ return d.getRequestHeaders(); }
  @Override public Headers getResponseHeaders(){ return d.getResponseHeaders(); }
  @Override public URI getRequestURI(){ return d.getRequestURI(); }
  @Override public String getRequestMethod(){ return d.getRequestMethod(); }
  @Override public HttpContext getHttpContext(){ return d.getHttpContext(); }
  @Override public void close(){ d.close(); }
  @Override public InputStream getRequestBody(){ return d.getRequestBody(); }
  @Override public OutputStream getResponseBody(){ return d.getResponseBody(); }
  @Override public InetSocketAddress getRemoteAddress(){ return d.getRemoteAddress(); }
  @Override public int getResponseCode(){ return d.getResponseCode(); }
  @Override public InetSocketAddress getLocalAddress(){ return d.getLocalAddress(); }
  @Override public String getProtocol(){ return d.getProtocol(); }
  @Override public Object getAttribute(String name){ return d.getAttribute(name); }
  @Override public void setAttribute(String name, Object value){ d.setAttribute(name, value); }
  @Override public void setStreams(InputStream i, OutputStream o){ d.setStreams(i, o); }
  @Override public HttpPrincipal getPrincipal(){ return d.getPrincipal(); }
}
