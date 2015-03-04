package jp.ac.iwatepu.sentic.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.DBStatus;
import jp.ac.iwatepu.twitter.model.DBUser;
import jp.ac.iwatepu.twitter.model.Reply;
import jp.ac.iwatepu.twitter.model.Retweet;
import jp.ac.iwatepu.twitter.model.TwitterUserNode;
import jp.ac.iwatepu.twitter.model.UserSentiment;
import twitter4j.User;

public class SentimentTraining {
	TwitterUserNode [] nodes;
	HashMap<Long, TwitterUserNode> idMapping = new HashMap<Long, TwitterUserNode>();
	HashMap<Long, List<DBStatus>> idMappingStatus = new HashMap<Long, List<DBStatus>>();

	public static void main(String[] args) throws Exception {
		SentimentTraining st = new SentimentTraining();
		st.run();
	}
	
	long testedUsers = 0;
	
	private void init() throws Exception {
		DBUser[] users = SQLConnector.getInstance().getUsers(null, -1);
		nodes = new TwitterUserNode[users.length];		
		for (DBUser dbUser : users) {	
			if (dbUser.getRecursionLevel() <= 1) {
				testedUsers++;
			}
		}
		System.out.println("User count: " + users.length + ", tested users: " + testedUsers);
		long[] allFollowers = SQLConnector.getInstance().getAllFollowers();
		System.out.println("Followers count: " + allFollowers.length / 2);
		long[] allFriends = SQLConnector.getInstance().getAllFriends();
		System.out.println("Friends count: " + allFriends.length / 2);
		
		for (int i = 0; i < users.length; i++) {
			DBUser user = users[i];
			TwitterUserNode node = new TwitterUserNode(user.getId(), user.getScreenName(), user.getDescription(), 
					user.getFriendsCount(), user.getFollowersCount(), user.getRecursionLevel());
			nodes[i] = node;
			idMapping.put(node.getId(), node);
			idMappingStatus.put(node.getId(), new LinkedList<DBStatus>());
		}
		UserSentiment[] userSents = SQLConnector.getInstance().getUserSentiments();
		for (UserSentiment us : userSents) {
			idMapping.get(us.getUserId()).setSent(us.getSentiment());
		}
		
		HashMap<Long, List<Long>> followersMapping = new HashMap<Long, List<Long>>();
		HashMap<Long, List<Long>> friendsMapping = new HashMap<Long, List<Long>>();
		
		for (int i = 0; i < allFollowers.length; i+=2) {
			long userId = allFollowers[i];
			long follower = allFollowers[i+1];
			
			if (!followersMapping.containsKey(userId)) {
				followersMapping.put(userId, new ArrayList<Long>());
			}
			followersMapping.get(userId).add(follower);
		}
		
		for (Entry<Long, List<Long>> entry : followersMapping.entrySet()) {
			long userId = entry.getKey();
			List<Long> followers = entry.getValue();		
			idMapping.get(userId).setFollowers(followers);
		}
		for (int i = 0; i < allFriends.length; i+=2) {
			long userId = allFriends[i];
			long friend = allFriends[i+1];
			
			if (!friendsMapping.containsKey(userId)) {
				friendsMapping.put(userId, new ArrayList<Long>());
			}
			friendsMapping.get(userId).add(friend);
		}
		for (Entry<Long, List<Long>> entry : friendsMapping.entrySet()) {
			idMapping.get(entry.getKey()).setFriends(entry.getValue());
		}
		
		List<Reply> replies = SQLConnector.getInstance().getAllReplies();
		for (Reply reply: replies) {
			idMapping.get(reply.getUserId()).getReplies().add(reply);
		}
		
		List<Retweet> retweets = SQLConnector.getInstance().getAllRetweets();
		for (Retweet retweet: retweets) {
			TwitterUserNode tun = idMapping.get(retweet.getRetweetedStatusIdUserId());
			if (tun != null) {
				tun.getRetweets().add(retweet);
			}
		}
		
		DBStatus[] statuses = SQLConnector.getInstance().getTweets();
		for (DBStatus status : statuses) {
			idMappingStatus.get(status.getUserId()).add(status);
		}
		
		System.out.println("Populated graph.");		
	}

	private double calculateConnectivity(TwitterUserNode n1, long id2) {
		TwitterUserNode n2 = null;
		if (idMapping.containsKey(id2)) {
			n2 = idMapping.get(id2);
		}
		
		int repliedCount = 0;
		for (Reply reply : n1.getReplies()) {
			if (reply.getReplyToUserId() == id2) {
				repliedCount++;
			}
		}
		
		int retweetCount = 0;
		if (n2 != null) {			
			for (Retweet retweet : n2.getRetweets()) {
				if (retweet.getUserId() == n1.getId()) {
					retweetCount++;
				}
			}
		}
		
		boolean follower = n1.getFollowers().contains(id2);
		boolean friend = n1.getFriends().contains(id2);
		double followerCount = n1.getFollowersCount();
		double friendCount = n1.getFollowersCount();
		
		//System.out.println(Math.log(1 + 100 * followerCount / maxFollowerCount));
		double fo = (follower?1:0) / Math.log(1 + 100 * followerCount / maxFollowerCount);
		double fr = (friend?1:0) / Math.log(1 + 100 * friendCount / maxFriendCount);
		double re = repliedCount / maxReplyCount;
		double rt = retweetCount / maxRetweetCount;
		//return (fo + fr + re + rt) / 4.0;
		
		return ((follower?1:0) + (friend?1:0) + (repliedCount>0?1:0) + (retweetCount>0?1:0)) / 4.0; 
	}
	
	double maxFollowerCount = 0;
	double maxFriendCount = 0;
	double maxReplyCount = 0;
	double maxRetweetCount = 0;
	int NUM_ITERS = 100;
	
