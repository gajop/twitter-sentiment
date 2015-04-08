package jp.ac.iwatepu.sentic.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import jp.ac.iwatepu.util.Util;
import twitter4j.User;

public class SentimentTraining {
	TwitterUserNode [] nodes;
	HashMap<Long, TwitterUserNode> idMapping = new HashMap<Long, TwitterUserNode>();
	HashMap<Long, List<DBStatus>> idMappingStatus = new HashMap<Long, List<DBStatus>>();
	List<Label> labels = new LinkedList<Label>();
	
	public static void main(String[] args) throws Exception {
		SentimentTraining st = new SentimentTraining();
		st.run();
	}
	
	long testedUsers = 0;
	List<Topic> searchTopics = Util.getInstance().getSearchTopics();
	
	double ALFA = 0.25, BETA = 0.25, GAMMA = 0.25, DELTA = 0.25;
	double X = 0.5; double Y = 0.5;
	
	
	public void testSentiments(HashMap<String, HashMap<Long, Sentiment>> topicSents) {
		int errors = 0; int correct = 0;
		for (Label lbl : labels) {
			Topic topic = searchTopics.get(lbl.getTopicId());
			Sentiment s = topicSents.get(topic.getTopic()).get(lbl.getUserId());
			if (s == null) {
				System.err.println(lbl.getTopicId() + " " + lbl.getUserId());
				continue;	
			}
			if (s.polarity * lbl.vote < 0) {
				errors++; 
			} else {
				correct++;
			}
		}
		double accuracy = correct * 100.0 / (errors + correct);
		System.out.println("Accuracy: " + accuracy + "% , Errors: " + errors + " Correct: " + correct);
	}
	
