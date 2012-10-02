package it.unisannio.ing.webserver.weblets;

import java.io.IOException;

import it.unisannio.ing.webserver.Config;
import it.unisannio.ing.webserver.HttpRequest;
import it.unisannio.ing.webserver.HttpResponse;
import it.unisannio.ing.webserver.Weblet;

public class HelloWorldWeblet implements Weblet {

	@Override
	public void initialize(Config config) { }

	@Override
	public void service(HttpRequest req, HttpResponse res) throws IOException {
		res.getWriter().println("Hello World!");
	}

}
