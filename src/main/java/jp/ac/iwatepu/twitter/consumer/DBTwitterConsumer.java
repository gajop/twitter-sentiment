package jp.ac.iwatepu.twitter.consumer;

import java.sql.SQLException;

import jp.ac.iwatepu.db.SQLConnector;
import twitter4j.Status;
import twitter4j.User;

public class DBTwitterConsumer implements TwitterConsumer {

	public void insertStatus(Status status, boolean isSeed) {
		//System.out.println(status);
		try {
			SQLConnector.getInstance().insertStatus(status, isSeed);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void insertUser(User user, int recursionLevel) {
		try {
			SQLConnector.getInstance().insertUser(user, recursionLevel);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	public void insertRetweet(long id, long followerId) {
		// TODO Auto-generated method stub
		
	}

	public void insertFollowers(long userId, long[] followers) {
		try {
			SQLConnector.getInstance().insertFollowers(userId, followers);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void insertFriends(long userId, long[] friends) {
		try {
			SQLConnector.getInstance().insertFriends(userId, friends);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
