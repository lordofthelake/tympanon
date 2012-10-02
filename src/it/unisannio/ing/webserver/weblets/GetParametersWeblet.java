package it.unisannio.ing.webserver.weblets;

import java.io.IOException;
import java.io.PrintWriter;

import it.unisannio.ing.webserver.Config;
import it.unisannio.ing.webserver.HttpRequest;
import it.unisannio.ing.webserver.HttpResponse;
import it.unisannio.ing.webserver.Weblet;

public class GetParametersWeblet implements Weblet {

	@Override
	public void initialize(Config config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void service(HttpRequest req, HttpResponse res) throws IOException {
		PrintWriter w = res.getWriter();
		w.println("<!doctype html>");
		w.println("<html>");
		w.println("<head><title>GET</title></head>");
		w.println("<body>");
		w.println("<dl>");
		for(String p : req.getParameters()) {
			w.println("<dt>" + p + "</dt><dd>" + req.getParameter(p));
		}
		w.println("</dl>");
		w.println("</body>");
		w.println("</html>");
	}

}
