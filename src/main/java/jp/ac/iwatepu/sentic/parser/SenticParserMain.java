package jp.ac.iwatepu.sentic.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		System.out.print("CONCEPTS:");
		for (String concept : concepts) {
			System.out.print(" \"" + concept + "\"");
		}
		System.out.println();
		List<Sentiment> allSents = new LinkedList<Sentiment>();
		for (String concept : concepts) {
			Sentiment newSentiment = searchConcept(concept);
			if (newSentiment != null) {
				sentiment = newSentiment;
				System.out.println("\n!!!" + concept + ": " + newSentiment);
			} else {
				List<Sentiment> pSents = new LinkedList<Sentiment>();
				for (String p : concept.split(" ")) {
					newSentiment = searchConcept(p);
					if (newSentiment != null) {
						pSents.add(newSentiment);
					}
					System.out.println("\n" + p + ": " + newSentiment);
				}
				newSentiment = avgSentiment(pSents);
			}
			if (newSentiment != null) {
				allSents.add(newSentiment);
			}
			System.out.print(concept + ":" + searchConcept(concept) + " ");
		}
		sentiment = avgSentiment(allSents);
		return sentiment;		
	}
	
	private List<String> extractTopics(String text) {
		List<String> allMatches = new ArrayList<String>();
		Matcher m = Pattern.compile("#[A-Za-z0-9]+").matcher(text);
		while (m.find()) {
			allMatches.add(m.group().toLowerCase());
		}
		return allMatches;
	}

	public void run() throws Exception {		
		loadSenticNet();
		for (DBStatus tweet: SQLConnector.getInstance().getTweets()) {
			try {
				String text = tweet.getText();
				System.out.println(text);
				String searchTopic = "obama";
				if (!text.toLowerCase().contains(searchTopic)) {
					System.out.println("Skip tweet");
					continue;
				}
	
				Sentiment sentiment = extractSentiment(text);
				List<String> topics = extractTopics(text);
				System.out.println();
				
				System.out.println("Tweet Sentiment: " + sentiment);
				if (sentiment != null) {
					for (String topic : topics) {
						System.out.println("TOPIC: " + topic);
					}
					if (topics.contains(searchTopic) || text.toLowerCase().contains(searchTopic)) {
						System.out.println("Topic exists: " + searchTopic);
						SQLConnector.getInstance().insertStatusSentiment(tweet.getStatusId(), sentiment, searchTopic);
					}
				}
				/*if (sentiment != null && !topics.isEmpty()) {
					for (String topic : topics) {
						//SQLConnector.getInstance().insertStatusSentiment(tweet.getStatusId(), sentiment, topic);
					}
				}*/
				
				System.out.println("=========================");
				//cp.display_concepts(concepts.toArray(new String[concepts.size()]));
				//semantic_parser.concept_parser.display_concepts(new String[] {tweet});
				//semantic_parser.concept_parser.
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		//semantic_parser.concept_parser.display_concepts(new String[] {"abcd"});
	} 
}
