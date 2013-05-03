package it.michelepiccirillo.tympanon.weblets;

import java.io.*;
import java.net.URLConnection;

import it.michelepiccirillo.tympanon.*;

public class StaticWeblet implements Route {
	private File documentRoot;
	private String index;
	
	@Override
	public void service(HttpRequest req, HttpResponse res) throws IOException {
		File resource = new File(documentRoot + req.getPath()); 
		
		if(!resource.getAbsolutePath().startsWith(documentRoot.getAbsolutePath())) {
			HttpUtils.send403(res);
			return;
		}
		
		if(resource.isDirectory()) {
			resource = new File(resource + File.separator + index);
		}
		
		if(resource.exists()) {
			res.setHeader("Content-Type", URLConnection.guessContentTypeFromName(resource.getName()));
			res.setHeader("Content-Length", String.valueOf(resource.length()));
			
			res.send();
			
			byte[] buf = new byte[1024];
			InputStream in = new BufferedInputStream(new FileInputStream(resource));
			OutputStream out = res.getOutputStream();
			
			for(int n; (n = in.read(buf)) > 0;) {
				out.write(buf, 0, n);
				res.flush();
			}
		} else {
			HttpUtils.send404(res, req.getPath());
		}
	}

	public void initialize(Config config) {
		documentRoot = new File(config.getParameter("docroot", "www")).getAbsoluteFile();		
		index = config.getParameter("index", "index.html");
	}
}
