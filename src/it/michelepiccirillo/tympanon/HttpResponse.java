package it.michelepiccirillo.tympanon;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
	enum Status {
		CONTINUE(100, "Continue"),
		SWITCHING_PROTOCOLS(101, "Switching protocols"),
		OK(200, "OK"),
		CREATED(201, "Created"),
		ACCEPTED(202, "Accepted"),
		NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
		NO_CONTENT(204, "No Content"),
		RESET_CONTENT(205, "Reset Content"),
		PARTIAL_CONTENT(206, "Partial Content"),
		MULTIPLE_CHOICES(300, "Multiple Choices"),
		MOVED_PERMANENTLY(301, "Moved Permanently"),
		FOUND(302, "Found"),
		SEE_OTHER(303, "See Other"),
		NOT_MODIFIED(304, "Not Modified"),
		USE_PROXY(305, "Use Proxy"),
		TEMPORARY_REDIRECT(307, "Temporary Redirect"),
		BAD_REQUEST(400, "Bad Request"),
		UNAUTHORIZED(401, "Unauthorized"),
		FORBIDDEN(403, "Forbidden"),
		NOT_FOUND(404, "Not Found"),
		METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
		NOT_ACCEPTABLE(406, "Not Acceptable"),
		PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
		REQUEST_TIMEOUT(408, "Request Timeout"),
		CONFLICT(409, "Conflict"),
		GONE(410, "Gone"),
		LENGTH_REQUIRED(411, "Length Required"),
		PRECONDITION_FAILED(412, "Precondition Failed"),
		REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
		REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
		UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
		REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
		EXPECTATION_FAILED(417, "Expectation Failed"),
		INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
		NOT_IMPLEMENTED(501, "Not Implemented"),
		BAD_GATEWAY(502, "Bad Gateway"),
		SERVICE_UNAVAILABLE(503, "Service Unavailable"),
		GATEWAY_TIMEOUT(504, "Gateway Timeout"),
		HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");
		
		private final String reason;
		private final int status;
		
		private Status(int status, String reason) {
			this.status = status;
			this.reason = reason;
		}
		
		public int getStatusCode() {
			return status;
		}
		
		public String getReasonPhrase() {
			return reason;
		}
		
		@Override
		public String toString() {
			return status + " " + reason;
		}
		
	}
	private Map<String, String> headers = new HashMap<String, String>();
	private Status status;
	private boolean headersSent = false;
	private boolean chunked = false;

	private OutputStream out;
	
	private ByteArrayOutputStream dataOut;
	private PrintWriter writer;
	
	HttpResponse(OutputStream out) {
		this.out = out;
		this.dataOut = new ByteArrayOutputStream();
		this.writer = new PrintWriter(new OutputStreamWriter(dataOut));
	}
	
	public OutputStream getOutputStream() throws IOException {
		return dataOut;
	}

	
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	
	public void setStatus(Status status) {
		if(headersSent) 
			throw new IllegalStateException("Headers already sent.");
		
		this.status = status;
	}

	
	public void setHeader(String header, String value) {
		if(headersSent) 
			throw new IllegalStateException("Headers already sent.");
		
		headers.put(header, value);
	}

	
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
				status = Status.OK;
			
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
