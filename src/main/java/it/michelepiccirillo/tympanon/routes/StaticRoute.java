package it.michelepiccirillo.tympanon.routes;

import java.io.*;
import java.net.URLConnection;

import it.michelepiccirillo.tympanon.*;

public class StaticRoute extends AbstractRoute {
	private File documentRoot;
	private String index;
	
	public StaticRoute(File documentRoot, String index) {
		this.documentRoot = documentRoot;
		this.index = index;
	}
	
	@Override
	public void get(HttpRequest req, HttpResponse res) throws IOException {
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
			
			res.flush();
			
			InputStream in = null;
			try {
				byte[] buf = new byte[1024];
				in = new BufferedInputStream(new FileInputStream(resource));
				OutputStream out = res.getOutputStream();
				
				for(int n; (n = in.read(buf)) > 0;) {
					out.write(buf, 0, n);
					res.flush();
				}
			} finally {
				if(in != null)
					in.close();
			}
		} else {
			HttpUtils.send404(res, req.getPath());
		}
	}

}
