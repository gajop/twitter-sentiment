package jp.ac.iwatepu.sentic.parser;

public class Sentiment {
	double polarity;
	double pleasantness, attention, sensitivity, aptitude;
	
	public double getPolarity() {
		return polarity;
	}
	public void setPolarity(double polarity) {
		this.polarity = polarity;
	}
	public double getPleasantness() {
		return pleasantness;
	}
	public void setPleasantness(double pleasantness) {
		this.pleasantness = pleasantness;
	}
	public double getAttention() {
		return attention;
	}
	public void setAttention(double attention) {
		this.attention = attention;
	}
	public double getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}
	public double getAptitude() {
		return aptitude;
	}
	public void setAptitude(double aptitude) {
		this.aptitude = aptitude;
	}
	public Sentiment(double polarity, double pleasantness, double attention,
			double sensitivity, double aptitude) {
		super();
		this.polarity = polarity;
		this.pleasantness = pleasantness;
		this.attention = attention;
		this.sensitivity = sensitivity;
		this.aptitude = aptitude;
	}	
	public Sentiment() {		
	}
	@Override
	public String toString() {
		return "Sentiment [polarity=" + polarity + ", pleasantness="
				+ pleasantness + ", attention=" + attention + ", sensitivity="
				+ sensitivity + ", aptitude=" + aptitude + "]";
	}
	
	
}
