package jp.ac.iwatepu.sentic.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.TweetSentiment;
import jp.ac.iwatepu.twitter.model.TwitterUserNode;
import jp.ac.iwatepu.twitter.model.UserSentiment;

public class UserAggregator {
	HashMap<Long, List<TweetSentiment>> idMapping = new HashMap<Long, List<TweetSentiment>>();
	TweetSentiment[] tweetSents;
	
	public static void main(String[] args) throws Exception {
		UserAggregator ua = new UserAggregator();
		ua.run();
	}
	
	private Collection<UserSentiment> avgSentiment(List<TweetSentiment> pSents) {
		if (!pSents.isEmpty()) {
			HashMap<String, UserSentiment> res = new HashMap<String, UserSentiment>();
			
			for (TweetSentiment ts: pSents) {
				if (!res.containsKey(ts.getTopic())) {
					UserSentiment us = new UserSentiment();
					us.setUserId(ts.getUserId());
					us.setSentiment(new Sentiment());
					us.setTopic(ts.getTopic());
					us.setNumTweets(0);
					res.put(ts.getTopic(), us);
				}
				UserSentiment us = res.get(ts.getTopic());
				Sentiment sent = us.getSentiment();
				us.setNumTweets(us.getNumTweets() + 1);
				
				Sentiment s = ts.getSentiment();
				sent.setAptitude(sent.getAptitude() + s.getAptitude());
				sent.setAttention(sent.getAttention() + s.getAttention());
				sent.setSensitivity(sent.getSensitivity() + s.getSensitivity());
				sent.setPleasantness(sent.getPleasantness() + s.getPleasantness());
				sent.setPolarity(sent.getPolarity() + s.getPolarity());
			}
			for (Entry<String, UserSentiment> entrySet : res.entrySet()) {
				UserSentiment us = entrySet.getValue();
				Sentiment s = us.getSentiment();
				s.setAptitude(s.getAptitude() / us.getNumTweets());
				s.setAttention(s.getAttention() / us.getNumTweets());
				s.setSensitivity(s.getSensitivity() / us.getNumTweets());
				s.setPleasantness(s.getPleasantness() / us.getNumTweets());
				s.setPolarity(s.getPolarity() / us.getNumTweets());
			}
			
			return res.values();
		}
		return null;
	}

	private void run() throws Exception {
		tweetSents = SQLConnector.getInstance().getTweetSentiments();
		for (TweetSentiment ts : tweetSents) {		
			if (!idMapping.containsKey(ts.getUserId())) {
				idMapping.put(ts.getUserId(), new LinkedList<TweetSentiment>());
			}
			List<TweetSentiment> sents = idMapping.get(ts.getUserId());
			sents.add(ts);
		}
		System.out.println("Tweet sentiments: " + tweetSents.length);
		HashMap<String, Integer> pos = new HashMap<String, Integer>();
		HashMap<String, Integer> neg = new HashMap<String, Integer>();
		for (List<TweetSentiment> sents : idMapping.values()) {
			for (UserSentiment us : avgSentiment(sents)) {
				if (!pos.containsKey(us.getTopic())) {
					pos.put(us.getTopic(), 0);
					neg.put(us.getTopic(), 0);
				}
				Sentiment s = us.getSentiment();
				if (s.getPolarity() > 0) {
					pos.put(us.getTopic(), pos.get(us.getTopic()) + 1);
				} else {
					neg.put(us.getTopic(), neg.get(us.getTopic()) + 1);
				}
				SQLConnector.getInstance().insertUserSentiment(us);
				System.out.println(us.getUserId() + ": " + sents.size() + ", sentiment: " + s);
			}
			
			
		}
		System.out.println("Positive: " + pos + ", negative: " + neg);
	}
	
}
