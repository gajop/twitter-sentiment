package jp.ac.iwatepu.sentic.parser;

import java.util.List;

public class Topic {
	String topic;
	List<String> topicSearch;
	List<String> negativeTopicSearch;
	List<String> sentimentHashtags;
	List<String> negativeSentimentHashtags;	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public List<String> getTopicSearch() {
		return topicSearch;
	}
	public void setTopicSearch(List<String> topicSearch) {
		this.topicSearch = topicSearch;
	}
	public List<String> getNegativeTopicSearch() {
		return negativeTopicSearch;
	}
	public void setNegativeTopicSearch(List<String> negativeTopicSearch) {
		this.negativeTopicSearch = negativeTopicSearch;
	}
	public List<String> getSentimentHashtags() {
		return sentimentHashtags;
	}
	public void setSentimentHashtags(List<String> sentimentHashtags) {
		this.sentimentHashtags = sentimentHashtags;
	}
	public List<String> getNegativeSentimentHashtags() {
		return negativeSentimentHashtags;
	}
	public void setNegativeSentimentHashtags(List<String> negativeSentimentHashtags) {
		this.negativeSentimentHashtags = negativeSentimentHashtags;
	}
	public Topic(String topic, List<String> topicSearch,
			List<String> negativeTopicSearch, List<String> sentimentHashtags,
			List<String> negativeSentimentHashtags) {
		super();
		this.topic = topic;
		this.topicSearch = topicSearch;
		this.negativeTopicSearch = negativeTopicSearch;
		this.sentimentHashtags = sentimentHashtags;
		this.negativeSentimentHashtags = negativeSentimentHashtags;
	}
		
	
}
