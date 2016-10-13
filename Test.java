import org.json.JSONArray;
import org.json.JSONObject;

import prottoapi.ClientSession;
import prottoapi.ResponseObject;

public class Test {

	public static void main(String[] args) {
		try {
			String osuKey = "apikey";
			String osuLink = "https://osu.ppy.sh/api/get_user";
			String userQuery = "randomuser";
			
			ClientSession session = new ClientSession(1);
			
			JSONObject params = new JSONObject();
			params.put("u", userQuery);
			params.put("k", osuKey);
			
			ResponseObject resp = session.get(osuLink, params);
			if (resp.onReady()) 
			{
				JSONArray info = new JSONArray(resp.text());
				System.out.println("Array: \n" + info.toString());
			}
			
			session.close();
		}
		catch (Exception e) 
		{
			System.err.println("ERror!");
			e.printStackTrace();
		}
	}

}
