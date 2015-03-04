package jp.ac.iwatepu.twitter;

import java.io.FileReader;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterManager {
	private static final Logger log = Logger.getLogger(TwitterManager.class.getName());
	
	private String twitterKeysFileName = "twitter-keys.json";
	private static TwitterManager instance = new TwitterManager();

	Vector<TwitterAccess> twitterAccess = new Vector<TwitterAccess>();
	
	Lock twitterAccessLock = new ReentrantLock();
	
	public static TwitterManager getInstance() {
		return instance;
	}

	private TwitterManager() {
		init();
	}
	private void init() {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(twitterKeysFileName));

			JSONArray keys = (JSONArray) obj;

			for (Object objKey : keys) {
				JSONObject jsonKey = (JSONObject) objKey;

				String handle = (String) jsonKey.get("handle");
				String key = (String) jsonKey.get("key");
				String keySecret = (String) jsonKey.get("keySecret");
				String token = (String) jsonKey.get("token");
				String tokenSecret = (String) jsonKey.get("tokenSecret");
				final TwitterAccess access = new TwitterAccess(handle, key, keySecret, token, tokenSecret);
				access.getTwitter().addRateLimitStatusListener(new RateLimitStatusListener() {
					//TODO: this might cause synchronization issues
					public void onRateLimitStatus(RateLimitStatusEvent rlse) { 
						updateRLS(access, rlse);
					}
					public void onRateLimitReached(RateLimitStatusEvent rls) {
						log.warning("Rate limit status reached");
					}
				});
				twitterAccess.add(access);
				log.info("Added twitter access key: " + access);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void updateRLS(TwitterAccess access, RateLimitStatusEvent rlse) {
		RateLimitStatus rls = rlse.getRateLimitStatus();
		//log.info(rls + " (" + currentAccess + ")");
		if (currentAccess != null) {
			switch (currentAccess) {
				case followers: access.rlsFollowers = new RateLimitStatusWrapper(rls); break;
				case friends: access.rlsFriends = new RateLimitStatusWrapper(rls); break;
				case status: access.rlsStatus = new RateLimitStatusWrapper(rls); break;
			}
		}
	}

	public boolean canUse(RateLimitStatusWrapper rls) {		
		/*if (!(rls.getRemaining() > 0 || rls.getSecondsUntilReset() <= 0)) {
			System.out.println("can't use:  " + rls.getSecondsUntilReset());
		}*/
		return rls.getRemaining() > 0 || rls.getSecondsUntilReset() <= 0;	
	}
	
	public enum AccessType { followers, friends, status } ;
	AccessType currentAccess;
	
	private TwitterAccess getTwitterAccess(AccessType accessType) throws TwitterException {	
		twitterAccessLock.lock();
		long waitTime = Long.MAX_VALUE;
		do {
			for (TwitterAccess access : twitterAccess) {
				RateLimitStatusWrapper rls;
				
				switch (accessType) {
					case followers: rls = access.rlsFollowers; break;
					case friends: rls = access.rlsFriends; break;
					case status: rls = access.rlsStatus; break;
					default: return null;
				}
				if (canUse(rls)) {
					currentAccess = accessType;
				//	log.fine("Using: " + access.getHandle() + " (" + accessType + ")");
					return access;
				} else {
					long newWaitTime = rls.getSecondsUntilReset();
					if (newWaitTime < waitTime) {
						waitTime = newWaitTime;
					}					
				}
			}	
			twitterAccessLock.unlock();
			try {
				if (waitTime < 0) {
				    System.out.println("wait time is negative " + waitTime);
				}
				waitTime = Math.max(waitTime, 1);
				log.info("Waiting: " + waitTime + " seconds. (" + accessType + ")");				
				Thread.sleep(waitTime * 1000);				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				twitterAccessLock.lock();
			}
		} while (true);
	}
	
	private IDs _getFollowersIDs(Twitter twitter, long userId, long cursor) throws TwitterException {
		return twitter.getFollowersIDs(userId, cursor);
	}
	public IDs getFollowersIDs(long userId, long cursor) throws TwitterException {
		IDs ids;
		try {
			Twitter twitter = getTwitterAccess(AccessType.followers).getTwitter();
			ids = _getFollowersIDs(twitter, userId, cursor);
		} finally {
			twitterAccessLock.unlock();			
		}
		
		return ids;
	}
	
	public IDs _getFriendsIDs(Twitter twitter, long userId, long cursor) throws TwitterException {
		return twitter.getFriendsIDs(userId, cursor);
	}
	public IDs getFriendsIDs(long userId, long cursor) throws TwitterException {
		IDs ids;
		try {
			Twitter twitter = getTwitterAccess(AccessType.friends).getTwitter();
			ids = _getFriendsIDs(twitter, userId, cursor);
		} finally {
			twitterAccessLock.unlock();
		}
		return ids;
	}
	
	public ResponseList<Status> _getTweets(Twitter twitter, long userId, int page) throws TwitterException {		
		return twitter.getUserTimeline(userId, new Paging(page));
	}
	public ResponseList<Status> getTweets(long userId, int page) throws TwitterException {
		ResponseList<Status> tweets;
		try {
			Twitter twitter = getTwitterAccess(AccessType.status).getTwitter();
			tweets = _getTweets(twitter, userId, page);
		} finally {
			twitterAccessLock.unlock();
		}
		return tweets;
	}
}