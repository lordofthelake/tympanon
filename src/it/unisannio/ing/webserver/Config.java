package it.unisannio.ing.webserver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Config {
	private static DocumentBuilder builder = null;
	
	static {
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch(ParserConfigurationException e) {}
	}
	
	private Map<String, String> parameters = new HashMap<String, String>();
	private Map<String, String> routes = new HashMap<String, String>();
	
	public Config(File file) throws SAXException, IOException {
		Document xml = builder.parse(file);
		
		Element root = xml.getDocumentElement();
		Node n = null;
		
		Element config = (Element) root.getElementsByTagName("config").item(0);
		for(int i = 0; (n = config.getChildNodes().item(i)) != null; ++i) {
			if(!(n instanceof Element)) continue;
			Element e = (Element) n;
			parameters.put(e.getTagName(), e.getTextContent());
		}
		
		Element routes = (Element) root.getElementsByTagName("routes").item(0);
		for(int i = 0; (n = routes.getChildNodes().item(i)) != null; ++i) {
			if(!(n instanceof Element)) continue;
			Element e = (Element) n;
			
			this.routes.put(e.getAttribute("for"), e.getTextContent());
		}
	}
	
	public String getParameter(String key, String defaultValue) {
		return parameters.containsKey(key) ? parameters.get(key) : defaultValue;
	}
	
	public int getParameter(String key, int defaultValue) {
		return parameters.containsKey(key) ? Integer.valueOf(parameters.get(key)) : defaultValue;
	}
	
	public String getParameter(String key) {
		return getParameter(key, null);
	}
	
	public String getRouteFor(String path) {
		return routes.get(path);
	}
}
