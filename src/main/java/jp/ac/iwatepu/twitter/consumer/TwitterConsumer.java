package jp.ac.iwatepu.twitter.consumer;

import twitter4j.Status;
import twitter4j.User;

public interface TwitterConsumer {
	public void insertStatus(Status status, boolean isSeed);
	public void insertUser(User user, int recursionLevel);
	public void insertFollowers(long userId, long [] followers);
	public void insertFriends(long userId, long [] friends);
	//public void insertRetweets(long id, long followerId);
}
