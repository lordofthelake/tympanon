package it.michelepiccirillo.tympanon;


import java.io.*;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
	
	enum Method { 
		OPTIONS("OPTIONS"),
		GET("GET"),
		HEAD("HEAD"),
		POST("POST", true),
		PUT("PUT", true),
		DELETE("DELETE"),
		TRACE("TRACE"),
		CONNECT("CONNECT");
	
		private final String token;
		private final boolean hasBody;
		
		private Method(String token, boolean hasBody) {
			this.token = token;
			this.hasBody = hasBody;
		}
		
		private Method(String token) {
			this(token, false);
		}
		
		public boolean hasBody() {
			return hasBody;
		}
		
		public String getToken() {
			return token;
		}
		
		@Override
		public String toString() {
			return token;
		}
	}
	
	private static final Pattern REQUEST_LINE = Pattern.compile("([A-Z]+) ([^\\s]*) HTTP/1\\..");
	private static final Pattern HEADER_LINE = Pattern.compile("([A-Za-z-]+): (.*)");
	
	private InputStream is;
	private BufferedReader r;
	
	private HttpRequest.Method method;
	private String path;
	private Map<String, String> headers;
	private Map<String, String> parameters = Collections.emptyMap();
	
	HttpRequest(InputStream is) throws IOException, HttpFormatException {
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
		
		String meth = m.group(1).toUpperCase();
		try {
			method = Enum.valueOf(Method.class, meth);
		} catch (Exception e) {
			throw new HttpFormatException("Unsupported HTTP method: " + meth, e);
		}
		
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
		
		if(method.hasBody() && getHeader("Content-Type").equals("application/x-www-form-urlencoded")) {
			int length = Integer.valueOf(getHeader("Content-Length"));
			char[] buf = new char[100];
			StringBuffer body = new StringBuffer();
			for(int n; length > body.length() && (n = r.read(buf, 0, buf.length)) > 0;) {
				body.append(buf, 0, n);
			}
			
			if(body.length() > 0) queryString = body.toString();
			
			// we have already consumed the body, but let's allow client to read raw input anyway
			this.is = new ByteArrayInputStream(queryString.getBytes());
			this.r = new BufferedReader(new InputStreamReader(is));
		} else if (!method.hasBody()) {
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

	public InputStream getInputStream() throws IOException {
		return is;
	}

	public BufferedReader getReader() throws IOException {
		return r;
	}

	public String getParameter(String param) {
		return parameters.get(param);
	}

	public Iterable<String> getHeaders() {
		return headers.keySet();
	}

	public Iterable<String> getParameters() {
		return parameters.keySet();
	}

	public String getHeader(String header) {
		return headers.get(header);
	}

	public Method getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}
	
	public String toString() {
		return method + " " + path + "?" + parameters + " " + headers;
	}
}
