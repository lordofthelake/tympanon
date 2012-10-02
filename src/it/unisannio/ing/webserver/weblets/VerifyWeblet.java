package it.unisannio.ing.webserver.weblets;

import java.io.IOException;
import java.io.PrintWriter;

import it.unisannio.ing.webserver.Config;
import it.unisannio.ing.webserver.HttpRequest;
import it.unisannio.ing.webserver.HttpResponse;
import it.unisannio.ing.webserver.Weblet;

public class VerifyWeblet implements Weblet {

	@Override
	public void initialize(Config config) {
		// TODO Auto-generated method stub

	}

	@Override
	public void service(HttpRequest req, HttpResponse res) throws IOException {
		PrintWriter w = res.getWriter();
		
		String pass = req.getParameter("pass1");
		String name = req.getParameter("name");
		String email = req.getParameter("email");
		
		w.println("<!doctype html>");
		w.println("<html><head><title>Verifica</title></head><body>");
		if(pass.equals(req.getParameter("pass2"))) {
			w.println("<ul>");
			w.println("<li><strong>Name</strong>: " + name + "</li>");
			w.println("<li><strong>Email</strong>: " + email + "</li>");
			w.println("<li><strong>Password</strong>: " + pass + "</li>");
			w.println("</ul>");
		} else {
			w.println("Le password non coincidono");
		}
		
		w.println("</body></html>");
		
	}

}
