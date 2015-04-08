package jp.ac.iwatepu.twitter.model;

public class DBStatus {
	long statusId;
	String text;
	long userId;
	boolean isRetweet;
	boolean isRetweeted;
	long retweetedStatusId;
	String retweetedStatusIdUserHandle;
	long retweetedStatusIdUserId;
	
	public long getRetweetedStatusId() {
		return retweetedStatusId;
	}
	public void setRetweetedStatusId(long retweetedStatusId) {
		this.retweetedStatusId = retweetedStatusId;
	}
	public String getRetweetedStatusIdUserHandle() {
		return retweetedStatusIdUserHandle;
	}
	public void setRetweetedStatusIdUserHandle(String retweetedStatusIdUserHandle) {
		this.retweetedStatusIdUserHandle = retweetedStatusIdUserHandle;
	}
	public long getRetweetedStatusIdUserId() {
		return retweetedStatusIdUserId;
	}
	public void setRetweetedStatusIdUserId(long retweetedStatusIdUserId) {
		this.retweetedStatusIdUserId = retweetedStatusIdUserId;
	}
	public long getStatusId() {
		return statusId;
	}
	public void setStatusId(long statusId) {
		this.statusId = statusId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public boolean isRetweet() {
		return isRetweet;
	}
	public void setRetweet(boolean isRetweet) {
		this.isRetweet = isRetweet;
	}
	public boolean isRetweeted() {
		return isRetweeted;
	}
	public void setRetweeted(boolean isRetweeted) {
		this.isRetweeted = isRetweeted;
	}
	public DBStatus(long statusId, String text) {
		super();
		this.statusId = statusId;
		this.text = text;
	}
	
	
}
