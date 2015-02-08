package jp.ac.iwatepu.twitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterObtainKeys {
	public static void main(String args[]) throws Exception{
		new TwitterObtainKeys().run();
	}

	private void run() throws Exception {
		Twitter twitter = TwitterManager.getInstance().twitterAccess.get(0).getTwitter();
		twitter.setOAuthAccessToken(null);
		RequestToken requestToken = twitter.getOAuthRequestToken();
		AccessToken accessToken = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (null == accessToken) {
			System.out.println("Open the following URL and grant access to your account:");
			System.out.println(requestToken.getAuthorizationURL());
			System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
			String pin = br.readLine();
			try{
				if(pin.length() > 0){
					accessToken = twitter.getOAuthAccessToken(requestToken, pin);
				}else{
					accessToken = twitter.getOAuthAccessToken();
				}
			} catch (TwitterException te) {
				if(401 == te.getStatusCode()){
					System.out.println("Unable to get the access token.");
				}else{
					te.printStackTrace();
				}
			}
		}
		storeAccessToken(accessToken);
		//persist to the accessToken for future reference.

		System.exit(0);
	}
	private static void storeAccessToken(AccessToken accessToken){
		// TODO: append to twitter-keys.json.. or just print it and I can copy/paste it then?
		System.out.println(
				"\"handle\":  \"" + accessToken.getScreenName() + "\"\n" +
				"\"token\":  \"" + accessToken.getToken() + "\"\n" +
				"\"tokenSecret\":   \"" + accessToken.getTokenSecret() + "\n");
	}
}
