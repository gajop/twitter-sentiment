package jp.ac.iwatepu.twittercrawler.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class StreamCrawlerMain {
	File outputFile;
	
	public static void main(String[] args) throws Exception {
		StreamCrawlerMain cm = new StreamCrawlerMain();
		cm.run();
	}

	private void run() throws InterruptedException, FileNotFoundException {
		//1) SETUP
		/** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
		BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

		/** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		// Optional: set up some followings and track terms
		//List<Long> followings = Lists.newArrayList(1234L, 566788L);
		List<String> terms = Lists.newArrayList("climatechange", "GunControl", "obama", "txlegechat");		
		//hosebirdEndpoint.followings(followings);
		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1("RK6DGmjlHBiyFgVmJwWhqX4e6", "sjlBLoiU38szzJu5cpCztFiFZG3Wgq2nhFOhMLFlQWxJ7n9w95", "2338876789-GNm0smjH3T22ApEeyHefmyXtwUhDPwxizNXSoF8", "WkQIvRjn0xJ5rn9PdMJNmMvqGuS6cYIjGjFZvF4eYtC80");
		
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
		
		String outputFileName = "stream-" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date())  + ".json";
		System.out.println("output file: " + outputFileName);
		outputFile = new File(outputFileName);
		
		PrintWriter pw = new PrintWriter(outputFile);
		//3) MAIN LOOP
		// on a different thread, or multiple different threads....
		int flushAmount = 100;
		int downloaded = 0;
		while (!hosebirdClient.isDone()) {
		  String msg = msgQueue.take();
		  pw.write(msg);
		  
		  downloaded++;
		  if (downloaded % flushAmount == 0) {
			  System.out.println("Downloaded: " + downloaded);
			  pw.flush();  
		  }		  
		  //System.out.println(msg);
		  
		}
		pw.close();
	}
	
}
