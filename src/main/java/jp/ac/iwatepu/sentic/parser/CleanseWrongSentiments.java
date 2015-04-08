package jp.ac.iwatepu.sentic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.DBStatus;

public class CleanseWrongSentiments {
	public static void main(String[] args) throws Exception {
		CleanseWrongSentiments cws = new CleanseWrongSentiments();
		cws.run();
	}
	
	private List<String> extractHashtags(String text) {
		List<String> allMatches = new ArrayList<String>();
		Matcher m = Pattern.compile("#[A-Za-z0-9]+").matcher(text);
		while (m.find()) {
			allMatches.add(m.group().substring(1));
		}
		return allMatches;
	}

	public void run() throws Exception {
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
		long [] foundCount = new long [] {0,0,0,0};
		long [] polarityChanged = new long [] {0,0,0,0};
		long [] mixedSearch = new long [] {0,0,0,0};
		long [] posSearch = new long [] {0,0,0,0};
		long [] negSearch = new long [] {0,0,0,0};
		
		long [] mixedHashtag = new long [] {0,0,0,0};
		long [] posHashtag = new long [] {0,0,0,0};
		long [] negHashtag = new long [] {0,0,0,0};
		
		long done = -1;
		DBStatus[] tweets = SQLConnector.getInstance().getTweets();
		for (DBStatus tweet: tweets) {
			try {
				if (done % 1000 == 0) {
					System.out.println("Progress: " + done + "/" + tweets.length);
				}
				done++;				
				if (tweet.isRetweet()) {
					//continue;
				}
				
				String text = tweet.getText();
				//System.out.println(text);
				String lowercase = text.toLowerCase();
				List<String> hashtags = extractHashtags(text);
				
				List<Integer> matchingTopics = new LinkedList<Integer>();
				boolean [] posFound = new boolean[searchTopics.size()];
				boolean [] negFound = new boolean[searchTopics.size()];
				boolean [] posHashtagFound = new boolean[searchTopics.size()];
				boolean [] negHashtagFound = new boolean[searchTopics.size()];
				for (int i = 0; i < searchTopics.size(); i++) {
					Topic searchTopic = searchTopics.get(i);
					boolean found = false;					
					for (String topicSearch : searchTopic.getTopicSearch()) {
						if (lowercase.contains(topicSearch.toLowerCase())) {
							posFound[i] = true;
							found = true;
							break;
						}
					}
					for (String topicSearch : searchTopic.getNegativeTopicSearch()) {
						if (lowercase.contains(topicSearch.toLowerCase())) {
							negFound[i] = true;
							found = true;			
							break;
						}
					}	
					for (String hashtag : hashtags) {					
						for (String posSearchHashTag : searchTopic.getSentimentHashtags()) {
							if (hashtag.toLowerCase().equals(posSearchHashTag.toLowerCase())) {
								posHashtagFound[i] = true;
								found = true;
								break;
							}
						}
						for (String negSearchHashTag : searchTopic.getNegativeSentimentHashtags()) {
							if (hashtag.toLowerCase().equals(negSearchHashTag.toLowerCase())) {
								negHashtagFound[i] = true;
								found = true;								
								break;
							}
						}
					}
					if (found) {						
						matchingTopics.add(i);
						foundCount[i]++;
						if (posFound[i] && negFound[i]) {
							mixedSearch[i]++;
						}
						if (posHashtagFound[i] && negHashtagFound[i]) {
							mixedHashtag[i]++;
						} else if (posHashtagFound[i]) {
							posHashtag[i]++;	
						} else if (negHashtagFound[i]) {
							negHashtag[i]++;
						}
						if (posFound[i] && negFound[i]) {
							mixedSearch[i]++;
						} else if (posFound[i]) {
							posSearch[i]++;	
						} else if (negFound[i]) {
							negSearch[i]++;
						}					
					}
				}
				if (matchingTopics.isEmpty()) {
					continue;
				}

				//System.out.println("Tweet Sentiment: " + sentiment);
				
				for (int i = 0; i < matchingTopics.size(); i++) {
					Topic topic = searchTopics.get(matchingTopics.get(i));
					SQLConnector.getInstance().insertStatusSentimentCleanse(tweet.getStatusId(), topic.getTopic());
				}

				//System.out.println("=========================");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (int i = 0; i < 4; i++) {
			Topic topic = searchTopics.get(i);
			System.out.println("Topic: " + topic.getTopic() + " matched: " + foundCount[i] + 
					", positive hashtags: " + posHashtag[i] + ", negative hashtags: " + negHashtag[i] + ", mixed hashtags: " + mixedHashtag[i]  + 
					", positive search: " + posSearch[i] + ", negative search: " + negSearch[i] + ", mixed search: " + mixedSearch[i] + ", polarity changed: " + polarityChanged[i]);			
		}
	}
}
