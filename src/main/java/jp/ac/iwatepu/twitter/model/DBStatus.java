package jp.ac.iwatepu.twitter.model;

public class DBStatus {
	long statusId;
	String text;
	long userId;
	
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
	public DBStatus(long statusId, String text) {
		super();
		this.statusId = statusId;
		this.text = text;
	}
	
	
}
