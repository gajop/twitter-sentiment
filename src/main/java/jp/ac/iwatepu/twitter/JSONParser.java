package jp.ac.iwatepu.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import jp.ac.iwatepu.db.SQLConnector;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

public class JSONParser {
	String inputFile = "stream-201502060137.json";
	
	public static void main(String[] args) throws Exception {
		JSONParser jsonParser = new JSONParser();
		jsonParser.run();
	}
	
	BufferedReader br;
	public void init() throws FileNotFoundException {
		br = new BufferedReader(new FileReader(new File(inputFile)));
	}
	
	public Status getNextStatus() throws Exception {
		String line = br.readLine();
		Status status = TwitterObjectFactory.createStatus(line);
		return status;
		
		/*		
		Object obj = JSONValue.parse(line);
		
		if (obj == null) {
			return null;
		}
		JSONObject json = (JSONObject) obj;*/
				
		//Status status = 
		//TwitterStatus twitterStatus = new TwitterStatus();
		//twitterStatus.setHandle((String) json.get("handle"));
		//twitterStatus.setTweet_id((Integer) json.get("id"));
		//twitterStatus.setCreated_at(json.get("created_at"));		
	}
	
	public void run() throws Exception {
		init();
		
	    Status status;
	    int processed = 0;
	    while (true) {
	    	try {
	    		status = getNextStatus();
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
	    		continue;
	    	}
	    	if (status == null) {
	    		break;
	    	}
	    	
	    	if (!status.getLang().equals("en")) { // only parse english tweets
	    		continue;
	    	}
	    	processed++;
	    	if (processed % 100 == 0) {
	    		System.out.println("Processed : " + processed);
	    	}
	    	SQLConnector.getInstance().insertStatus(status, true);	    	
	    }	    	 

    	/*User user = status.getUser();
    	System.out.println("User: " + user.getName() + "link: " + user.getURL());
    	System.out.println("Description: " + user.getDescription());
    	
    	IDs followerIDs = twitter.getFollowersIDs(user.getId());
    	System.out.println(followerIDs);
    	
    	/*
	    	System.out.println("User: " + user.getName());
	    	System.out.println("Followers: " + user.getFollowersCount());
	    	//semantic_parser.concept_parser.display_concepts(status.getText().split("\\s+"));
	    	System.out.println(status.getText());
	    	semantic_parser.concept_parser.main(new String[] { status.getText() });
	    	//semantic_parser.example.main(status.getText().split("\\s+"));
	    	//System.out.println(twitter.showUser(status.getId()));
	    	//System.out.println(twitter.getFollowersIDs(handle));
    	 
    	 */
    	
    
	    
	}
}
