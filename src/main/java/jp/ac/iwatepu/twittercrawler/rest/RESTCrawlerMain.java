package jp.ac.iwatepu.twittercrawler.rest;


import java.util.Arrays;
import java.util.logging.Logger;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.TwitterManager;
import jp.ac.iwatepu.twitter.TwitterManager.AccessType;
import jp.ac.iwatepu.twitter.consumer.DBTwitterConsumer;
import jp.ac.iwatepu.twitter.model.DBUser;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class RESTCrawlerMain {
	private static final Logger log = Logger.getLogger(RESTCrawlerMain.class.getName());
	int maxFollowers = 100;
	int maxFriends = 100;
	int maxStatuses = 200;
	
	DBTwitterConsumer twitterConsumer = new DBTwitterConsumer();
	
	public static void main(String[] args) throws Exception {
		RESTCrawlerMain restCrawlerMain = new RESTCrawlerMain();
		restCrawlerMain.run();
	}
	
	public void run() throws Exception {		
		Thread followersThread = new Thread(new RunFollowers());
		Thread friendsThread = new Thread(new RunFriends());
		Thread statusesThread = new Thread(new RunStatus());
		
		followersThread.start();
		friendsThread.start();
		statusesThread.start();		
		
		followersThread.join();
		friendsThread.join();
		statusesThread.join();		
	}
		
	class RunFollowers implements Runnable {
		public void run() {
			try {
			for (User user : SQLConnector.getInstance().getUsers(AccessType.followers, 1)) {	
				try {
					long[] followerIds = new long[maxFollowers];
					int count = 0;
					
					IDs response;
					long cursor = -1;					 
					do {
						response = TwitterManager.getInstance().getFollowersIDs(user.getId(), cursor);
						for (long followerId : response.getIDs()) {
							if (count >= maxFollowers) {
								break;
							}
							followerIds[count++] = followerId;	
						}
						cursor = response.getNextCursor();
					} while (cursor != 0 && maxFollowers > count);

					if (count > 0) {
						twitterConsumer.insertFollowers(user.getId(), Arrays.copyOfRange(followerIds, 0, count));
					}
					SQLConnector.getInstance().updateUserID(user.getId(), AccessType.followers);

				} catch (TwitterException ex) {
					if (ex.getStatusCode() == 401 || ex.getStatusCode() == 404) {
						SQLConnector.getInstance().updateUserID(user.getId(), AccessType.followers);
						//log.warning("Not authorized: " + ex.toString()); 
					} else {
						log.severe("error: " + ex.toString());
						ex.printStackTrace();
					}
				} 
			}			
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			System.out.println("Follower extraction done");
		}		
	}
	
	class RunFriends implements Runnable {
		public void run() {
			try {			
			for (User user : SQLConnector.getInstance().getUsers(AccessType.friends, 1)) {	
				try {
					long[] friendsIds = new long[maxFriends];
					int count = 0;
					
					IDs response;
					long cursor = -1;					 
					do {
						response = TwitterManager.getInstance().getFriendsIDs(user.getId(), cursor);
						for (long friendId : response.getIDs()) {
							if (count >= maxFriends) {
								break;
							}
							friendsIds[count++] = friendId;	
						}
						cursor = response.getNextCursor();
					} while (cursor != 0 && maxFriends > count);

					if (count > 0) {
						twitterConsumer.insertFriends(user.getId(), Arrays.copyOfRange(friendsIds, 0, count));
					}
					SQLConnector.getInstance().updateUserID(user.getId(), AccessType.friends);

				} catch (TwitterException ex) {
					if (ex.getStatusCode() == 401 || ex.getStatusCode() == 404) {
						SQLConnector.getInstance().updateUserID(user.getId(), AccessType.friends);
						//log.warning("Not authorized: " + ex.toString()); 
					} else {
						log.severe("error: " + ex.toString());
						ex.printStackTrace();
					}
				} 
			}			
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			System.out.println("Friend extraction done");
		}
	}
	
	class RunStatus implements Runnable {
		public void run() {
			try {			
			for (User user : SQLConnector.getInstance().getUsers(AccessType.status, -1)) {
				try {
					Status[] statuses= new Status[maxStatuses];
					int count = 0;					
					
					ResponseList<Status> response;
					int cursor = 1;
					do {
						response = TwitterManager.getInstance().getTweets(user.getId(), cursor);
						for (Status status : response) {
							if (count >= maxStatuses) {
								break;
							}
							statuses[count++] = status;
						}
						cursor++;
					} while (cursor < 6);
					
					if (count > 0 && user.getScreenName() == null) { //this user hasn't been collected yet
						SQLConnector.getInstance().updateUserFields(statuses[0].getUser(), 1);
					}
					for (Status status : statuses) {
						try {
							if (status == null) {
								break;
							}
							twitterConsumer.insertStatus(status, false);
						} catch (Exception e) {
							//ignore exceptions (we already have the tweet)
						}
					}
					SQLConnector.getInstance().updateUserID(user.getId(), AccessType.status);
				} catch (TwitterException ex) {
					if (ex.getStatusCode() == 401 || ex.getStatusCode() == 404) {
						SQLConnector.getInstance().updateUserID(user.getId(), AccessType.status);
						//log.warning("Not authorized: " + ex.toString()); 
					} else {
						log.severe("error: " + ex.toString());
						ex.printStackTrace();
					}
				}
			}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			System.out.println("Status extraction done");
		}
	}
}
