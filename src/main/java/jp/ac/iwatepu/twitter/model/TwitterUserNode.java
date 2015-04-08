package jp.ac.iwatepu.twitter.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jp.ac.iwatepu.sentic.parser.Sentiment;

public class TwitterUserNode extends DBUser {
	List<Long> followers = new ArrayList<Long>();
	List<Long> friends = new ArrayList<Long>();
	List<Retweet> retweets = new ArrayList<Retweet>();
	List<Reply> replies = new ArrayList<Reply>();
	List<UserSentiment> sents = new LinkedList<UserSentiment>();
	Set<Long> related;
	
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

	public List<UserSentiment> getSents() {
		return sents;
	}

	public void setSents(List<UserSentiment> sents) {
		this.sents = sents;
	}
	
	public Set<Long> getRelated() {
		if (related == null) {
			related = new HashSet<Long>();
			related.addAll(friends);
			related.addAll(followers);
//			System.out.println();
//			System.out.println("RETWEETS");
//			System.out.println();
			for (Retweet rt : retweets) {
				long userId = rt.getRetweetedStatusIdUserId();
//				System.out.println(userId + " " + id);
				related.add(userId);
			}
//			System.out.println();
//			System.out.println("REPLIES: ");
//			System.out.println();
			for (Reply re : replies) {
				long userId = re.getReplyToUserId();
//				System.out.println(userId + " " + id);
				related.add(userId);
			}
		}
		return related;
	}
	
}
