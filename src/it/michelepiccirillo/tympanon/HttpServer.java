package it.michelepiccirillo.tympanon;

import it.michelepiccirillo.tympanon.HttpResponse.Status;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer implements Runnable {
	private Config config;
	
	private Map<String, Route> routes = new HashMap<String, Route>();
	
	private final InetSocketAddress address;
	private final Executor executor;
	
	public HttpServer(InetSocketAddress address, Executor executor) {
		this.address = address;
		this.executor = executor;
	}
	
	public void run() {
		final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		ServerSocket ss;
		try {
			ss = new ServerSocket(address.getPort(), 0, address.getAddress());
			log.log(Level.INFO, "Server listening on " + ss.getInetAddress() + ":" + ss.getLocalPort());
			
			while(true) {
				final Socket socket = ss.accept();
				final OutputStream out = socket.getOutputStream();
				final InputStream in = socket.getInputStream();
				
				executor.execute(new Runnable() {
					public void run() {
						
						while(!socket.isClosed()) {
							HttpRequest req;
							HttpResponse res = new HttpResponse(out);
							
							try {
								try {
									req = new HttpRequest(in);
									log.log(Level.FINE, req.toString());
					
									res.setStatus(Status.OK);
									res.setHeader("Cache-Control", "max-age=0");
									res.setHeader("Content-Type", "text/html");
									
									try {
										Route route = getWebletFor(req.getPath());
									
										if(route == null) {
											HttpUtils.send404(res, req.getPath());
										} else {
											route.service(req, res);
										}			
									} catch (Exception e) {
										HttpUtils.send500(res, e);
									} 
								} catch (HttpFormatException ex) {
									HttpUtils.send400(res, ex.getMessage());
								} 
					
								res.end();
							} catch (SocketException se) {
								break;
							} catch (IOException e) {
								e.printStackTrace();
							} 
						}
						
						try {
							socket.close();
						} catch (Exception e) {}
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public Config getConfig() {
		return config;
	}
	
	public Route getWebletFor(String path) throws ClassNotFoundException, ClassCastException {
		String className = config.getRouteFor(path);
		if(className == null)
			className = config.getRouteFor("*");
		
		if(className == null)
			return null;
		
		if(!routes.containsKey(className)) {
			try {
				Route route = (Route) Class.forName(className).newInstance();
				//route.initialize(this.getConfig());
				routes.put(className, route);
			} catch (IllegalAccessException e) { 
				return null; 
			} catch (InstantiationException e) { 
				return null; 
			}
		}
		
		return routes.get(className);
	}
	
}
