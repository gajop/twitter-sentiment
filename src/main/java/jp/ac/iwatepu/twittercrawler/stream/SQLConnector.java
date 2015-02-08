package jp.ac.iwatepu.twittercrawler.stream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import jp.ac.iwatepu.twitter.TwitterManager;
import twitter4j.Status;

public class SQLConnector {
	static SQLConnector instance = new SQLConnector();
	Connection conn;
	public static SQLConnector getInstance() {
		return instance;
	}
	
	private Connection getConnection() throws SQLException{
		if (conn == null || conn.isClosed()) {
			conn = DriverManager.getConnection("jdbc:postgresql:twitter-sentiment","postgres", "root");
		}
		return conn;
	}
	
	public void insertStatus(Status status) throws SQLException {		
		String insertTableSQL = "INSERT INTO twitter_status"
				+ "(tweet_id, handle, user_id, text, created_at) VALUES"
				+ "(?,?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);		
		preparedStatement.setLong(1, status.getId());
		preparedStatement.setString(2, status.getUser().getScreenName());
		preparedStatement.setLong(3, status.getUser().getId());		
		preparedStatement.setString(4, status.getText());
		preparedStatement.setDate(5, new java.sql.Date(status.getCreatedAt().getTime()));
		preparedStatement.execute();
	}
	
	public void insertUser(long id, String handle, String description, int recursionLevel) throws SQLException {		
		String insertTableSQL = "INSERT INTO twitter_user"
				+ "(id, handle, description, recursionLevel) VALUES"
				+ "(?,?,?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insertTableSQL);		
		preparedStatement.setLong(1, id);
		preparedStatement.setString(2, handle);
		preparedStatement.setString(3, description);		
		preparedStatement.setLong(4, recursionLevel);
		preparedStatement.execute();
	}
	
	public Long[] getTwitterIDs(TwitterManager.AccessType accessType) throws SQLException {
		Vector<Long> ids = new Vector<Long>();
		Statement statement = getConnection().createStatement();
		
		String filter = "";
		switch (accessType) {
			case followers: filter = " where NOT processedFollowers "; break;
			case friends:  filter = " where NOT processedFriends "; break;
			case status:  filter = " where NOT processedTweets "; break;
		}	
		
		ResultSet rs = statement.executeQuery("SELECT id from twitter_user" + filter);
		while (rs.next()) {
			ids.add(rs.getLong(1));
		}
		return ids.toArray(new Long[ids.size()]);
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
	
	public String[] getTweets() throws SQLException {
		Vector<String> tweets = new Vector<String>();
		Statement statement = getConnection().createStatement();
		
		ResultSet rs = statement.executeQuery("SELECT text from twitter_status");
		while (rs.next()) {
			tweets.add(rs.getString(1));
		}
		return tweets.toArray(new String[tweets.size()]);
	}
	
}
