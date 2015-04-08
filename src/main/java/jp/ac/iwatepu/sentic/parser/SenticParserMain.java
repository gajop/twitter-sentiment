package jp.ac.iwatepu.sentic.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.FileManager;

import semantic_parser.concept_parser;
import jp.ac.iwatepu.db.SQLConnector;
import jp.ac.iwatepu.twitter.model.DBStatus;

public class SenticParserMain {

	public static void main(String[] args) throws Exception {
		SenticParserMain senticParserMain = new SenticParserMain();
		senticParserMain.run();
	}

	Model model;
	concept_parser cp = new concept_parser();
	private void loadSenticNet() {
		model = ModelFactory.createDefaultModel();
		String filename = "senticnet3.rdf.xml";
		InputStream in = FileManager.get().open(filename);		
		model.read(in, null);
	}
	
	private Sentiment searchConcept(String concept) {
		String queryRequest = "PREFIX sentic: <http://sentic.net/api>\n" +				
				"SELECT ?polarity ?pleasantness ?attention ?sensitivity ?aptitude \n" +
				"WHERE\n" +
				" { ?x sentic:text \"" + concept + "\".\n" +
				"   ?x sentic:polarity ?polarity." +
				"   ?x sentic:pleasantness ?pleasantness." +
				"   ?x sentic:attention ?attention." +
				"   ?x sentic:sensitivity ?sensitivity." +
				"   ?x sentic:aptitude ?aptitude" +
				" }";			
		
		Query query = QueryFactory.create(queryRequest);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
			
		ResultSet response = qexec.execSelect();
		try {
		while (response.hasNext()) {
			QuerySolution soln = response.nextSolution();
			Sentiment sentiment = new Sentiment();
			RDFNode polarityNode = soln.get("?polarity");
			RDFNode pleasantnessNode = soln.get("?pleasantness");
			RDFNode attentionNode = soln.get("?attention");
			RDFNode sensitivityNode = soln.get("?sensitivity");
			RDFNode aptitudeNode = soln.get("?aptitude");
			
			// pleasantness, attention, sensitivity, aptitude			
			sentiment.setPolarity(polarityNode.asLiteral().getFloat());
			sentiment.setPleasantness(pleasantnessNode.asLiteral().getFloat());
			sentiment.setAttention(attentionNode.asLiteral().getFloat());
			sentiment.setSensitivity(sensitivityNode.asLiteral().getFloat());
			sentiment.setAptitude(aptitudeNode.asLiteral().getFloat());
			
			return sentiment;
			//System.out.println(polarityNode.toString());
			//String seeAlsoStr = seeAlsoLink.toString();
		}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		return null;
	}
	
	private Sentiment avgSentiment(List<Sentiment> pSents) {
		if (!pSents.isEmpty()) {
			Sentiment newSentiment = new Sentiment();
			for (Sentiment s: pSents) {
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
	
	private Sentiment extractSentiment(String text) throws IOException {
		Sentiment sentiment = null;
		ArrayList<String> concepts = cp.get_concepts(text);
		//System.out.print("CONCEPTS:");
		for (String concept : concepts) {
			//System.out.print(" \"" + concept + "\"");
		}
		//System.out.println();
		List<Sentiment> allSents = new LinkedList<Sentiment>();
		for (String concept : concepts) {
			Sentiment newSentiment = searchConcept(concept);
			if (newSentiment != null) {
				sentiment = newSentiment;
				//System.out.println("\n!!!" + concept + ": " + newSentiment);
			} else {
				List<Sentiment> pSents = new LinkedList<Sentiment>();
				for (String p : concept.split(" ")) {
					newSentiment = searchConcept(p);
					if (newSentiment != null) {
						pSents.add(newSentiment);
					}
					//System.out.println("\n" + p + ": " + newSentiment);
				}
				newSentiment = avgSentiment(pSents);
			}
			if (newSentiment != null) {
				allSents.add(newSentiment);
			}
			//System.out.print(concept + ":" + searchConcept(concept) + " ");
		}
		sentiment = avgSentiment(allSents);
		return sentiment;		
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
		loadSenticNet();
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
				//System.out.println(text);
	
				Sentiment sentiment = extractSentiment(text);
				
				//System.out.println();
				
				//System.out.println("Tweet Sentiment: " + sentiment);
				if (sentiment != null) {
					for (int i = 0; i < matchingTopics.size(); i++) {
						Topic topic = searchTopics.get(matchingTopics.get(i));
						Sentiment s = new Sentiment();
						s.setAptitude(sentiment.getAptitude());
						s.setAttention(sentiment.getAttention());
						s.setSensitivity(sentiment.getSensitivity());
						s.setPleasantness(sentiment.getPleasantness());
						s.setPolarity(sentiment.getPolarity());
						
						if (negHashtagFound[i]) {
							s.polarity -= 0.5;
						}
						if (posHashtagFound[i]) {
							s.polarity += 0.5;
						}
						
						if (negFound[i] && !posFound[i]) {
							s.polarity = -s.polarity;
						}
						s.polarity = Math.max(-1, s.polarity);
						s.polarity = Math.min(1, s.polarity);
						if (s.polarity != sentiment.polarity) {
							polarityChanged[i]++;
						}
						SQLConnector.getInstance().insertStatusSentiment(tweet.getStatusId(), s, topic.getTopic());
					}
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
		//semantic_parser.concept_parser.display_concepts(new String[] {"abcd"});
	} 
}
