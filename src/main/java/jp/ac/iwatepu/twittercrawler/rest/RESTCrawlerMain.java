package jp.ac.iwatepu.twittercrawler.rest;


import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import jp.ac.iwatepu.twitter.TwitterManager;
import jp.ac.iwatepu.twitter.TwitterManager.AccessType;
import jp.ac.iwatepu.twittercrawler.stream.SQLConnector;
import twitter4j.IDs;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class RESTCrawlerMain {
	private static final Logger log = Logger.getLogger(RESTCrawlerMain.class.getName());
	
	public static void main(String[] args) throws Exception {
		RESTCrawlerMain restCrawlerMain = new RESTCrawlerMain();
		restCrawlerMain.run();
	}
	
	String timestamp;
	
	public void run() throws Exception {
		timestamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
		
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
			String outputFileName = "followers-" + timestamp  + ".json";
			System.out.println("output file: " + outputFileName);
			File outputFile = new File(outputFileName);

			PrintWriter pw = new PrintWriter(outputFile);

			int flushAmount = 10;
			int downloaded = 0;

			for (Long id : SQLConnector.getInstance().getTwitterIDs(AccessType.followers)) {	
				try {
					JSONObject json = new JSONObject();
					json.append("user_id", id);

					JSONArray followersJSON = new JSONArray();

					IDs followerIDs;
					long cursor = -1;			
					do {
						followerIDs = TwitterManager.getInstance().getFollowersIDs(id, cursor);
						for (long followerID : followerIDs.getIDs()) {
							followersJSON.put(followerID);
						}
						cursor = followerIDs.getNextCursor();
					} while (cursor != 0);

					json.append("followers", followersJSON);

					pw.write(json.toString());
					SQLConnector.getInstance().updateUserID(id, AccessType.followers);

					downloaded++;
					if (downloaded % flushAmount == 0) {
						System.out.println("Downloaded followers: " + downloaded);
						pw.flush();  
					}
				} catch (TwitterException ex) {
					if (ex.getStatusCode() == 401) {
						log.warning("Not authorized: " + ex.toString()); 
					} else {
						log.severe("error: " + ex.toString());
						ex.printStackTrace();
					}
				} 
			}
			pw.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	class RunFriends implements Runnable {
		public void run() {
			try {
			String outputFileName = "friends-" + timestamp  + ".json";
			System.out.println("output file: " + outputFileName);
			File outputFile = new File(outputFileName);

			PrintWriter pw = new PrintWriter(outputFile);

			int flushAmount = 10;
			int downloaded = 0;

			for (Long id : SQLConnector.getInstance().getTwitterIDs(AccessType.friends)) {
				try {
					JSONObject json = new JSONObject();
					json.append("user_id", id);

					JSONArray friendsJSON = new JSONArray();

					IDs friendIDs;
					long cursor = -1;			
					do {
						friendIDs = TwitterManager.getInstance().getFriendsIDs(id, cursor);
						for (long friendID : friendIDs.getIDs()) {
							friendsJSON.put(friendID);
						}
						cursor = friendIDs.getNextCursor();
					} while (cursor != 0);

					json.append("friends", friendsJSON);

					pw.write(json.toString());
					SQLConnector.getInstance().updateUserID(id, AccessType.friends);

					downloaded++;
					if (downloaded % flushAmount == 0) {
						System.out.println("Downloaded friends: " + downloaded);
						pw.flush();  
					}
				} catch (TwitterException ex) {
					if (ex.getErrorCode() == 401) {
						log.warning("Not authorized: " + ex.toString()); 
					} else {
						log.severe("error: " + ex.toString());
						ex.printStackTrace();
					}
				}
			}
			pw.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	class RunStatus implements Runnable {
		public void run() {
			try {
			String outputFileName = "status-" + timestamp  + ".json";
			System.out.println("output file: " + outputFileName);
			File outputFile = new File(outputFileName);

			PrintWriter pw = new PrintWriter(outputFile);

			int flushAmount = 10;
			int downloaded = 0;

			for (Long id : SQLConnector.getInstance().getTwitterIDs(AccessType.status)) {
				try {
					JSONObject json = new JSONObject();
					json.append("user_id", id);
								
					JSONArray statusJSON = new JSONArray();
					
					ResponseList<Status> statuses;
					long cursor = -1;			
					do {
						statuses = TwitterManager.getInstance().getTweets(id);
						for (Status friendID : statuses) {
							statusJSON.put(friendID);
						}
						//cursor = statusJSON.getNextCursor();
					} while (false);
					
					json.append("status", statusJSON);
					
					pw.write(json.toString());
	
					SQLConnector.getInstance().updateUserID(id, AccessType.status);
					downloaded++;
					if (downloaded % flushAmount == 0) {
						System.out.println("Downloaded statuses: " + downloaded);
						pw.flush();  
					}
				} catch (TwitterException ex) {
					if (ex.getStatusCode() == 401) {
						log.warning("Not authorized: " + ex.toString()); 
					} else {
						log.severe("error: " + ex.toString());
						ex.printStackTrace();
					}
				}
			}
			pw.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
