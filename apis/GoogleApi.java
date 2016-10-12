package apis;

import org.json.JSONObject;

import prottoapi.ClientSession;
import prottoapi.ResponseObject;

public class GoogleApi 
{
	private String API_KEY;
	public ClientSession session;
	private String URLSHORTEN;
	
	public GoogleApi(String apiKey) 
	{
		API_KEY    = apiKey;
		URLSHORTEN = "https://content.googleapis.com/urlshortener/v1/url";
		session    = new ClientSession(4);
	}
	
	public String shortenUrl(String url) throws Exception 
	{
		JSONObject formdata = new JSONObject();
		formdata.put("longUrl", url);
		
		String requestUrl   = URLSHORTEN + "?key=" + API_KEY + "&alt=json";
		
		ResponseObject resp = session.get(requestUrl, null, formdata);
		return resp.json().getString("id");
	}
}
