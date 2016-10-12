package examples;

import apis.GoogleApi;

public class ShortenUrl {
	
	public static void google(String[] args) {
		String API_KEY  = "";
		GoogleApi googl = new GoogleApi(API_KEY);
		
		try {
			String linkToShorten = "http://github.com";
			String shortenedLink = googl.shortenUrl(linkToShorten);
			System.out.println(shortenedLink);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		googl.session.close();
	}
}
