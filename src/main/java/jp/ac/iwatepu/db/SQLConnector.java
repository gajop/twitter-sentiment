package jp.ac.iwatepu.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;

import jp.ac.iwatepu.sentic.parser.Sentiment;
import jp.ac.iwatepu.twitter.TwitterManager;
import jp.ac.iwatepu.twitter.model.DBStatus;
import jp.ac.iwatepu.twitter.model.DBUser;
import jp.ac.iwatepu.twitter.model.Reply;
import jp.ac.iwatepu.twitter.model.Retweet;
import jp.ac.iwatepu.twitter.model.TweetSentiment;
import jp.ac.iwatepu.twitter.model.UserSentiment;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class SQLConnector {
	static SQLConnector instance = new SQLConnector();
	Connection conn;
	public static SQLConnector getInstance() {
		return instance;
	}
	
	public Connection getConnection() throws SQLException{
		if (conn == null || conn.isClosed()) {
			conn = DriverManager.getConnection("jdbc:postgresql:twitter-sentiment","postgres", "root");
		}
		return conn;
	}
	
	public void insertStatus(Status status, boolean isSeed) throws SQLException {		
		String insertTableSQL = "INSERT INTO twitter_status"
				+ "(id, handle, user_id, text, created_at, isSeed, isTruncated, isPossiblySensitive, isRetweet, isRetweeted, favoriteCount,"
				+ "retweetCount, retweetedStatusId, retweetedStatusIdUserHandle, retweetedStatusIdUserId, inReplyToScreenName, inReplyToStatusId, inReplyToUserId, lang, lat, long) VALUES"
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);		
		preparedStatement.setLong(1, status.getId());
		preparedStatement.setString(2, status.getUser().getScreenName());
		preparedStatement.setLong(3, status.getUser().getId());		
		preparedStatement.setString(4, status.getText());
		preparedStatement.setDate(5, new java.sql.Date(status.getCreatedAt().getTime()));
		preparedStatement.setBoolean(6, isSeed);
		preparedStatement.setBoolean(7, status.isTruncated());
		preparedStatement.setBoolean(8, status.isPossiblySensitive());
		preparedStatement.setBoolean(9, status.isRetweet());
		preparedStatement.setBoolean(10, status.isRetweeted());
		preparedStatement.setInt(11, status.getFavoriteCount());
		preparedStatement.setInt(12, status.getRetweetCount());
		if (status.getRetweetedStatus() != null) {
			preparedStatement.setLong(13, status.getRetweetedStatus().getUser().getId());
			preparedStatement.setString(14, status.getRetweetedStatus().getUser().getScreenName());
			preparedStatement.setLong(15, status.getRetweetedStatus().getId());
		} else {
			preparedStatement.setNull(13, Types.BIGINT);
			preparedStatement.setNull(14, Types.VARCHAR);
			preparedStatement.setNull(15, Types.BIGINT);
		}
		preparedStatement.setString(16, status.getInReplyToScreenName());
		preparedStatement.setLong(17, status.getInReplyToStatusId());
		preparedStatement.setLong(18, status.getInReplyToUserId());
		preparedStatement.setString(19, status.getLang());
		if (status.getGeoLocation() != null) {
			preparedStatement.setDouble(20, status.getGeoLocation().getLatitude());
			preparedStatement.setDouble(21, status.getGeoLocation().getLongitude());
		} else {
			preparedStatement.setNull(20, Types.INTEGER);
			preparedStatement.setNull(21, Types.INTEGER);
		}
		preparedStatement.execute();
		
		insertTableSQL = "INSERT INTO twitter_status_hashtag"
				+ "(status_id, hashtag) VALUES"
				+ "(?,?)";
		preparedStatement = getConnection().prepareStatement(insertTableSQL);
		for (HashtagEntity hashtagEntity : status.getHashtagEntities()) {
			preparedStatement.setLong(1, status.getId());
			preparedStatement.setString(2, hashtagEntity.getText());
			preparedStatement.execute();
		}
		
		insertTableSQL = "INSERT INTO twitter_status_user_entity"
				+ "(status_id, user_id, handle, startIndex, endIndex) VALUES"
				+ "(?,?,?,?,?)";
		preparedStatement = getConnection().prepareStatement(insertTableSQL);
		for (UserMentionEntity userMentionEntity :  status.getUserMentionEntities()) {
			preparedStatement.setLong(1, status.getId());
			preparedStatement.setLong(2, userMentionEntity.getId());
			preparedStatement.setString(3, userMentionEntity.getScreenName());
			preparedStatement.setInt(4, userMentionEntity.getStart());
			preparedStatement.setInt(5, userMentionEntity.getEnd());
			preparedStatement.execute();
		}
		
		insertTableSQL = "INSERT INTO twitter_status_url_entity"
				+ "(status_id, url, displayUrl, expandedUrl, startIndex, endIndex) VALUES"
				+ "(?,?,?,?,?,?)";
		preparedStatement = getConnection().prepareStatement(insertTableSQL);
		for (URLEntity urlEntity : status.getURLEntities()) {
			preparedStatement.setLong(1, status.getId());
			preparedStatement.setString(2, urlEntity.getURL());
			preparedStatement.setString(3, urlEntity.getDisplayURL());
			preparedStatement.setString(4, urlEntity.getExpandedURL());
			preparedStatement.setInt(5, urlEntity.getStart());
			preparedStatement.setInt(6, urlEntity.getEnd());
			preparedStatement.execute();
		}
		
		insertTableSQL = "INSERT INTO twitter_status_symbol_entity"
				+ "(status_id, text, startIndex, endIndex) VALUES"
				+ "(?,?,?,?)";
		preparedStatement = getConnection().prepareStatement(insertTableSQL);
		for (SymbolEntity symbolEntity: status.getSymbolEntities()) {
			preparedStatement.setLong(1, status.getId());
			preparedStatement.setString(2, symbolEntity.getText());
			preparedStatement.setInt(3, symbolEntity.getStart());
			preparedStatement.setInt(4, symbolEntity.getEnd());
			preparedStatement.execute();
		}
	}
	
	public void insertUser(User user, int recursionLevel) throws SQLException {		
		String insertTableSQL = "INSERT INTO twitter_user"
				+ "(id, handle, description, followerCount, friendCount, recursionLevel) VALUES"
				+ "(?,?,?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);		
		preparedStatement.setLong(1, user.getId());
		preparedStatement.setString(2, user.getScreenName());
		preparedStatement.setString(3, user.getDescription());		
		preparedStatement.setLong(4, user.getFriendsCount());
		preparedStatement.setLong(5, user.getFollowersCount());
		preparedStatement.setLong(6, recursionLevel);
		preparedStatement.execute();
	}
	
	public void insertFollowers(long userId, long [] followers) throws SQLException {
		String insertTableSQL = "INSERT INTO twitter_followers"
				+ "(user_id, follower_id) VALUES"
				+ "(?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);
		
		String insertTableSQLUser = "INSERT INTO twitter_user"
				+ "(id) VALUES"
				+ "(?)";		
		
		PreparedStatement preparedStatementUser = getConnection().prepareStatement(insertTableSQLUser);
		for (long follower : followers) {			
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, follower);
			preparedStatement.addBatch();
							
			try {
				preparedStatementUser.setLong(1, follower);
				preparedStatementUser.execute();
			} catch (Exception ex) {
				// ignore : user already exists
			}
		}
		preparedStatement.executeBatch();
	}
	
	public void insertFriends(long userId, long [] friends) throws SQLException {
		String insertTableSQL = "INSERT INTO twitter_friends"
				+ "(user_id, friend_id) VALUES"
				+ "(?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);	
		
		String insertTableSQLUser = "INSERT INTO twitter_user"
				+ "(id) VALUES"
				+ "(?)";		
		
		PreparedStatement preparedStatementUser = getConnection().prepareStatement(insertTableSQLUser);
		for (long friend : friends) {			
			preparedStatement.setLong(1, userId);
			preparedStatement.setLong(2, friend);
			preparedStatement.addBatch();
			
			try {
				preparedStatementUser.setLong(1, friend);
				preparedStatementUser.execute();
			} catch (Exception ex) {
				// ignore : user already exists
			}
		}
		preparedStatement.executeBatch();
	}
	
	public DBUser[] getUsers(TwitterManager.AccessType accessType, int maxRecursionLevel) throws SQLException {
		Vector<DBUser> users = new Vector<DBUser>();
		Statement statement = getConnection().createStatement();
		
		String filter = "";
		if (accessType != null) {
			switch (accessType) {
				case followers: filter = " where NOT processedFollowers "; break;
				case friends:  filter = " where NOT processedFriends "; break;
				case status:  filter = " where NOT processedTweets "; break;
			}
			if (maxRecursionLevel >= 0) {
				 filter = filter + " and recursionlevel <= " + maxRecursionLevel; 
			}
		}
		
		ResultSet rs = statement.executeQuery("SELECT id, handle, description, friendCount, followerCount, recursionLevel from twitter_user" + filter);
		while (rs.next()) {
			long id = rs.getLong(1);
			String screenName = rs.getString(2);
			String description = rs.getString(3);
			int friendCount = rs.getInt(4);
			int followerCount = rs.getInt(5);
			int recursionLevel = rs.getInt(6);
			users.add(new DBUser(id, screenName, description, friendCount, followerCount, recursionLevel));
		}
		return users.toArray(new DBUser[users.size()]);
	}
	
	public List<Reply> getAllReplies() throws SQLException {
		Vector<Reply> replies = new Vector<Reply>();		
		Statement statement = getConnection().createStatement();		
		
		ResultSet rs = statement.executeQuery("SELECT id, user_id, handle, inReplyToStatusId, inReplyToUserId, inReplyToScreenName from twitter_status");
	
		while (rs.next()) {			
			long statusId = rs.getLong(1);
			long userId = rs.getLong(2);
			String handle = rs.getString(3);
			
			long replyToStatusId = rs.getLong(4);
			long replyToUserId = rs.getLong(5);
			String replyToScreenName = rs.getString(6);
			
			Reply reply = new Reply(statusId, userId, handle, replyToStatusId, replyToUserId, replyToScreenName);
			replies.add(reply);
		}
		return replies;
	}	
	
	public List<Retweet> getAllRetweets() throws SQLException {
		Vector<Retweet> retweets = new Vector<Retweet>();		
		Statement statement = getConnection().createStatement();		
		
		ResultSet rs = statement.executeQuery("SELECT id, user_id, handle, retweetedStatusId, retweetedStatusIdUserId, retweetedStatusIdUserHandle from twitter_status where isRetweet");
	
		while (rs.next()) {			
			long statusId = rs.getLong(1);
			long userId = rs.getLong(2);
			String handle = rs.getString(3);
			
			long retweetedStatusId = rs.getLong(4);
			long retweetedStatusIdUserId = rs.getLong(5);
			String retweetedStatusIdUserHandle = rs.getString(6);
			
			Retweet retweet = new Retweet(statusId, userId, handle, retweetedStatusId, retweetedStatusIdUserId, retweetedStatusIdUserHandle);
			retweets.add(retweet);
		}
		return retweets;
	}	
	
	public long[] getAllFollowers() throws SQLException {
		Vector<Long> followers = new Vector<Long>();		
		Statement statement = getConnection().createStatement();		
		
		ResultSet rs = statement.executeQuery("SELECT user_id, follower_id from twitter_followers");
	
		while (rs.next()) {
			long userId = rs.getLong(1);
			long follower = rs.getLong(2);
			followers.add(userId);
			followers.add(follower);
		}
		return ArrayUtils.toPrimitive(followers.toArray(new Long[followers.size()]));
	}
	
	public long[] getAllFriends() throws SQLException {
		Vector<Long> friends = new Vector<Long>();		
		Statement statement = getConnection().createStatement();		
		
		ResultSet rs = statement.executeQuery("SELECT user_id, friend_id from twitter_friends");
	
		while (rs.next()) {
			long userId = rs.getLong(1);
			long friend = rs.getLong(2);
			friends.add(userId);
			friends.add(friend);
		}
		return ArrayUtils.toPrimitive(friends.toArray(new Long[friends.size()]));
	}
	
	public long[] getFollowers(long userId) throws SQLException {
		Vector<Long> followers = new Vector<Long>();		
		Statement statement = getConnection().createStatement();		
		
		ResultSet rs = statement.executeQuery("SELECT follower_id from twitter_followers WHERE user_id = " + userId);
	
		while (rs.next()) {
			long follower = rs.getLong(1);
			followers.add(follower);
		}
		return ArrayUtils.toPrimitive(followers.toArray(new Long[followers.size()]));
	}
	
	public long[] getFriends(long userId) throws SQLException {
		Vector<Long> friends = new Vector<Long>();		
		Statement statement = getConnection().createStatement();		
		
		ResultSet rs = statement.executeQuery("SELECT friend_id from twitter_friends WHERE user_id = " + userId);
	
		while (rs.next()) {
			long friend = rs.getLong(1);
			friends.add(friend);
		}
		return ArrayUtils.toPrimitive(friends.toArray(new Long[friends.size()]));
	}
	
	public void updateUserID(long userId, TwitterManager.AccessType accessType) throws SQLException {
		Statement statement = getConnection().createStatement();
		
		String filter = "";
		switch (accessType) {
			case followers: filter = " SET processedFollowers=TRUE"; break;
			case friends:  filter = " SET processedFriends=TRUE"; break;
			case status:  filter = " SET processedTweets=TRUE"; break;
		}	
		
		String query = "UPDATE twitter_user " + filter + " WHERE id=" + userId + ";";
		statement.executeUpdate(query);
	}
	
	public void updateUserFields(User user, int recursionLevel) throws SQLException {
		String insertTableSQL = "UPDATE twitter_user "
				+ "SET handle=?, description=?, followerCount=?, friendCount=?, recursionLevel=? WHERE id = ?";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);		
		preparedStatement.setString(1, user.getScreenName());
		preparedStatement.setString(2, user.getDescription());		
		preparedStatement.setLong(3, user.getFriendsCount());
		preparedStatement.setLong(4, user.getFollowersCount());
		preparedStatement.setLong(5, recursionLevel);
		preparedStatement.setLong(6, user.getId());
		preparedStatement.execute();		
	}
	
	public DBStatus[] getTweets() throws SQLException {
		Vector<DBStatus> tweets = new Vector<DBStatus>();
		Statement statement = getConnection().createStatement();
		
		ResultSet rs = statement.executeQuery("SELECT id, text, user_id, isRetweet, isRetweeted, retweetedStatusId, retweetedStatusIdUserHandle, retweetedStatusIdUserId from twitter_status");
		while (rs.next()) {
			DBStatus status = new DBStatus(rs.getLong(1), rs.getString(2));
			status.setUserId(rs.getLong(3));
			status.setRetweet(rs.getBoolean(4));
			status.setRetweeted(rs.getBoolean(5));
			if (status.isRetweet()) {
				status.setRetweetedStatusId(rs.getLong(6));
				status.setRetweetedStatusIdUserHandle(rs.getString(7));
				status.setRetweetedStatusIdUserId(rs.getLong(8));				
			}
			
			tweets.add(status);
		}
		return tweets.toArray(new DBStatus[tweets.size()]);
	}
	
	public DBStatus[] getTweetsWithoutSentiment() throws SQLException {
		Vector<DBStatus> tweets = new Vector<DBStatus>();
		Statement statement = getConnection().createStatement();
		
		ResultSet rs = statement.executeQuery("SELECT twitter_status.id, twitter_status.text from twitter_status " + 
				"left outer join twitter_status_sentiment on twitter_status.id = twitter_status_sentiment.status_id where " + 
				"twitter_status_sentiment.status_id is NULL;");
		while (rs.next()) {
			tweets.add(new DBStatus(rs.getLong(1), rs.getString(2)));
		}
		return tweets.toArray(new DBStatus[tweets.size()]);
	}
	
	public TweetSentiment[] getTweetSentiments() throws SQLException {
		Vector<TweetSentiment> tweetSents = new Vector<TweetSentiment>();
		Statement statement = getConnection().createStatement();
		
		ResultSet rs = statement.executeQuery("SELECT status_id, topic, polarity, pleasantness, attention, sensitivity, aptitude, user_id from twitter_status_sentiment, twitter_status where id=status_id");
		while (rs.next()) {
			TweetSentiment ts = new TweetSentiment();
			ts.setStatusId(rs.getLong(1));
			ts.setTopic(rs.getString(2));
			ts.setSentiment(new Sentiment(rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7)));
			ts.setUserId(rs.getLong(8));
			tweetSents.add(ts);
		}
		return tweetSents.toArray(new TweetSentiment[tweetSents.size()]);
	}
	
	public UserSentiment[] getUserSentiments() throws SQLException {
		Vector<UserSentiment> userSents = new Vector<UserSentiment>();
		Statement statement = getConnection().createStatement();
		
		ResultSet rs = statement.executeQuery("SELECT user_id, topic, polarity, pleasantness, attention, sensitivity, aptitude, num_tweets from twitter_user_profile");
		while (rs.next()) {
			UserSentiment us = new UserSentiment();
			us.setUserId(rs.getLong(1));
			us.setTopic(rs.getString(2));
			us.setSentiment(new Sentiment(rs.getDouble(3), rs.getDouble(4), rs.getDouble(5), rs.getDouble(6), rs.getDouble(7)));
			
			userSents.add(us);
			us.setNumTweets(rs.getLong(8));
		}
		return userSents.toArray(new UserSentiment[userSents.size()]);
	}
	
	public void insertStatusSentiment(long statusId, Sentiment sentiment, String topic) throws SQLException {		
		String insertTableSQL = "INSERT INTO twitter_status_sentiment "
				+ "(status_id, topic, polarity, pleasantness, attention, sensitivity, aptitude) VALUES "
				+ "(?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);
		preparedStatement.setLong(1, statusId);
		preparedStatement.setString(2, topic);
		preparedStatement.setDouble(3, sentiment.getPolarity());
		preparedStatement.setDouble(4, sentiment.getPleasantness());
		preparedStatement.setDouble(5, sentiment.getAttention());
		preparedStatement.setDouble(6, sentiment.getSensitivity());
		preparedStatement.setDouble(7, sentiment.getAptitude());
		preparedStatement.execute();
	}
	public void insertUserSentiment(UserSentiment us) throws SQLException {		
		String insertTableSQL = "INSERT INTO twitter_user_profile "
				+ "(user_id, topic, polarity, pleasantness, attention, sensitivity, aptitude, num_tweets) VALUES "
				+ "(?,?,?,?,?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);
		preparedStatement.setLong(1, us.getUserId());
		preparedStatement.setString(2, us.getTopic());
		Sentiment sentiment = us.getSentiment();
		preparedStatement.setDouble(3, sentiment.getPolarity());
		preparedStatement.setDouble(4, sentiment.getPleasantness());
		preparedStatement.setDouble(5, sentiment.getAttention());
		preparedStatement.setDouble(6, sentiment.getSensitivity());
		preparedStatement.setDouble(7, sentiment.getAptitude());
		preparedStatement.setLong(8, us.getNumTweets());
		preparedStatement.execute();
	}

	public void insertRetweet(long userId, long retweetUserId, long statusId, long retweetedStatusId) throws SQLException {
		String insertTableSQL = "INSERT INTO twitter_retweets"
				+ "(user_id, retweet_user_id, status_id, retweet_status_id) VALUES"
				+ "(?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);	
				
		preparedStatement.setLong(1, userId);
		preparedStatement.setLong(2, retweetUserId);
		preparedStatement.setLong(3, statusId);
		preparedStatement.setLong(4, retweetedStatusId);
		preparedStatement.execute();
	}

	public void insertStatusSentimentCleanse(long statusId, String topic) throws SQLException {
		String insertTableSQL = "INSERT INTO twitter_status_sentiment_cleanse "
				+ "(status_id, topic) VALUES "
				+ "(?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);
		preparedStatement.setLong(1, statusId);
		preparedStatement.setString(2, topic);
		preparedStatement.execute();
	}
	
}
