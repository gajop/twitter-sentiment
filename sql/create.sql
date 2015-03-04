DROP TABLE IF EXISTS twitter_status_sentiment;

DROP TABLE IF EXISTS twitter_followers;
DROP TABLE IF EXISTS twitter_friends;
DROP TABLE IF EXISTS twitter_retweets;

DROP TABLE IF EXISTS twitter_status_user_entity;
DROP TABLE IF EXISTS twitter_status_url_entity;
DROP TABLE IF EXISTS twitter_status_symbol_entity;
DROP TABLE IF EXISTS twitter_status_hashtag;
DROP TABLE IF EXISTS twitter_status;

DROP TABLE IF EXISTS twitter_user_profile;
DROP TABLE IF EXISTS twitter_user;


CREATE TABLE twitter_user
(
    id BIGINT PRIMARY KEY,
    handle VARCHAR(64),
    description VARCHAR(2000),
    
    recursionLevel INTEGER,

    followerCount INTEGER DEFAULT 0,
    friendCount INTEGER DEFAULT 0,
    
    processedFollowers boolean DEFAULT(false),
    processedFriends boolean DEFAULT(false),
    processedTweets boolean DEFAULT(false)
);


CREATE TABLE twitter_status
(
    id BIGSERIAL PRIMARY KEY,
    handle VARCHAR(64),
    user_id BIGINT NOT NULL,
    text VARCHAR(192),    
    created_at timestamp,
	isSeed boolean DEFAULT(false),
	isTruncated BOOLEAN,
	isPossiblySensitive BOOLEAN,
	isRetweet BOOLEAN,
	isRetweeted BOOLEAN,
	favoriteCount INTEGER,
	retweetCount INTEGER,
	retweetedStatusId BIGINT,
	retweetedStatusIdUserHandle VARCHAR(64),
	retweetedStatusIdUserId BIGINT,
	inReplyToScreenName VARCHAR(64),
	inReplyToStatusId BIGINT,
	inReplyToUserId BIGINT,
	lang VARCHAR(10),
	lat REAL,
	long REAL
);

CREATE TABLE twitter_status_hashtag
(
    id BIGSERIAL PRIMARY KEY,
    status_id BIGINT REFERENCES twitter_status(id),
    hashtag VARCHAR(80)
);

CREATE TABLE twitter_status_user_entity
(
    id BIGSERIAL PRIMARY KEY,
    status_id BIGINT REFERENCES twitter_status(id),
    user_id BIGINT,
    handle VARCHAR(64),
    startIndex INTEGER,
    endIndex INTEGER
);

CREATE TABLE twitter_status_url_entity
(
	id BIGSERIAL PRIMARY KEY,
	status_id BIGINT REFERENCES twitter_status(id),
	url VARCHAR(1000), 
	displayUrl VARCHAR(1000), 
	expandedUrl VARCHAR(1000),
    startIndex INTEGER,
    endIndex INTEGER
);

CREATE TABLE twitter_status_symbol_entity
(
	id BIGSERIAL PRIMARY KEY,
	status_id BIGINT REFERENCES twitter_status(id),
	text VARCHAR(200), 
    startIndex INTEGER,
    endIndex INTEGER
);

CREATE TABLE twitter_followers
(
    user_id BIGINT,
    follower_id BIGINT
);

CREATE TABLE twitter_friends
(
    user_id BIGINT,
    friend_id BIGINT
);

CREATE TABLE twitter_retweets
(
    user_id BIGINT,
    retweet_user_id BIGINT,
    status_id BIGINT
);

CREATE TABLE twitter_status_sentiment
(
    status_id BIGINT,
    topic VARCHAR(80),
    polarity NUMERIC,
    pleasantness NUMERIC, 
    attention NUMERIC, 
    sensitivity NUMERIC, 
    aptitude NUMERIC
);

CREATE TABLE twitter_user_profile
(
    user_id BIGINT,
    topic VARCHAR(80),
    polarity NUMERIC,
    pleasantness NUMERIC, 
    attention NUMERIC, 
    sensitivity NUMERIC, 
    aptitude NUMERIC
);