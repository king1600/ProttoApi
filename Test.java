import org.json.JSONArray;
import org.json.JSONObject;

import apis.CleverbotApi;
import prottoapi.ClientSession;
import prottoapi.ResponseObject;

public class Test {

	public static void main(String[] args) {
		try {
			CleverbotApi cleverbot = new CleverbotApi();
			String response = cleverbot.ask("Hey how are you?");
			System.out.println("Response: " + response);
		}
		catch (Exception e) 
		{
			System.err.println("ERror!");
			e.printStackTrace();
		}
	}

}
