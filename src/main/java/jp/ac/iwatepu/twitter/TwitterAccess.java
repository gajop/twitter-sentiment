package jp.ac.iwatepu.twitter;

import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterAccess {
	String handle;
	String key;
	String keySecret;
	String token;
	String tokenSecret;
	Twitter twitter;
	
	RateLimitStatusWrapper rlsFollowers;
	RateLimitStatusWrapper rlsStatus;
	RateLimitStatusWrapper rlsFriends;
	
	public TwitterAccess(String handle, String key, String keySecret,
			String token, String tokenSecret) {
		super();
		this.handle = handle;
		this.key = key;
		this.keySecret = keySecret;
		this.token = token;
		this.tokenSecret = tokenSecret;
		
		TwitterFactory factory = new TwitterFactory();
	    twitter = factory.getInstance();
		twitter.setOAuthConsumer(key, keySecret);
		twitter.setOAuthAccessToken(new AccessToken(token, tokenSecret));
		
		Map<String, RateLimitStatus> rlsMap;
		try {
			rlsMap = twitter.getRateLimitStatus("followers", "friends", "statuses");
			rlsFollowers = new RateLimitStatusWrapper(rlsMap.get("/followers/ids"));
			rlsFriends = new RateLimitStatusWrapper(rlsMap.get("/friends/ids"));
			rlsStatus = new RateLimitStatusWrapper(rlsMap.get("/statuses/user_timeline"));
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getKeySecret() {
		return keySecret;
	}
	public void setKeySecret(String keySecret) {
		this.keySecret = keySecret;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public Twitter getTwitter() {
		return twitter;
	}
		
}
