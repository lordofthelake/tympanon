package it.unisannio.ing.webserver;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

public class Webserver implements Runnable {
	private Config config;
	private Map<String, Weblet> weblets = new HashMap<String, Weblet>();
	
	public Webserver(String configFile) throws SAXException, IOException {
		this.config = new Config(new File(configFile));
	}
	
	public void run() {
		ServerSocket ss;
		try {
			ss = new ServerSocket(config.getParameter("port", 8080));
			System.out.println("Server started on port " + ss.getLocalPort());
			while(true) {
				Socket client = ss.accept();
				new Worker(this, client).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Weblet getWebletFor(String path) throws ClassNotFoundException, ClassCastException {
		String className = config.getRouteFor(path);
		if(className == null)
			className = config.getRouteFor("*");
		
		if(className == null)
			return null;
		
		if(!weblets.containsKey(className)) {
			try {
				Weblet weblet = (Weblet) Class.forName(className).newInstance();
				weblet.initialize(this.getConfig());
				weblets.put(className, weblet);
			} catch (IllegalAccessException e) { 
				return null; 
			} catch (InstantiationException e) { 
				return null; 
			}
		}
		
		return weblets.get(className);
	}
	
	public static void main(String[] args) throws Exception {
		new Webserver("config.xml").run();
	}
}
