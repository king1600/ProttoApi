package prottoapi;

import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.concurrent.Callable;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class AsyncRequestObject implements Callable<ResponseObject> {
	// Connection objects
	public HttpURLConnection conn;
	public String url;
	public static final int BUFFER_SIZE = 32768;
	
	// Constant Charset
	public static final String UTF_8 = StandardCharsets.UTF_8.toString();
	
	// final Response object
	public ResponseObject response;
	
	// perform types of request
	public AsyncRequestObject
	(String _method, String _url, JSONObject _params, Object _form, JSONObject _headers)
	throws Exception
	{	
		// initialize response object
		response = new ResponseObject();
		
		// create url
		url = _url;
		if (_params != null) {
			url += buildPostQuery(_params);
		}
		
		//conn.setRequestProperty("Accept-Charset", UTF_8);
		conn = (HttpURLConnection)new URL(url).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		// add any extra headers
		if (_headers != null)
		{
			Iterator<?> keys = _headers.keys();
			while (keys.hasNext()) 
			{
				String key   = (String)keys.next();
				String value = new String("");
				if (_headers.get(key) != null) 
				{
					value = _headers.get(key).toString();
					conn.setRequestProperty(key, value);
				}
			}
		}
		
		// Perform HTTP Request
		switch(_method.toLowerCase()) {
			case "get":  GetRequest(conn, _form);
						 break;
			case "post": PostRequest(conn, _form);
						 break;
			default:	 ;
						 break;
		}
	}
	
	/******** HTTP Functions (begin) ***********/
	
	// Perform HTTP Get request
	public void GetRequest(HttpURLConnection conn, Object formdata) throws Exception {
		// set method
		conn.setRequestMethod("GET");
		
		// Write data if any
		byte[] dataBytes = getFormData(formdata);
		if (dataBytes != null) 
		{
			conn.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
			try (OutputStream out = conn.getOutputStream()) 
			{
				out.write(dataBytes);
			}
		}
		
		// set basic info in a thread
		try
		{
			AsyncInfoLoader runnable = new AsyncInfoLoader(this);
			Thread infoLoader = new Thread(runnable);
			infoLoader.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Perform HTTP Post request
	public void PostRequest(HttpURLConnection conn, Object formdata) throws Exception {
		// set method
		conn.setRequestMethod("POST");
		
		// Write data if any
		byte[] dataBytes = getFormData(formdata);
		if (dataBytes != null) 
		{
			conn.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
			try (OutputStream out = conn.getOutputStream())
			{
				out.write(dataBytes);
			}
		}
		
		// set basic info in a thread
		try
		{
			AsyncInfoLoader runnable = new AsyncInfoLoader(this);
			Thread infoLoader = new Thread(runnable);
			infoLoader.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Get Formdata as bt
	public byte[] getFormData(Object formdata) throws Exception
	{
		// byte data to be returned
		byte[] data = null;
		
		// Check if formdata is valid
		if (formdata != null) {
			// Using Json object
			if (formdata instanceof JSONObject) 
			{
				conn.setRequestProperty("Content-Type", "application/json");
				JSONObject json = (JSONObject)formdata;
				data            = json.toString().getBytes(UTF_8);
			}
			
			// Using String
			else 
			{
				String charset = "application/x-www-form-urlencoded;charset=" + UTF_8;
				conn.setRequestProperty("Content-Type", charset);
				data = formdata.toString().getBytes(UTF_8);
			}
		}
		
		// return byte data
		return data;
	}
	
	/****************************************/
	
	/******** Connection & Parse Functions (begin) ***********/
	
	// Convert json object into url parameters
	public static String buildPostQuery(JSONObject json) throws Exception 
	{
		String postQuery = "";
		Iterator<?> keys = json.keys();
		
		// convert keys into string parameters
		while (keys.hasNext()) 
		{
			String key = (String)keys.next();
			if (json.get(key) != null) 
			{
				if (postQuery.length() == 0) postQuery += "?";
				else postQuery += "&";
				postQuery += key.replaceAll("&", "");
				postQuery += "=";
				postQuery += quote(json.get(key).toString());
			}
		}
		
		// return completed formed query
		return postQuery;
	}
	
	// URL Quote a string
	public static String quote(String data) throws UnsupportedEncodingException {
		return URLEncoder.encode(data, UTF_8).toString();
	}
		
	// Url De-Quote string
	public String unquote(String data) throws UnsupportedEncodingException {
		return URLDecoder.decode(data, UTF_8).toString();
	}
	
	/****************************************/
	
	// Return response object
	@Override
	public ResponseObject call() throws Exception 
	{
		return this.response;
	}
}
