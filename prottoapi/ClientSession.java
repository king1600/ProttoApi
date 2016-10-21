package prottoapi;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

public class ClientSession {
	private ExecutorService pool;
	public CookieManager cookies;
	public final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;
	public final String UTF_8          = UTF_8_CHARSET.toString();
	
	public ClientSession(int workerNum)
	{
		// create thread pool
		pool = Executors.newFixedThreadPool(workerNum);
		
		// create cookie manager
		cookies = new CookieManager();
	}
	
	// close execution pool
	public void close()
	{
		pool.shutdownNow();
	}
	
	// convert string to base64
	public String encodeBase64(String data) throws Exception
	{
		return Base64.getEncoder().encodeToString(data.getBytes(UTF_8));
	}
	
	// URL Quote a string
	public String quote(String data) throws UnsupportedEncodingException {
		return URLEncoder.encode(data, UTF_8).toString();
	}
			
	// Url De-Quote string
	public String unquote(String data) throws UnsupportedEncodingException {
		return URLDecoder.decode(data, UTF_8).toString();
	}
	
	// url Query encode
	public String urlencode(JSONObject data) throws Exception {
		String query = "";
		Iterator<?> keys = data.keys();
		
		// convert keys into string parameters
		while (keys.hasNext()) 
		{
			String key = (String)keys.next();
			if ( data.get(key) instanceof Object ) 
			{
				if (query.length() == 0) query += "?";
				else query += "&";
				query += key.replaceAll("&", "");
				query += "=";
				query += quote(data.get(key).toString());
			}
		}
		
		// return completed formed query
		return query;
	}
		
	
	public ResponseObject get(Object...args) throws Exception
	{
		// Get Arguments
		String url         = args.length > 0 ? (String)args[0]     : null;
		JSONObject params  = args.length > 1 ? (JSONObject)args[1] : null;
		Object formdata    = args.length > 2 ? args[2]             : null;
		JSONObject headers = args.length > 3 ? (JSONObject)args[3] : null;
		
		// Perform request and get reponse
		Future<ResponseObject> response = pool.submit(
				new AsyncRequestObject("get", url, params, formdata, headers, cookies)
		);
		
		// return Response object
		return response.get();
	}
	
	public ResponseObject post(Object...args) throws Exception
	{
		// Get Arguments
		String url         = args.length > 0 ? (String)args[0]     : null;
		JSONObject params  = args.length > 1 ? (JSONObject)args[1] : null;
		Object formdata    = args.length > 2 ? args[2]             : null;
		JSONObject headers = args.length > 3 ? (JSONObject)args[3] : null;
		
		// Perform request and get response
		Future<ResponseObject> response = pool.submit(
				new AsyncRequestObject("post", url, params, formdata, headers, cookies)
		);
		
		// return Response object
		return response.get();
	}
}
