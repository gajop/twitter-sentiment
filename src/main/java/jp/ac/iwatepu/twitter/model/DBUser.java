package jp.ac.iwatepu.twitter.model;

import java.util.Date;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

public class DBUser implements User {
	long id;
	String screenName;
	String description;
	int friendCount;
	int followerCount;
	int recursionLevel = 2;	
	
	public int getRecursionLevel() {
		return recursionLevel;
	}

	public DBUser(long id, String screenName, String description,
			int friendCount, int followerCount, int recursionLevel) {
		super();
		this.id = id;
		this.screenName = screenName;
		this.description = description;
		this.friendCount = friendCount;
		this.followerCount = followerCount;
		this.recursionLevel = recursionLevel;
	}

	public int compareTo(User o) {
		// TODO Auto-generated method stub
		return 0;
	}

	public RateLimitStatus getRateLimitStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAccessLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getScreenName() {
		return screenName;
	}

	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDescription() {
		return description;
	}

	public boolean isContributorsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBiggerProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMiniProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOriginalProfileImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBiggerProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMiniProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOriginalProfileImageURLHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDefaultProfileImage() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isProtected() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getFollowersCount() {
		return followerCount;
	}

	public Status getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileTextColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileLinkColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileSidebarFillColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileSidebarBorderColor() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isProfileUseBackgroundImage() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDefaultProfile() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowAllInlineMedia() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getFriendsCount() {
		return friendCount;
	}

	public Date getCreatedAt() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getFavouritesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getUtcOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getTimeZone() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBackgroundImageURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBackgroundImageUrlHttps() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerIPadURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerIPadRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerMobileURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProfileBannerMobileRetinaURL() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isProfileBackgroundTiled() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getLang() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStatusesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isGeoEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isVerified() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTranslator() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getListedCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isFollowRequestSent() {
		// TODO Auto-generated method stub
		return false;
	}

	public URLEntity[] getDescriptionURLEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	public URLEntity getURLEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}
