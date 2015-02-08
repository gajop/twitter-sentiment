package jp.ac.iwatepu.sentic.parser;

import java.io.InputStream;
import java.util.ArrayList;

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
import jp.ac.iwatepu.twittercrawler.stream.SQLConnector;

public class SenticParserMain {

	public static void main(String[] args) throws Exception {
		SenticParserMain senticParserMain = new SenticParserMain();
		senticParserMain.run();
	}

	Model model;
	private void loadSenticNet() {
		// create an empty model
		model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		String filename = "senticnet3.rdf.xml";
		InputStream in = FileManager.get().open(filename);		
		// read the RDF/XML file
		model.read(in, null);
		// write it to standard out
		//model.write(System.out);		
	}
	
	private String searchConcept(String concept) {
		String queryRequest = "PREFIX sentic: <http://sentic.net/api>\n" +				
				"SELECT ?polarity \n" +
				"WHERE\n" +
				" { ?x sentic:text \"" + concept + "\".\n" +
				"   ?x sentic:polarity ?polarity" +
				" }";			
		
		Query query = QueryFactory.create(queryRequest);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
			
		ResultSet response = qexec.execSelect();

		while (response.hasNext()) {
			QuerySolution soln = response.nextSolution();
			RDFNode polarityNode = soln.get("?polarity");
			return polarityNode.toString();
			//System.out.println(polarityNode.toString());
			//String seeAlsoStr = seeAlsoLink.toString();
		}
		return "";
	}

	public void run() throws Exception {
		concept_parser cp = new concept_parser();
		loadSenticNet();

		for (String tweet: SQLConnector.getInstance().getTweets()) {
			System.out.println(tweet);
			ArrayList<String> concepts = cp.get_concepts(tweet);
			for (String concept : concepts) {
				for (String p : concept.split(" ")) {
					System.out.println("\n" + p + ": " + searchConcept(p));
				}
				System.out.print(concept + ":" + searchConcept(concept) + " ");
			}
			//cp.display_concepts(concepts.toArray(new String[concepts.size()]));
			System.out.println();
			//semantic_parser.concept_parser.display_concepts(new String[] {tweet});
			//semantic_parser.concept_parser.
			Thread.sleep(500);
		}
		//semantic_parser.concept_parser.display_concepts(new String[] {"abcd"});
	} 
}
