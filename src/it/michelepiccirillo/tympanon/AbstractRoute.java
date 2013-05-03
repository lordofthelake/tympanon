package it.michelepiccirillo.tympanon;

import it.michelepiccirillo.tympanon.HttpResponse.Status;

import java.io.IOException;

public class AbstractRoute implements Route {

	@Override
	public void service(HttpRequest req, HttpResponse res) throws IOException {
		// TODO Auto-generated method stub

	}
	
	protected void options(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void get(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void head(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void post(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void put(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void delete(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void trace(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
	
	protected void connect(HttpRequest req, HttpResponse res) throws Exception {
		res.setStatus(Status.METHOD_NOT_ALLOWED);
	}
}
