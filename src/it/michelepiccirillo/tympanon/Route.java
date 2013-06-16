package it.michelepiccirillo.tympanon;

public interface Route {
	public void service(HttpRequest req, HttpResponse res) throws Exception;
}
