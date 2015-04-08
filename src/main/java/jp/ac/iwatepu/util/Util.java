package jp.ac.iwatepu.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jp.ac.iwatepu.sentic.parser.Topic;

public class Util {
	private static Util instance = new Util();
	public static Util getInstance() {
		return instance;
	}
	public List<Topic> getSearchTopics() {
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

		return searchTopics;
	}
}
