package jp.ac.iwatepu.twitter.model;

public class Retweet {
	long statusId;
	long userId;
	String handle;
	
	long retweetedStatusId;
	long retweetedStatusIdUserId;
	String retweetedStatusIdUserHandle;
	public long getStatusId() {
		return statusId;
	}
	public void setStatusId(long statusId) {
		this.statusId = statusId;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public long getRetweetedStatusId() {
		return retweetedStatusId;
	}
	public void setRetweetedStatusId(long retweetedStatusId) {
		this.retweetedStatusId = retweetedStatusId;
	}
	public long getRetweetedStatusIdUserId() {
		return retweetedStatusIdUserId;
	}
	public void setRetweetedStatusIdUserId(long retweetedStatusIdUserId) {
		this.retweetedStatusIdUserId = retweetedStatusIdUserId;
	}
	public String getRetweetedStatusIdUserHandle() {
		return retweetedStatusIdUserHandle;
	}
	public void setRetweetedStatusIdUserHandle(String retweetedStatusIdUserHandle) {
		this.retweetedStatusIdUserHandle = retweetedStatusIdUserHandle;
	}
	public Retweet(long statusId, long userId, String handle,
			long retweetedStatusId, long retweetedStatusIdUserId,
			String retweetedStatusIdUserHandle) {
		super();
		this.statusId = statusId;
		this.userId = userId;
		this.handle = handle;
		this.retweetedStatusId = retweetedStatusId;
		this.retweetedStatusIdUserId = retweetedStatusIdUserId;
		this.retweetedStatusIdUserHandle = retweetedStatusIdUserHandle;
	}
	
	
}