	private void init() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File("labels.csv")));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			long id = Long.valueOf(parts[0]);
			for (int i = 1; i < parts.length; ++i) {
				String part = parts[i];
				Label lbl = new Label();
				lbl.setUserId(id);
				int topicId = Integer.valueOf(part.substring(part.length()-1, part.length()))  - 1;
				lbl.setTopicId(topicId);
				int vote = 0;
				if (parts.length > 1) {
					if (part.charAt(0) == '+') {
						vote = 1;
					} else {
						vote = -1;
					}
				}
				lbl.setVote(vote);
				labels.add(lbl);
			}
		}
		
		DBUser[] users = SQLConnector.getInstance().getUsers(null, -1);
		nodes = new TwitterUserNode[users.length];		
		for (DBUser dbUser : users) {	
			if (dbUser.getRecursionLevel() <= 1) {
				testedUsers++;
			}
		}		
		long[] allFollowers = SQLConnector.getInstance().getAllFollowers();
		long[] allFriends = SQLConnector.getInstance().getAllFriends();		
		
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
			idMapping.get(us.getUserId()).getSents().add(us);
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
			if (reply.getReplyToUserId() != -1) {
				idMapping.get(reply.getUserId()).getReplies().add(reply);
			}
		}
		
		List<Retweet> retweets = SQLConnector.getInstance().getAllRetweets();
		for (Retweet retweet: retweets) {
			TwitterUserNode tun = idMapping.get(retweet.getRetweetedStatusIdUserId());
			if (tun != null) {
				tun.getRetweets().add(retweet);
			}
		}
		
		for (TwitterUserNode node : nodes) {
			node.getRelated();
			//System.in.read();
		}
		
		/*DBStatus[] statuses = SQLConnector.getInstance().getTweets();
		for (DBStatus status : statuses) {
			idMappingStatus.get(status.getUserId()).add(status);
		}*/
		
		System.out.println("Populated graph.");
		System.out.println("User count: " + users.length + ", tested users: " + testedUsers);
		System.out.println("Followers count: " + allFollowers.length / 2);
		System.out.println("Friends count: " + allFriends.length / 2);
		//System.out.println("Statuses: " + statuses.length + ", replies: " + replies.size() + ", retweets: " + retweets.size());
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
		
		return (ALFA * (follower?1:0) + BETA * (friend?1:0) + GAMMA * (repliedCount>0?1:0) + DELTA * (retweetCount>0?1:0)) / (ALFA + BETA + GAMMA + DELTA); 
	}
	
	double maxFollowerCount = 0;
	double maxFriendCount = 0;
	double maxReplyCount = 0;
	double maxRetweetCount = 0;
	int NUM_ITERS = 10;
	
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
		HashMap<String, HashMap<Long, Sentiment>> topicSents = new HashMap<String, HashMap<Long, Sentiment>>();
		HashMap<String, HashMap<Long, Sentiment>> initialTopicSents = new HashMap<String, HashMap<Long, Sentiment>>();
		//HashMap<Long, Sentiment> sents = new HashMap<Long, Sentiment>();
		//HashMap<Long, Sentiment> initialSents = new HashMap<Long, Sentiment>();
		for (TwitterUserNode node : nodes) {
			for (UserSentiment us : node.getSents()) {
				String topic = us.getTopic();
				if (!(topicSents.containsKey(topic))) {
					topicSents.put(topic, new HashMap<Long, Sentiment>());
					initialTopicSents.put(topic, new HashMap<Long, Sentiment>());
				}
				topicSents.get(topic).put(node.getId(), us.getSentiment());
				initialTopicSents.get(topic).put(node.getId(), us.getSentiment());
				//sents.put(node.getId(), node.getSent());
				//initialSents.put(node.getId(), node.getSent());
			}
		}		
		
		testSentiments(initialTopicSents);
		for (int iter = 0; iter < NUM_ITERS; iter++) {
			
			// INITIALIZE NEW ITERATION
			System.out.println("ITERATION: " + (iter+1) + "/" + NUM_ITERS);
			HashMap<String, HashMap<Long, Sentiment>> newTopicSents = new HashMap<String, HashMap<Long, Sentiment>>();
			for (TwitterUserNode node : nodes) {
				for (UserSentiment us : node.getSents()) {
					String topic = us.getTopic();
					if (!(newTopicSents.containsKey(topic))) {
						newTopicSents.put(topic, new HashMap<Long, Sentiment>());
					}
					topicSents.get(topic).put(node.getId(), topicSents.get(topic).get(node.getId()));
				}
			}
			
			// CALCULATE CONNECTIVITY AND NEW SENTIMENTS			
			for (TwitterUserNode node : nodes) {
				for (Topic searchTopic : searchTopics) {
					String topic = searchTopic.getTopic();					
					double totalConn = 0;
					Sentiment s1 = topicSents.get(topic).get(node.getId());
					Sentiment newSent = new Sentiment();
					long totalWeight = 0;
										
					/*if (node.getFollowers().isEmpty()) {
						continue;
					}*/
					for (long id2 : node.getRelated()) {
						/*if (n2 == node) {
							continue;
						}
						long id2 = n2.getId();
						*/						
						double conn = calculateConnectivity(node, id2);						
						Sentiment s2 = topicSents.get(topic).get(id2);
						if (s2 == null) {
							continue;
						}
						long weight = 0;
						TwitterUserNode n2 = idMapping.get(id2);
						for (UserSentiment us2 : n2.getSents()) {
							if (us2.getTopic().equals(topic)) {
								weight = us2.getNumTweets();
							}
						}
						
						newSent.aptitude += s2.aptitude * conn * weight;
						newSent.attention += s2.attention * conn * weight;
						newSent.pleasantness += s2.pleasantness * conn * weight;
						newSent.sensitivity += s2.sensitivity * conn * weight;
						newSent.polarity += s2.polarity * conn * weight;
						totalConn += conn * weight;
						totalWeight += weight;
					}
					if (totalConn != 0) {
						newSent.aptitude = newSent.aptitude / totalConn;
						newSent.attention = newSent.attention / totalConn;
						newSent.pleasantness = newSent.pleasantness / totalConn;
						newSent.sensitivity = newSent.sensitivity / totalConn;
						newSent.polarity = newSent.polarity / totalConn;
					}
					
					if (s1 != null) {
						UserSentiment us = null;
						for (int i = 0; i < node.getSents().size(); i++) {
							if (node.getSents().get(i).getTopic().equals(topic)) {
								us = node.getSents().get(i);
								break;
							}
						}
						long numTweets = 1;
						if (us != null) {
							numTweets = us.getNumTweets();
						}
						double w1 = X * numTweets;
						double w2 = Y * totalConn;
						newSent.aptitude = (newSent.aptitude * w2 + w1 * s1.aptitude) / (w1 + w2);
						newSent.attention = (newSent.attention * w2 + w1 *  s1.attention) / (w1 + w2);
						newSent.pleasantness = (newSent.pleasantness * w2 + w1 * s1.pleasantness) / (w1 + w2);
						newSent.sensitivity = (newSent.sensitivity * w2 + w1 * s1.sensitivity) / (w1 + w2);
						newSent.polarity = (newSent.polarity * w2 + w1 * s1.polarity) / (w1 + w2);
						
						newSent.polarity = Math.max(newSent.polarity, -1); newSent.polarity = Math.min(newSent.polarity, 1);
						//System.out.println(newSent.polarity + " " + s1.polarity);
					}
					if (totalConn != 0 || s1 != null) {
						newTopicSents.get(topic).put(node.getId(), newSent);
					}
				}
			}
			
			// PRINT VALEUS AND UPDATE
			for (String topic : topicSents.keySet()) {
				double totalDifference = 0;
				double totalDifferenceInitial = 0;
				int changed = 0;
				int numberNull = 0;
				int diffPolarity = 0;
				int diffPolarityInitial = 0;
				int positivePolarityChangeCount = 0;
				int coldStartThingy = 0;
				for (TwitterUserNode node : nodes) {
					boolean checkMe = false;
					if (node.getRecursionLevel() <= 1) {
						for (Label lbl : labels) {
							if (lbl.userId == node.getId()) {
								checkMe = true;
								break;
							}
						}
					}
					
					Sentiment s1 = topicSents.get(topic).get(node.getId());
					Sentiment s2 = newTopicSents.get(topic).get(node.getId());
					Sentiment s3 = initialTopicSents.get(topic).get(node.getId());
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
						if (node.getRecursionLevel() <= 1) {
							coldStartThingy++;
						}
					} else if (checkMe) {
						if (s3.polarity * s2.polarity < 0) {
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
						totalDifferenceInitial += Math.abs(s3.polarity - s2.polarity);
					}
					changed++;
					topicSents.get(topic).put(node.getId(), newTopicSents.get(topic).get(node.getId()));				
				}
				long ignoredNum = nodes.length - testedUsers;
				System.out.println(topic + ": number null: " + (numberNull - ignoredNum)  + ", diffPolarity: " + diffPolarity + ", diffPolarityInitial: " + diffPolarityInitial + 
						", polarityChange: " + positivePolarityChangeCount + "/" + (diffPolarityInitial - positivePolarityChangeCount));
				System.out.println("Average changed:" + totalDifferenceInitial / changed);
				System.out.println("Cold start thingy: " + coldStartThingy);
			}
			testSentiments(topicSents);
		}
		System.out.println("INITIAL: ");
		testSentiments(initialTopicSents);
		System.out.println("FINAl: ");
		testSentiments(topicSents);
		System.out.println("DONE");
	}
}

class Label {
	long userId;
	int topicId;
	int vote; // 1, 0, -1
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getTopicId() {
		return topicId;
	}
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	public int getVote() {
		return vote;
	}
	public void setVote(int vote) {
		this.vote = vote;
	}
	
}