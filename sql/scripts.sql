select count(*) from twitter_user where not processedTweets;
select count(*) from twitter_user where processedTweets;
--SELECT A.ABC_ID, A.VAL LEFT OUTER JOIN B ON A.ABC_ID = B.ABC_ID AND A.VAL = B.VAL WHERE B.VAL IS NULL
SELECT twitter_status.id, twitter_status.text from twitter_status left outer join twitter_status_sentiment on twitter_status.id = twitter_status_sentiment.status_id where twitter_status_sentiment.status_id is NULL;
--select name from student left join student_course on id = st_id where st_id is NULL


select count(*) from twitter_status_sentiment;