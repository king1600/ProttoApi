package apis;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import prottoapi.ClientSession;
import prottoapi.ResponseObject;

public class CleverbotApi {
	/*
	 * Wrapper over the Cleverbot API
	 */
	
	public ClientSession session;
	public String HOST     = "www.cleverbot.com";
	public String PROTOCOL = "http://";
	public String RESOURCE = "/webservicemin?uc=165&";
	public String API_URL  = PROTOCOL + HOST + RESOURCE;
	public JSONObject headers;
	public JSONObject data;
	public List<String> conversation;
	public String resp;
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public CleverbotApi() throws Exception {
		
		// create session
		int maxThreads = 8;
		session = new ClientSession(maxThreads);
		
		// create headers
		headers = new JSONObject();
		headers.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		headers.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		headers.put("Accept-Language", "en-us,en;q=0.8,en-us;q=0.5,en;q=0.3");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Cache-Control", "no-cache");
		headers.put("Connection", "keep-alive");
		headers.put("Host", HOST);
		headers.put("Referer", PROTOCOL + HOST + "/");
		headers.put("Pragma", "no-cache");

		// the data that will get passed to Cleverbot's web API
		data = new JSONObject();
		data.put("stimulus", "");
		data.put("cb_settings_language", "");
		data.put("cb_settings_scripting", "no");
		data.put("islearning", 1); // never modified
		data.put("icognoid", "wsf"); // never modified
		data.put("icognocheck", "");
		
		data.put("start", "y"); // never modified
		data.put("sessionid", "");
		data.put("vText8", "");
		data.put("vText7", "");
		data.put("vText6", "");
		data.put("vText5", "");
		data.put("vText4", "");
		data.put("vText3", "");
		data.put("vText2", "");
		data.put("fno", 0); // never modified
		data.put("prevref", "");
		data.put("emotionaloutput", ""); // never modified
		data.put("emotionalhistory", ""); // never modified
		data.put("asbotname", ""); // never modified
		data.put("ttsvoice", ""); // never modified
		data.put("typing", ""); // never modified
		data.put("lineref", "");
		data.put("sub", "Say"); // never modified
		data.put("cleanslate", false); // never modified
		
		// the log of our conversation with Cleverbot
		conversation = new ArrayList<String>();
		resp = new String();
		
		// get the main page to get a cookie
		ResponseObject r = session.get(PROTOCOL + HOST);
		if (r.onReady()) {
			System.out.println("Headers: " + r.headers);
		}
	}
	
	public String ask(String question) throws Exception {
		/*
		 * Asks Cleverbot a question
		 * 
		 * Maintains message history
		 * 
		 * @param q (String) : The question to ask
		 * @return (String)  : Cleverbot's answer
		 */
		
		// set the current question
		data.put("stimulus", question);
		
		// connect to cleverbot's api and remember the response
		resp = _send();
		
		// add the current question to the conversation log
		conversation.add(question);
		
		JSONObject parsed = _parse();
		
		// set data as appropriate
		if (data.getString("sessionid") != "") {
			data.put("sessionid", parsed.getString("conversation_id"));
		}
		
		// add Cleverbot's reply to the conversation log
		conversation.add(parsed.getString("answer"));
		
		return session.unquote(parsed.getString("answer"));
	}
	
	public String _send() throws Exception {
		/*
		 * POST the user's question and all required information to the
		 * Cleverbot API
		 * 
		 * Cleverbot tries to prevent unauthorized access to its API by
		 * obfuscating how it generates the 'icognocheck' token. The token
		 * is currently the md5 checksum of the 10th through the 36th characters of the
		 * encoded data. This may change in the future.
		 * TODO: Order is not guaranteed when urlencoding dict. This hasn't been
		 * a problem yet, but you should look into Ordered Dicts instead
		 */
		
		// set data as appropriate
		if (conversation != null) {
			int linecount = 1;
			List<String> reversed = conversation.subList(0, conversation.size());
			Collections.reverse(reversed);
			for (String line : reversed) {
				linecount++;
				data.put("vText" + linecount, line);
				if (linecount == 8) break;
			}
		}
		
		// generate the token
		String enc_data   = URLEncoder.encode(data.getString("stimulus"), "UTF-8");
		enc_data += "&cb_settings_language=&cb_";
		String digest_txt = enc_data.substring(0, 26);

		
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] digest_data = md.digest(digest_txt.getBytes(session.UTF_8));
		String token = bytesToHex(digest_data);
		data.put("icognocheck", token);
		
		System.out.println(data);
		System.out.println(session.cookies);
		
		// POST data and return string response
		ResponseObject response = session.post(API_URL, null, session.urlencode(data), headers);
		String responseString = "";
		responseString = response.text();
		
		// return data
		return responseString;
	}
	
	public JSONObject _parse() throws Exception {
		/*
		 * Parse Cleverbot's response
		 */
		
		List<String> parts = new ArrayList<String>();
		List<List<String>> parsed = new ArrayList<List<String>>();
		
		for (String s : resp.split("\r\r\r\r\r\r")) parts.add(s);
		parts.remove(parts.size() - 1);
		for (String item : parts) {
			List<String> items = new ArrayList<String>();
			for (String s : item.split("\r")) items.add(s);
			parsed.add(items);
		}
		
		if (parsed.get(0).get(1) == "DENIED") throw new Exception("Cleverbot API Error!");
		
		JSONObject parsed_dict = new JSONObject();
		parsed_dict.put("answer", parsed.get(0).get(0));
		parsed_dict.put("conversation_id", parsed.get(0).get(1));
		try {
			parsed_dict.put("unknown", parsed.get(1).get(parsed.size() - 1));
		} catch (Exception e) {
			Object nullObject = null;
			parsed_dict.put("unknown", nullObject);
		}
		
		return parsed_dict;
	}
	
	private String bytesToHex(byte[] bytes) {
		/*
		 * Convert bytearray to string
		 * from: http://stackoverflow.com/a/9855338
		 */
		char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
