package jp.ac.iwatepu.twitter.model;

import jp.ac.iwatepu.sentic.parser.Sentiment;

public class TweetSentiment {
	Sentiment sentiment;
	long statusId;
	String topic;
	long userId;
	
	public Sentiment getSentiment() {
		return sentiment;
	}
	public void setSentiment(Sentiment sentiment) {
		this.sentiment = sentiment;
	}
	public long getStatusId() {
		return statusId;
	}
	public void setStatusId(long statusId) {
		this.statusId = statusId;
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
	public TweetSentiment() {
		super();
	}
	
}
