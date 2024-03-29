package ua.nure.sentiment.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.TwitterException;
import ua.nure.sentiment.converter.TweeterConverter;
import ua.nure.sentiment.entity.Country;
import ua.nure.sentiment.entity.CountrySentiment;
import ua.nure.sentiment.entity.Sentiment;
import ua.nure.sentiment.entity.Tweet;
import ua.nure.sentiment.service.DictionarySentimentService;
import ua.nure.sentiment.service.SparkSentimentService;
import ua.nure.sentiment.service.TwitterService;
import ua.nure.sentiment.service.CoreSentimentAnalysisService;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static ua.nure.sentiment.util.DateUtil.getDayOfWeek;

@Component
public class TwitterFacade {

    @Autowired
    private TwitterService twitterService;

    @Autowired
    private TweeterConverter tweeterConverter;

    @Autowired
    private ForkJoinPool searchPool;

    @Autowired
    private CoreSentimentAnalysisService coreSentimentAnalysisService;

    @Autowired
    private DictionarySentimentService dictionarySentimentService;

    @Autowired
    private SparkSentimentService sparkSentimentService;

    public List<Tweet> getTweets(List<String> tags, int count) throws TwitterException, ExecutionException, InterruptedException {
        return searchPool.submit(() -> twitterService.getTweetsForWeek(tags, count).parallelStream()
                .map(status -> tweeterConverter.convert(status)).collect(toList()))
                .get();
    }

    public Sentiment getSentiment(String text) {
        Tweet tweet = new Tweet();
        tweet.setText(text);
        tweet.setCoreSentiment(coreSentimentAnalysisService.detectSentiment(text));
        tweet.setDictionarySentiment(dictionarySentimentService.detectSentiment(text));
        tweet.setLogisticSentiment(sparkSentimentService.detectSentiment(text));
        return tweet.getSentiment();
    }

    public Map<Integer, Long> groupTweetsByDayOfWeek(List<Tweet> tweets, Sentiment sentiment) {
        return tweets.stream()
                .filter(tw -> sentiment.equals(tw.getSentiment()))
                .map(tw -> getDayOfWeek(tw.getCreatedAt()))
                .collect(groupingBy(identity(), counting()));
    }

    public Map<Sentiment, Long> groupTweetsBySentiment(List<Tweet> tweets) {
        return tweets.stream().map(Tweet::getSentiment).collect(groupingBy(identity(), counting()));
    }

    public List<CountrySentiment> getCountrySentiment(List<Tweet> tweets) {
        return tweets.stream().collect(groupingBy(Tweet::getCountry))
                .entrySet().stream().map(entry -> {
                    CountrySentiment countrySentiment = new CountrySentiment();
                    countrySentiment.setCountry(entry.getKey());
                    countrySentiment.setTotalCount(entry.getValue().size());
                    Map<Sentiment, List<Tweet>> collect = entry.getValue().stream().collect(groupingBy(Tweet::getSentiment));
                    countrySentiment.setNegative(collect.get(Sentiment.NEGATIVE));
                    countrySentiment.setNeutral(collect.get(Sentiment.NEUTRAL));
                    countrySentiment.setPositive(collect.get(Sentiment.POSITIVE));
                    return countrySentiment;
                }).collect(toList());
    }

    public List<Tweet> getGeoTweets(List<String> tags, List<Country> locs, int count)
            throws ExecutionException, InterruptedException {
        return searchPool.submit(() -> twitterService.getTweetsByGeoLocation(tags, locs, count).entrySet().parallelStream()
                .flatMap(entry -> entry.getValue().stream().map(status -> {
                            Tweet tweet = tweeterConverter.convert(status);
                            tweet.setCountry(entry.getKey());
                            return tweet;
                        }
                ))
                .collect(toList())).get();
    }

    public List<Tweet> getOpinion(List<String> strings) {
        return Collections.emptyList();
    }

}
