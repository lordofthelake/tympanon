package it.michelepiccirillo.tympanon;

import it.michelepiccirillo.tympanon.HttpResponse.Status;

public class AbstractRoute implements Route {
	
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

	public void service(HttpRequest req, HttpResponse res) throws Exception {
		switch(req.getMethod()) {
		case OPTIONS:
			options(req, res);
			break;
		case GET:
			get(req, res);
			break;
		case HEAD:
			head(req,res);
			break;
		case POST:
			post(req, res);
			break;
		case PUT:
			put(req,res);
			break;
		case DELETE:
			delete(req,res);
			break;
		case TRACE:
			trace(req,res);
			break;
		case CONNECT:
			connect(req,res);
			break;
			
		}	
	}
}
