package it.unisannio.ing.webserver;



import java.io.*;

public interface HttpRequest {
	
	enum Method { GET, POST; }

    public InputStream getInputStream() throws IOException;

    public BufferedReader getReader() throws IOException;

    public Method getMethod();
    
    public String getPath();
    
    public Iterable<String> getHeaders();
    
    public Iterable<String> getParameters();

    public String getHeader(String header);

    public String getParameter(String param);

}
