package jp.ac.iwatepu.sentic.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.TweetSentiment;
import jp.ac.iwatepu.twitter.model.TwitterUserNode;

public class UserAggregator {
	HashMap<Long, List<TweetSentiment>> idMapping = new HashMap<Long, List<TweetSentiment>>();
	TweetSentiment[] tweetSents;
	
	public static void main(String[] args) throws Exception {
		UserAggregator ua = new UserAggregator();
		ua.run();
	}
	
	private Sentiment avgSentiment(List<TweetSentiment> pSents) {
		if (!pSents.isEmpty()) {
			Sentiment newSentiment = new Sentiment();
			for (TweetSentiment ts: pSents) {
				Sentiment s = ts.getSentiment();
				newSentiment.setAptitude(newSentiment.getAptitude() + s.getAptitude());
				newSentiment.setAttention(newSentiment.getAttention() + s.getAttention());
				newSentiment.setSensitivity(newSentiment.getSensitivity() + s.getSensitivity());
				newSentiment.setPleasantness(newSentiment.getPleasantness() + s.getPleasantness());
				newSentiment.setPolarity(newSentiment.getPolarity() + s.getPolarity());
			}
			newSentiment.setAptitude(newSentiment.getAptitude() / pSents.size());
			newSentiment.setAttention(newSentiment.getAttention() / pSents.size());
			newSentiment.setSensitivity(newSentiment.getSensitivity() / pSents.size());
			newSentiment.setPleasantness(newSentiment.getPleasantness() / pSents.size());
			newSentiment.setPolarity(newSentiment.getPolarity() / pSents.size());
			return newSentiment;
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
		int pos = 0;
		int neg = 0;
		for (List<TweetSentiment> sents : idMapping.values()) {
			Sentiment s = avgSentiment(sents);
			if (s.getPolarity() > 0) {
				pos++;
			} else {
				neg++;
			}
			SQLConnector.getInstance().insertUserSentiment(sents.get(0).getUserId(), s, sents.get(0).getTopic());
			System.out.println(sents.get(0).getUserId() + ": " + sents.size() + ", sentiment: " + s);
		}
		System.out.println("Positive: " + pos + ", negative: " + neg);
	}
	
}
