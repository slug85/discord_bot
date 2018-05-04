package com.slug85.http;

import ga.dryco.redditjerk.api.enums.FromPast;
import ga.dryco.redditjerk.api.enums.Sorting;
import ga.dryco.redditjerk.implementation.RedditApi;
import ga.dryco.redditjerk.wrappers.Link;
import ga.dryco.redditjerk.wrappers.RedditThread;
import ga.dryco.redditjerk.wrappers.Subreddit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MyRedditClient implements InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(MyRedditClient.class);

    @Value("${reddit.clientid}")
    private String clientid;

    @Value("${reddit.clientsecret}")
    private String clientSecret;

    @Value("${reddit.user}")
    private String user;

    @Value("${reddit.password}")
    private String password;

    private RedditApi red;

    private List<String> subReddits = Arrays.asList(
            "gifs",
            "funny",
            "Wellthatsucks",
            "instant_regret",
            "holdmycosmo",
            "holdmybeer"
    );


    @Override
    public void afterPropertiesSet() {

        //Getting the api instance with useragent as parameter:
        red = RedditApi.getRedditInstance("Reddit client");

        //OAuth login, returns User object of the logged in account
        red.login(user, password, clientid, clientSecret);


    }

    public RedditThread getRandomMeme(){

        String r = subReddits.get(new Random().nextInt(subReddits.size()-1));
        Subreddit randomReddit = red.getSubreddit(r);
        List<Link> posts = randomReddit.getHot(50, FromPast.DAY);

        Link randomPost = posts.get(new Random().nextInt(posts.size()-1));
        if(!randomPost.getOver18()){
            try {
                return red.getRedditThread(
                        "https://www.reddit.com/"+randomPost.getPermalink(), Sorting.NEW);
            } catch (MalformedURLException e) {
                LOGGER.error(e.getLocalizedMessage());
                return null;
            }
        }
        else return null;



    }
}
