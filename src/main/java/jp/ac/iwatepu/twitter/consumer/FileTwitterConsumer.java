package jp.ac.iwatepu.twitter.consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.User;

public class FileTwitterConsumer implements TwitterConsumer {		
	int flushAmount = 100;
	int downloadedStatus = 0;
	private PrintWriter pwStatus;
	private PrintWriter pwFollowers;
	private PrintWriter pwFriends;
	
	public FileTwitterConsumer() {
		initOutputFile();
	}
	
	private void initOutputFile() {
		String timestamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());		
		
		String outputFileNameStatus = "stream-" + timestamp  + ".json";
		File outputFileStatus = new File(outputFileNameStatus);
		
		String outputFileNameFollowers = "followers-" + timestamp  + ".json";		
		File outputFileFollowers = new File(outputFileNameFollowers);		
		
		String outputFileNameFriends = "friends-" + timestamp  + ".json";		
		File outputFileFriends = new File(outputFileNameFriends);
		try {
			pwStatus = new PrintWriter(outputFileStatus);
			pwFollowers = new PrintWriter(outputFileFollowers);
			pwFriends = new PrintWriter(outputFileFriends);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} 
	}
	
	public void insertStatus(Status status, boolean isSeed) {		
		pwStatus.write(status.toString() + ", isSeed: " + isSeed);
		downloadedStatus++;
		if (downloadedStatus % flushAmount == 0) {
			pwStatus.flush();  
		}
	}

	public void insertUser(User user, int recursionLevel) {
		// TODO Auto-generated method stub

	}
	
	public void close() {
		pwStatus.close();
	}

	public void insertFollowers(long userId, long [] followers) {
		try {
			JSONObject json = new JSONObject();
			json.append("user_id", userId);
	
			JSONArray followersJSON = new JSONArray();
			for (long follower : followers) {
				followersJSON.put(follower);
			}
			json.append("followers", followersJSON);
	
			pwFollowers.write(json.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void insertFriends(long userId, long [] friends) {
		try {
			JSONObject json = new JSONObject();
			json.append("user_id", userId);
	
			JSONArray friendsJSON = new JSONArray();
			for (long friend : friends) {
				friendsJSON.put(friend);
			}
			json.append("friend", friendsJSON);
	
			pwFriends.write(json.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void insertRetweet(long id, long followerId) {
		// TODO Auto-generated method stub
		
	}

}
