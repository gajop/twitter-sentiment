package jp.ac.iwatepu.sentic.parser;

import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.DBStatus;

public class ExtractRetweets {
	public static void main(String[] args) throws Exception {
		ExtractRetweets er = new ExtractRetweets();
		er.run();
	}
	
	public void run() throws Exception {
		System.out.println("Adding retweets...");
		long totalInserted = 0;
		for (DBStatus tweet: SQLConnector.getInstance().getTweets()) {				
			if (tweet.isRetweet()) {				
				//System.out.println("Insert retweet: " + tweet.getUserId() + " " +  tweet.getRetweetedStatusIdUserId() + " " +  tweet.getStatusId() + " " +  tweet.getRetweetedStatusId());
				SQLConnector.getInstance().insertRetweet(tweet.getUserId(), tweet.getRetweetedStatusIdUserId(), tweet.getStatusId(), tweet.getRetweetedStatusId());
				totalInserted++;
			} 
		}
		System.out.println("Total retweets inserted: " + totalInserted);
	}
}
