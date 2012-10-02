package it.unisannio.ing.webserver;

import java.io.IOException;
import java.io.PrintWriter;

public final class HttpUtils {
	private HttpUtils() {}
	
	public static void send404(HttpResponse res, String path) throws IOException {
		res.setStatus("404 Not Found");
		PrintWriter w = res.getWriter();
		
		w.println("<!doctype html>");
		w.println("<html>");
		w.println("<head><title>404 Not Found</title></head>");
		w.println("<body>");
		w.println("<h1>404 Not Found</h1>");
		w.println("<p>Server couldn't locate a resource for the path <code>" + path + "</code></p>");
		w.println("</body>");
		w.println("</html>");
	}
	
	public static void send400(HttpResponse res, String message) throws IOException {
		res.setStatus("400 Bad Request");
		PrintWriter w = res.getWriter();
		
		w.println("<!doctype html>");
		w.println("<html>");
		w.println("<head><title>400 Bad Request</title></head>");
		w.println("<body>");
		w.println("<h1>400 Bad Request</h1>");
		w.println("<p>Server couldn't fulfill your request due to bad syntax (" + message + ").</p>");
		w.println("</body>");
		w.println("</html>");
		
	}
	
	public static void send500(HttpResponse res, Exception e) throws IOException {
		res.setStatus("500 Internal Server Error");
		PrintWriter w = res.getWriter();
		
		w.println("<!doctype html>");
		w.println("<html>");
		w.println("<head><title>500 Internal Server Error</title></head>");
		w.println("<body>");
		w.println("<h1>500 Internal Server Error</h1>");
		w.println("<pre>");
		e.printStackTrace(w);
		w.println("</pre>");
		w.println("</body>");
		w.println("</html>");
	}

	public static void send403(HttpResponse res) throws IOException {
		res.setStatus("403 Forbidden");
		PrintWriter w = res.getWriter();
		
		w.println("<!doctype html>");
		w.println("<html>");
		w.println("<head><title>403 Forbidden</title></head>");
		w.println("<body>");
		w.println("<h1>403 Forbidden</h1>");
		w.println("</body>");
		w.println("</html>");
	}
}
