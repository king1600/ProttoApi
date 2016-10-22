import org.json.JSONArray;
import org.json.JSONObject;

import prottoapi.ClientSession;
import prottoapi.ResponseObject;

public class Test {

	public static void main(String[] args) {
		try {
			String url = "https://www.google.com/humans.txt";

			int workers = 1;
			ClientSession session = new ClientSession(workers);
			
			ResponseObject resp = session.get(url);
			if (resp.onReady() && resp.status == 200) {
				String content = resp.text();
				System.out.println(content);
			}
			
			session.close();
		}
		catch (Exception e) 
		{
			System.err.println("Error!");
			e.printStackTrace();
		}
	}

}
