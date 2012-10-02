package it.unisannio.ing.webserver;

import static it.unisannio.ing.webserver.HttpRequest.Method.GET;
import static it.unisannio.ing.webserver.HttpRequest.Method.POST;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Worker extends Thread {
	private Webserver server;
	private Socket socket;

	private OutputStream out;
	private InputStream in;
	
	public Worker(Webserver server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		
		in = socket.getInputStream();
		out = socket.getOutputStream();
	}
	
	public void run() {
		
		
		while(!socket.isClosed()) {
			HttpRequest req;
			HttpResponse res = new HttpResponseImpl(out);
			
			try {
				try {
					req = new HttpRequestImpl(in);
					System.out.println(req);
	
					res.setHeader("Cache-Control", "max-age=0");
					res.setHeader("Content-Type", "text/html");
					
					try {
						Weblet weblet = server.getWebletFor(req.getPath());
					
						if(weblet == null) {
							HttpUtils.send404(res, req.getPath());
						} else {
							weblet.service(req, res);
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

	private static class HttpResponseImpl implements HttpResponse {

		private Map<String, String> headers = new HashMap<String, String>();
		private String status;
		private boolean headersSent = false;
		private boolean chunked = false;

		private OutputStream out;
		
		private ByteArrayOutputStream dataOut;
		private PrintWriter writer;
		
		HttpResponseImpl(OutputStream out) {
			this.out = out;
			this.dataOut = new ByteArrayOutputStream();
			this.writer = new PrintWriter(new OutputStreamWriter(dataOut));
		}
		
		@Override
		public OutputStream getOutputStream() throws IOException {
			return dataOut;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			return writer;
		}

		@Override
		public void setStatus(String statusLine) {
			if(headersSent) 
				throw new IllegalStateException("Headers already sent.");
			
			status = statusLine;
		}

		@Override
		public void setHeader(String header, String value) {
			if(headersSent) 
				throw new IllegalStateException("Headers already sent.");
			
			headers.put(header, value);
		}

		@Override
		public void flush() throws IOException {
			if(!headersSent)
				send();
			
			writer.flush();
			
			if(dataOut.size() > 0) {
				PrintWriter sockWriter = new PrintWriter(out);
				if(chunked) {
					sockWriter.write(Integer.toHexString(dataOut.size()) + "\r\n");
					sockWriter.flush();
				}
				
				dataOut.writeTo(out);
				dataOut.reset();
				
				if(chunked) {
					sockWriter.write("\r\n");
					sockWriter.flush();
				}
			}
			
			out.flush();
		}
		
		public void end() throws IOException {
			flush();
			
			if(chunked) {
				PrintWriter sockWriter = new PrintWriter(out);
				sockWriter.write("0\r\n\r\n");
				sockWriter.flush();
				out.flush();
			}	
		}

		public void send() throws IOException {
			if(!headersSent) {
				PrintWriter writer = new PrintWriter(this.out);
				
				if(status == null)
					status = "200 OK";
				
				writer.write("HTTP/1.1 " + status + "\r\n");
				
				if(!headers.containsKey("Content-Length")) {
					chunked = true;
					setHeader("Transfer-Encoding", "chunked");
				}
				
				for(String key : headers.keySet()) {
					writer.print(key + ": " + headers.get(key) + "\r\n");
				}
				
				writer.print("\r\n");
				writer.flush();
				headersSent = true;
			}
			
			flush();			
		}
	}
	
	private static class HttpRequestImpl implements HttpRequest{
		
		private static final Pattern REQUEST_LINE = Pattern.compile("([A-Z]+) ([^\\s]*) HTTP/1\\..");
		private static final Pattern HEADER_LINE = Pattern.compile("([A-Za-z-]+): (.*)");
		
		private InputStream is;
		private BufferedReader r;
		
		private HttpRequest.Method method;
		private String path;
		private Map<String, String> headers;
		private Map<String, String> parameters = Collections.emptyMap();
		
		HttpRequestImpl(InputStream is) throws IOException, HttpFormatException {
			this.is = is;
			this.r = new BufferedReader(new InputStreamReader(is));
			
			parse();
		}
		
		private void parse() throws IOException, HttpFormatException {
			String requestLine = r.readLine();
			if(requestLine == null)
				throw new SocketException("Socket closed by peer.");
			
			Matcher m = REQUEST_LINE.matcher(requestLine);

			if(!m.matches())
				throw new HttpFormatException("Bad request line: '" + requestLine + "'.");
			
			String meth = m.group(1);
			
			if("GET".equals(meth)) {
				method = GET;
			} else if ("POST".equals(meth)) {
				method = POST;
			} else
				throw new HttpFormatException("Unsupported HTTP method: " + meth);
			
			path = m.group(2);
			
			headers = new HashMap<String, String>();
			
			for(String line; (line = r.readLine()) != null && !line.isEmpty();) {
				Matcher m1 = HEADER_LINE.matcher(line);
				if(!m1.matches())
					throw new HttpFormatException("Bad header '" + line + "'");
				
				String key = m1.group(1);
				String value = m1.group(2);

				headers.put(key, value);
			}
					
			String queryString = null;
			
			if(method == POST && getHeader("Content-Type").equals("application/x-www-form-urlencoded")) {
				int length = Integer.valueOf(getHeader("Content-Length"));
				char[] buf = new char[100];
				StringBuffer body = new StringBuffer();
				for(int n; length > body.length() && (n = r.read(buf, 0, buf.length)) > 0;) {
					body.append(buf, 0, n);
				}
				
				if(body.length() > 0) queryString = body.toString();
			} else if (method == GET) {
				int question = path.indexOf('?');
				if(question != -1) {
					queryString = path.substring(question + 1);
					path = path.substring(0, question);
				}
			}

			if(queryString != null) {
				queryString = URLDecoder.decode(queryString, "utf-8");
				String[] params = queryString.split("&");
				parameters = new HashMap<String, String>();
				for(String p : params) {
					String[] pair = p.split("=");
					parameters.put(pair[0], pair.length > 1 ? pair[1] : "");
				}
			}
			
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return is;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return r;
		}

		@Override
		public String getParameter(String param) {
			return parameters.get(param);
		}

		@Override
		public Iterable<String> getHeaders() {
			return headers.keySet();
		}

		@Override
		public Iterable<String> getParameters() {
			return parameters.keySet();
		}

		@Override
		public String getHeader(String header) {
			return headers.get(header);
		}

		@Override
		public Method getMethod() {
			return method;
		}

		@Override
		public String getPath() {
			return path;
		}
		
		public String toString() {
			return method + " " + path + "?" + parameters + " " + headers;
		}
	}
}
