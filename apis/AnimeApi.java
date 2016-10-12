package apis;

import org.json.JSONObject;

import prottoapi.ClientSession;
import prottoapi.ResponseObject;

public class AnimeApi 
{
	public ClientSession session;
	private String animeURL = "https://myanimelist.net/api/anime/search.xml";
	private String username;
	private String password;
	
	public AnimeApi (String user, String pass) 
	{
		session = new ClientSession(4);
		username = user;
		password = pass;
	}
	
	public String infoXML(String query) throws Exception
	{
		String authString = "Basic ";
		authString += session.encodeBase64(username + ":" + password);
		
		String paramString = session.quote(query);
		JSONObject params  = new JSONObject();
		params.put("q", paramString);
		
		JSONObject headers = new JSONObject();
		headers.put("Authorization", authString);
		
		ResponseObject resp = session.get(animeURL, params, null, headers);
		return resp.text();
	}
}
