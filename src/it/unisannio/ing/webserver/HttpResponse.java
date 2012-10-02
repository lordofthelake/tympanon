package it.unisannio.ing.webserver;

import java.io.*;

public interface HttpResponse {

    public OutputStream getOutputStream() throws IOException;

    public PrintWriter getWriter() throws IOException;
    
    public void setStatus(String statusLine);

    public void setHeader(String header, String value);

    public void send() throws IOException;
    
    public void flush() throws IOException;
    
    public void end() throws IOException;
}
