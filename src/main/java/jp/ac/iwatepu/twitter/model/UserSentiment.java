package jp.ac.iwatepu.twitter.model;

import jp.ac.iwatepu.sentic.parser.Sentiment;

public class UserSentiment {
	Sentiment sentiment;
	String topic;
	long userId;
	long numTweets;
	
	public long getNumTweets() {
		return numTweets;
	}
	public void setNumTweets(long numTweets) {
		this.numTweets = numTweets;
	}
	public Sentiment getSentiment() {
		return sentiment;
	}
	public void setSentiment(Sentiment sentiment) {
		this.sentiment = sentiment;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public UserSentiment() {
		super();
	}
	
}
