package jp.ac.iwatepu.sentic.parser;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.DBStatus;
import jp.ac.iwatepu.twitter.model.DBUser;
import jp.ac.iwatepu.twitter.model.TwitterUserNode;
import jp.ac.iwatepu.twitter.model.UserSentiment;

public class ManualClassifier {
	public static void main(String [] args) throws Exception {
		new ManualClassifier().run();
	}
	
	HashMap<Long, TwitterUserNode> idMapping = new HashMap<Long, TwitterUserNode>();
	private TwitterUserNode[] nodes;
	private int testedUsers;
	HashMap<Long, List<DBStatus>> idMappingStatus = new HashMap<Long, List<DBStatus>>();
	int TOTAL_USERS = 100;
	
	public void run() throws Exception {
		System.out.println("STARTING MANUAL CLASSIFICATION");
		List<Topic> searchTopics = new LinkedList<Topic>();
		searchTopics.add(new Topic("Obama, GOP", Arrays.asList(new String[] {"obama"}), Arrays.asList(new String[] {"gop"}),
				Arrays.asList(new String[] {"UniteBlue", "p2", "ObamaLovesAmerica", "SOTU", "ILoveObama"}),
				Arrays.asList(new String[] {"tcot", "pjnet", "ccot", "teaparty", "RedNationRising"})
		));
		searchTopics.add(new Topic("Net Neutrality", Arrays.asList(new String[] {"net neutrality", "netneutrality"}), Arrays.asList(new String[] {}),
				Arrays.asList(new String[] {"SaveOurNet", "NetNeutrality"}),
				Arrays.asList(new String[] {"NoNetNeutrality"})
		));
		searchTopics.add(new Topic("Gun control", Arrays.asList(new String[] {"gun control"}), Arrays.asList(new String[] {}),
				Arrays.asList(new String[] {}),
				Arrays.asList(new String[] {"2A"})
		));
		searchTopics.add(new Topic("Obama care", Arrays.asList(new String[] {"obama care", "obamacare"}), Arrays.asList(new String[] {}),
				Arrays.asList(new String[] {}),
				Arrays.asList(new String[] {})
		));

		DBUser[] users = SQLConnector.getInstance().getUsers(null, -1);
		nodes = new TwitterUserNode[users.length];		
		for (DBUser dbUser : users) {	
			if (dbUser.getRecursionLevel() <= 1) {
				testedUsers++;
			}
		}		

		long[] allFollowers = SQLConnector.getInstance().getAllFollowers();
		long[] allFriends = SQLConnector.getInstance().getAllFriends();		
		
		LinkedList<Integer> allIndexes = new LinkedList<Integer>();
		for (int i = 0; i < users.length; i++) {
			DBUser user = users[i];
			TwitterUserNode node = new TwitterUserNode(user.getId(), user.getScreenName(), user.getDescription(), 
					user.getFriendsCount(), user.getFollowersCount(), user.getRecursionLevel());
			nodes[i] = node;
			idMapping.put(node.getId(), node);
			idMappingStatus.put(node.getId(), new LinkedList<DBStatus>());
			if (user.getRecursionLevel() <= 1) {
				allIndexes.add(i);
			}
		}
		UserSentiment[] userSents = SQLConnector.getInstance().getUserSentiments();
		for (UserSentiment us : userSents) {
			idMapping.get(us.getUserId()).getSents().add(us);
		}
		
		long seed = System.nanoTime();
		Collections.shuffle(allIndexes, new Random(seed));
		LinkedList<Long> testUserIds = new LinkedList<Long>();
		//HashMap<String, Integer> topicsCount = new HashMap<String, Integer>();
		
		DBStatus[] statuses = SQLConnector.getInstance().getTweets();
		for (DBStatus status : statuses) {
			idMappingStatus.get(status.getUserId()).add(status);
		}
		
		PrintWriter pw = new PrintWriter(new File("labels.csv"));
		for (int ind : allIndexes) {
			TwitterUserNode node = nodes[ind];
			if (node.getSents().isEmpty()) {
				continue;
			}
			testUserIds.add(node.getId());
			pw.write("" + node.getId());
			for (UserSentiment us: node.getSents()) {
				String topic = us.getTopic();
				for (int i = 0; i < searchTopics.size(); i++) {
					Topic searchTopic = searchTopics.get(i);
					if (topic.equals(searchTopic.getTopic())) {
						pw.write("," + (i+1));
						break;
					}
				}
			}
			pw.write("\n");
			if (testUserIds.size() >= TOTAL_USERS) {
				break;
			}
		}
		pw.flush();
		pw.close();
		
		System.out.println("TEST USERS");
		for (long ind : testUserIds) {
			TwitterUserNode node = idMapping.get(ind);
			
			for (DBStatus status: idMappingStatus.get(node.getId())) {
				String text = status.getText();
				String lowercase = text.toLowerCase();
				List<String> hashtags = extractHashtags(text);
				
				boolean anyFound = false;
				for (int i = 0; i < searchTopics.size(); i++) {
					Topic searchTopic = searchTopics.get(i);
					boolean found = false;
					for (String topicSearch : searchTopic.getTopicSearch()) {
						if (lowercase.contains(topicSearch.toLowerCase())) {
							found = true;
							break;
						}
					}
					for (String topicSearch : searchTopic.getNegativeTopicSearch()) {
						if (lowercase.contains(topicSearch.toLowerCase())) {
							found = true;			
							break;
						}
					}	
					for (String hashtag : hashtags) {					
						for (String posSearchHashTag : searchTopic.getSentimentHashtags()) {
							if (hashtag.toLowerCase().equals(posSearchHashTag.toLowerCase())) {
								found = true;
								break;
							}
						}
						for (String negSearchHashTag : searchTopic.getNegativeSentimentHashtags()) {
							if (hashtag.toLowerCase().equals(negSearchHashTag.toLowerCase())) {
								found = true;								
								break;
							}
						}
					}
					if (found) {
						anyFound = true;
						System.out.print("[" + searchTopic.getTopic() + "]");
					}
				}
				if (!anyFound) {
					continue;
				}
				
				System.out.println(status.getText());
			}
			System.out.println();
			System.out.println("User: " + node.getScreenName() + ", id: " + node.getId());
			System.out.println("INPUT: ");
			System.in.read();
			System.out.println("=========");
			System.out.println();
		}
		System.out.println("DONE");
	}

	private List<String> extractHashtags(String text) {
		List<String> allMatches = new ArrayList<String>();
		Matcher m = Pattern.compile("#[A-Za-z0-9]+").matcher(text);
		while (m.find()) {
			allMatches.add(m.group().substring(1));
		}
		return allMatches;
	}
}
