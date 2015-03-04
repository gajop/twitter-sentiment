package jp.ac.iwatepu.twittercrawler.stream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import jp.ac.iwatepu.db.DatabaseCleanupMain;
import jp.ac.iwatepu.twitter.SearchPreferences;
import jp.ac.iwatepu.twitter.consumer.DBTwitterConsumer;
import jp.ac.iwatepu.twitter.consumer.TwitterConsumer;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.twitter.hbc.twitter4j.Twitter4jStatusClient;

public class StreamCrawlerMain implements Runnable {	

	String consumerKey = "RK6DGmjlHBiyFgVmJwWhqX4e6";
	String consumerSecret = "sjlBLoiU38szzJu5cpCztFiFZG3Wgq2nhFOhMLFlQWxJ7n9w95";
	String token = "2338876789-GNm0smjH3T22ApEeyHefmyXtwUhDPwxizNXSoF8";
	String secret = "WkQIvRjn0xJ5rn9PdMJNmMvqGuS6cYIjGjFZvF4eYtC80";
	int downloaded = 0;
	int downloadLimit = 100;
	boolean excludeRetweets = true;
	boolean excludeUnlinkedUsers = true;
	
	TwitterConsumer twitterConsumer = new DBTwitterConsumer();

	public static void main(String[] args) throws FileNotFoundException, SQLException, IOException {
		DatabaseCleanupMain dcm = new DatabaseCleanupMain();
		dcm.run();
		StreamCrawlerMain sc = new StreamCrawlerMain();
		sc.run();
		System.exit(0);
	}
	
	private void saveStatus(Status status) {
		try {
			try {
				twitterConsumer.insertUser(status.getUser(), 0);						
			} catch (Exception e) {
				//might be a duplicate user, but we just ignore this status then
				return;
			}		
			twitterConsumer.insertStatus(status, true);
			downloaded++;
		} catch (Exception ex) {
			ex.printStackTrace();			
		}
	}	
	
	// A bare bones listener
	private StatusListener listener1 = new StatusListener() {
		public void onStatus(Status status) {
			if (excludeRetweets && status.isRetweet()) {
				return;
			}
			if (downloaded >= downloadLimit) {
				return;
			}
			if (excludeUnlinkedUsers && (status.getUser().getFriendsCount() == 0 && status.getUser().getFollowersCount() == 0)) {
				return;
			}
			
			saveStatus(status);
		}

		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

		public void onTrackLimitationNotice(int limit) {}

		public void onScrubGeo(long user, long upToStatus) {}

		public void onStallWarning(StallWarning warning) {}

		public void onException(Exception e) {}
	};

	public void run() {	
		// Create an appropriately sized blocking queue
		BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);

		// Define our endpoint: By default, delimited=length is set (we need this for our processor)
		// and stall warnings are on.
		StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();		
		endpoint.trackTerms(SearchPreferences.terms);	
		//endpoint.addPostParameter("track", SearchPreferences.terms);
		endpoint.languages(SearchPreferences.languages);		

		Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
		// Authentication auth = new BasicAuth(username, password);

		// Create a new BasicClient. By default gzip is enabled.
		BasicClient client = new ClientBuilder()
		.hosts(Constants.STREAM_HOST)
		.endpoint(endpoint)
		.authentication(auth)
		.processor(new StringDelimitedProcessor(queue))
		.build();

		// Create an executor service which will spawn threads to do the actual work of parsing the incoming messages and
		// calling the listeners on each message
		int numProcessingThreads = 4;
		ExecutorService service = Executors.newFixedThreadPool(numProcessingThreads);

		// Wrap our BasicClient with the twitter4j client
		Twitter4jStatusClient t4jClient = new Twitter4jStatusClient(
				client, queue, Lists.newArrayList(listener1), service);

		// Establish a connection
		t4jClient.connect();
		for (int threads = 0; threads < numProcessingThreads; threads++) {
			// This must be called once per processing thread
			t4jClient.process();
		}

		/*
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			while (downloaded != downloadLimit) {
				Thread.sleep(20);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		client.stop();		
	}


	public void run2() {
		//1) SETUP
		/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
		BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

		/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		// Optional: set up some followings and track terms
		//List<Long> followings = Lists.newArrayList(1234L, 566788L);
		List<String> terms = Lists.newArrayList("climatechange", "GunControl", "obama");		
		//hosebirdEndpoint.followings(followings);
		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

		//2) CREATE CLIENT
		ClientBuilder builder = new ClientBuilder()
		.name("Hosebird-Client-01")                              // optional: mainly for the logs
		.hosts(hosebirdHosts)
		.authentication(hosebirdAuth)
		.endpoint(hosebirdEndpoint)
		.processor(new StringDelimitedProcessor(msgQueue))
		.eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

		Client hosebirdClient = builder.build();
		// Attempts to establish a connection.
		hosebirdClient.connect();

		//3) MAIN LOOP
		// on a different thread, or multiple different threads....		
		while (!hosebirdClient.isDone()) {
			String msg;
			try {
				msg = msgQueue.take();
				System.out.println(msg);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}			
		}
	}
}
