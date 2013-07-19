package it.michelepiccirillo.tympanon;

import it.michelepiccirillo.tympanon.HttpResponse.Status;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpServer implements Runnable {
	
	private Map<String, Route> routes = new HashMap<String, Route>();
	private Map<Pattern, Route> matchers = new LinkedHashMap<Pattern, Route>();
	
	private final InetSocketAddress address;
	private final ExecutorService executor;
	private Route defaultRoute;
	
	private boolean stopped = false;
	
	private ServerSocket ss;
	
	public HttpServer(InetSocketAddress address, ExecutorService executor) {
		this.address = address;
		this.executor = executor;
	}
	
	public HttpServer(InetSocketAddress address) {
		this(address, Executors.newCachedThreadPool());
	}
	
	public synchronized void bind() throws IOException {
		if(ss == null) {
			ss = new ServerSocket();
		}
		
		if(!ss.isBound())
			ss.bind(address);
	}
	
	public void run() {
		final Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		try {
			bind();
			log.log(Level.INFO, "Server listening on " + ss.getInetAddress() + ":" + ss.getLocalPort());
			
			while(!stopped) {
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
									req = new HttpRequest(in, (InetSocketAddress) socket.getRemoteSocketAddress());
									log.log(Level.FINE, req.toString());
					
									res.setStatus(Status.OK);
									res.setHeader("Cache-Control", "max-age=0");
									res.setHeader("Content-Type", "text/html");
									
									try {
										Route route = getRoute(req.getPath());
									
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
								log.log(Level.WARNING, "I/O Error", e);
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
	
	
	
	public InetSocketAddress getInetSocketAddress() {
		return address;
	}
	
	public int getUsedPort() {
		try {
			bind();
			return ss.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	
	public void stop() throws IOException {
		stopped = true;
		executor.shutdownNow();
		
		if(ss == null)
			return;
		
		ss.close();
	}
	
	public HttpServer route(String url, Route route) {
		if(url == null)
			throw new IllegalArgumentException("URL cannot be null");
		
		if(route == null)
			routes.remove(route);
		else
			routes.put(url, route);
		
		return this;
	}
	
	public HttpServer route(Pattern pattern, Route route) {
		if(pattern == null)
			throw new IllegalArgumentException("URL cannot be null");
		
		if(route == null)
			matchers.remove(pattern);
		else
			matchers.put(pattern, route);
		
		return this;
	}
	
	public HttpServer route(Route route) {
		this.defaultRoute = route;
		
		return this;
	}
	
	public Route getRoute(String path) {
		if(routes.containsKey(path))
			return routes.get(path);
		
		for(Map.Entry<Pattern, Route> e : matchers.entrySet()) {
			Pattern p = e.getKey();
			Matcher m = p.matcher(path);
			if(m.matches())
				return e.getValue();
		}
		
		return defaultRoute;
		
	}
	
}
