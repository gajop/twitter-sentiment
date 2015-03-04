package jp.ac.iwatepu.twitter.model;

import java.util.ArrayList;
import java.util.List;

import jp.ac.iwatepu.sentic.parser.Sentiment;

public class TwitterUserNode extends DBUser {
	List<Long> followers = new ArrayList<Long>();
	List<Long> friends = new ArrayList<Long>();
	List<Retweet> retweets = new ArrayList<Retweet>();
	List<Reply> replies = new ArrayList<Reply>();
	Sentiment sent;
	
	public TwitterUserNode(long id, String screenName, String description,
			int friendCount, int followerCount, int recursionLevel) {
		super(id, screenName, description, friendCount, followerCount, recursionLevel);
	}

	public List<Long> getFollowers() {
		return followers;
	}

	public void setFollowers(List<Long> followers) {
		this.followers = followers;
	}

	public List<Long> getFriends() {
		return friends;
	}

	public void setFriends(List<Long> friends) {
		this.friends = friends;
	}
	
	public List<Retweet> getRetweets() {
		return retweets;
	}

	public void setRetweets(List<Retweet> retweets) {
		this.retweets = retweets;
	}

	public List<Reply> getReplies() {
		return replies;
	}

	public void setReplies(List<Reply> replies) {
		this.replies = replies;
	}

	public Sentiment getSent() {
		return sent;
	}

	public void setSent(Sentiment sent) {
		this.sent = sent;
	}
	
	
}
