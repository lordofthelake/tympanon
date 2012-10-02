package it.unisannio.ing.webserver;
import java.io.*;

public interface Weblet {
	public void initialize(Config config);
	public void service(HttpRequest req, HttpResponse res) throws IOException ;
}
