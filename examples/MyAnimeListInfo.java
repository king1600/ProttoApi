package examples;

import apis.AnimeApi;

public class MyAnimeListInfo {
	
	public static void anime(String[] args) {
		String myanimelistUsername = "";
		String myanimelistPassword = "";
		
		AnimeApi anime = new AnimeApi(
				myanimelistUsername, myanimelistPassword);
		
		try {
			String animeSearch = "naruto";
			String xml         = anime.infoXML(animeSearch);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		anime.session.close();
	}
}