	public void run() throws Exception {
		init();
		for (TwitterUserNode node : nodes) {
			maxFollowerCount = Math.max(maxFollowerCount, node.getFollowersCount());
			maxFriendCount = Math.max(maxFriendCount, node.getFriendsCount());
			maxReplyCount = Math.max(maxReplyCount, node.getReplies().size());
			maxRetweetCount = Math.max(maxRetweetCount, node.getRetweets().size());
		}
		long noFollowersCount = 0;
		for (TwitterUserNode node : nodes) {
			if (node.getFollowers().isEmpty() && node.getRecursionLevel() <= 1) {
				noFollowersCount++;
			}
		/*	
		   for (long id2 : node.getFollowers()) {
				double conn = calculateConnectivity(node, id2);
				if (conn != 0) {
					System.out.println("connectivity: " + conn);
				}
			}
			
			*/
		}
		System.out.println("Nodes without followers: " + noFollowersCount + "/" + testedUsers);
		HashMap<Long, Sentiment> sents = new HashMap<Long, Sentiment>();
		HashMap<Long, Sentiment> initialSents = new HashMap<Long, Sentiment>();
		for (TwitterUserNode node : nodes) {
			sents.put(node.getId(), node.getSent());
			initialSents.put(node.getId(), node.getSent());
		}		
		for (int iter = 0; iter < NUM_ITERS; iter++) {
			System.out.println("ITERATION: " + (iter+1) + "/" + NUM_ITERS);
			HashMap<Long, Sentiment> newSents = new HashMap<Long, Sentiment>();
			for (TwitterUserNode node : nodes) {
				newSents.put(node.getId(), sents.get(node.getId()));
			}
			for (TwitterUserNode node : nodes) {
				double totalConn = 0;
				Sentiment s1 = sents.get(node.getId());
				Sentiment newSent = new Sentiment();
				/*if (node.getFollowers().isEmpty()) {
					continue;
				}*/
				for (long id2 : node.getFollowers()) {
					/*if (n2 == node) {
						continue;
					}
					long id2 = n2.getId();
					*/
					double conn = calculateConnectivity(node, id2);		
					Sentiment s2 = sents.get(id2);
					if (s2 == null) {
						continue;
					}
					newSent.aptitude += s2.aptitude * conn;
					newSent.attention += s2.attention * conn;
					newSent.pleasantness += s2.pleasantness * conn;
					newSent.sensitivity += s2.sensitivity * conn;
					newSent.polarity += s2.polarity * conn;
					totalConn += conn;
				}
				if (totalConn != 0) {
					newSent.aptitude = newSent.aptitude / totalConn;
					newSent.attention = newSent.attention / totalConn;
					newSent.pleasantness = newSent.pleasantness / totalConn;
					newSent.sensitivity = newSent.sensitivity / totalConn;
					newSent.polarity = newSent.polarity / totalConn;
				}
				
				if (s1 != null) {
					newSent.aptitude += s1.aptitude;
					newSent.attention += s1.attention;
					newSent.pleasantness += s1.pleasantness;
					newSent.sensitivity += s1.sensitivity;
					newSent.polarity += s1.polarity;
					
					newSent.polarity = Math.max(newSent.polarity, -1); newSent.polarity = Math.min(newSent.polarity, 1);
					//System.out.println(newSent.polarity + " " + s1.polarity);
				}
				newSents.put(node.getId(), newSent);
			}
			double totalDifference = 0;
			double totalDifferenceInitial = 0;
			int changed = 0;
			int numberNull = 0;
			int diffPolarity = 0;
			int diffPolarityInitial = 0;
			int positivePolarityChangeCount = 0;
			for (TwitterUserNode node : nodes) {
				Sentiment s1 = sents.get(node.getId());
				Sentiment s2 = newSents.get(node.getId());
				Sentiment s3 = initialSents.get(node.getId());
				if (s1 == null && s2 == null) {	
					numberNull++;
					continue;
				}
				if (s1 == null) {
					numberNull++;
					//continue;
					totalDifference += s2.polarity;
				} else {
					if (s1.polarity * s2.polarity < 0) {
						diffPolarity++;
					}
					totalDifference += Math.abs(s1.polarity - s2.polarity);
				}
				if (s3 == null) {
					totalDifferenceInitial += s2.polarity;
				} else {
					if (s3.polarity * s2.polarity < 0) {
						if (node.getRecursionLevel() <= 1) {
							diffPolarityInitial++;
							if (s2.polarity > 0) {
								positivePolarityChangeCount++;
							}
							/*
							if (iter == NUM_ITERS - 1) {
								System.out.println("Old polarity: " + s3.polarity + ", new polarity: " + s2.polarity);
								for (DBStatus status : idMappingStatus.get(node.getId())) {
									if (status.getText().toLowerCase().contains("obama")) {
										System.out.println(status.getText());
									}
								}
							}
							*/
						}
					}
					totalDifferenceInitial += Math.abs(s3.polarity - s2.polarity);
				}
				changed++;
				sents.put(node.getId(), newSents.get(node.getId()));				
			}
			System.out.println("Total diff:" + totalDifference + ", average:" + 
					totalDifference / nodes.length + ", average changed:" + totalDifference / changed +
					", number null: " + numberNull + ", diffPolarity: " + diffPolarity + ", diffPolarityInitial: " + diffPolarityInitial + 
					", polarityChange: " + positivePolarityChangeCount + "/" + (diffPolarityInitial - positivePolarityChangeCount));
			System.out.println("Total initial diff:" + totalDifferenceInitial + ", average:" + 
					totalDifferenceInitial / nodes.length + ", average changed:" + totalDifferenceInitial / changed);
		}
		System.out.println("DONE");
	}
}
