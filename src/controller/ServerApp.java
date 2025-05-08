package controller;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

import java.util.concurrent.Executor;
import java.net.InetSocketAddress;

public class ServerApp {
	  public static void main(String[] args) throws IOException {

		  //create  a server 
		  HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
		  
		  //create context or mapping url with particular httpHandler object
		  server.createContext( "/test", new MyHandle());
		  
		  //start the server
		  server.setExecutor(null);//CREATE A DEFAULT THREADPOOL IN THAT SERVER 
		  server.start();
		  
		  System.out.println("Server is running on the port 8000"); 
		  
}
	  
	  //user definde handler 
	  static class MyHandle implements HttpHandler
	  {
		  @Override
		  public void handle(HttpExchange exchange) throws IOException 
	        {
			  String Response = "Hello this is a simple http server response in against fo that http mapping request";
			  exchange.sendResponseHeaders(200, Response.length());
			OutputStream os =   exchange.getResponseBody();
			os.write(Response.getBytes());
			os.close();
	  }
	  
	  
}
}