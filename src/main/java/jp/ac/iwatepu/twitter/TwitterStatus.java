package jp.ac.iwatepu.twitter;
import java.util.Date;

public class TwitterStatus {
    int tweet_id;
    String handle;
    String text;
    double lat;
    long lon;
    Date created_at;
    /*processed boolean,
    sourceHashtag VARCHAR(128),
    recursionLevel INTEGER*/
	public int getTweet_id() {
		return tweet_id;
	}
	public void setTweet_id(int tweet_id) {
		this.tweet_id = tweet_id;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public long getLon() {
		return lon;
	}
	public void setLon(long lon) {
		this.lon = lon;
	}
	public Date getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Date created_at) {
		this.created_at = created_at;
	}
	public TwitterStatus(int tweet_id, String handle, String text, double lat,
			long lon, Date created_at) {
		super();
		this.tweet_id = tweet_id;
		this.handle = handle;
		this.text = text;
		this.lat = lat;
		this.lon = lon;
		this.created_at = created_at;
	}
    
	public TwitterStatus() {
		super();
	}
	@Override
	public String toString() {
		return "TwitterStatus [tweet_id=" + tweet_id + ", handle=" + handle
				+ ", text=" + text + ", lat=" + lat + ", lon=" + lon
				+ ", created_at=" + created_at + "]";
	}
    
}

