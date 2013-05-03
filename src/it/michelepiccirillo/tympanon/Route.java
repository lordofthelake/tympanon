package it.michelepiccirillo.tympanon;
import java.io.*;

public interface Route {
	public void service(HttpRequest req, HttpResponse res) throws IOException;
}
