DROP TABLE IF EXISTS twitter_status;
CREATE TABLE twitter_status
(
    id BIGSERIAL PRIMARY KEY,
    tweet_id BIGINT NOT NULL, --UNIQUE
    handle VARCHAR(64),
    user_id BIGINT NOT NULL,
    text VARCHAR(192),
    lat DECIMAL(11,7),
    lon DECIMAL(11,7),
    created_at timestamp,
    
    sourceHashtag VARCHAR(128)       
);

DROP TABLE IF EXISTS twitter_user;
CREATE TABLE twitter_user
(
    id BIGINT PRIMARY KEY,
    handle VARCHAR(64),
    description VARCHAR(2000),
    
    recursionLevel INTEGER,
    
    processedFollowers boolean DEFAULT(false),
    processedFriends boolean DEFAULT(false),
    processedTweets boolean DEFAULT(false)
);
