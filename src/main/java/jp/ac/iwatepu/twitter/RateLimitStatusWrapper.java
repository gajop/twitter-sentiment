package jp.ac.iwatepu.twitter;

import java.util.Date;

import twitter4j.RateLimitStatus;

public class RateLimitStatusWrapper implements RateLimitStatus {
	RateLimitStatus orig;
	Date secondsUntilReset;
	
	public RateLimitStatusWrapper(RateLimitStatus orig) {
		secondsUntilReset = new Date(orig.getSecondsUntilReset() * 1000 + new Date().getTime() + 30000); 
		//add an extra 30s wait time
		this.orig = orig;
	}
	
	public int getRemaining() {
		return orig.getRemaining();
	}

	public int getLimit() {
		return orig.getLimit();
	}

	public int getResetTimeInSeconds() {
		return orig.getResetTimeInSeconds();
	}

	public int getSecondsUntilReset() {
		return (int) ((secondsUntilReset.getTime() - (new Date().getTime())) / 1000);
	}
	
	@Override
	public String toString() {	
		return orig.toString() + " REAL remaining time in seconds (override): " + getResetTimeInSeconds();
	}
}