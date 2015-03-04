package jp.ac.iwatepu.twitter.model;

public class Reply {
	long statusId;
	long userId;
	String handle;
	
	long replyToStatusId;
	long replyToUserId;
	String replyToScreenName;
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
	public long getReplyToStatusId() {
		return replyToStatusId;
	}
	public void setReplyToStatusId(long replyToStatusId) {
		this.replyToStatusId = replyToStatusId;
	}
	public long getReplyToUserId() {
		return replyToUserId;
	}
	public void setReplyToUserId(long replyToUserId) {
		this.replyToUserId = replyToUserId;
	}
	public String getReplyToScreenName() {
		return replyToScreenName;
	}
	public void setReplyToScreenName(String replyToScreenName) {
		this.replyToScreenName = replyToScreenName;
	}
	public Reply(long statusId, long userId, String handle,
			long replyToStatusId, long replyToUserId, String replyToScreenName) {
		super();
		this.statusId = statusId;
		this.userId = userId;
		this.handle = handle;
		this.replyToStatusId = replyToStatusId;
		this.replyToUserId = replyToUserId;
		this.replyToScreenName = replyToScreenName;
	}
	

	
}
