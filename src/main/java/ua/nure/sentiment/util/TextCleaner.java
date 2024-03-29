package ua.nure.sentiment.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;


public class TextCleaner {

    /**
     * Returns `text` with all sub-strings matching the regular expression `regex`
     * replaced by the string `normalizationString`
     */
    private static String applyNormalizationTemplate(String text, String regex, String normalizationString) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        return matcher.replaceAll(normalizationString);
    }

    /**
     * Returns `text` with every occurrence of one of the currency symbol
     * '$', '€' or '£' replaced by the string literal " normalizedcurrencysymbol ".
     */
    private static String normalizeCurrencySymbol(String text) {
        String regex = "[\\$\\€\\£]";
        //return applyNormalizationTemplate(text, regex, " normalizedcurrencysymbol ");
        return applyNormalizationTemplate(text, regex, " ");
    }

    /**
     * Returns `text` with every occurrence of one of a number
     * replaced by the string literal "normalizednumber".
     */
    private static String normalizeNumbers(String text) {
        String regex = "\\d+";
        //return applyNormalizationTemplate(text, regex, " normalizednumber ");
        return applyNormalizationTemplate(text, regex, " ");
    }

    /**
     * Returns `text` with every occurrence of one of an URL
     * replaced by the string literal " normalizedurl ".
     */
    private static String normalizeURL(String text) {
        String regex = "\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))";
        //return applyNormalizationTemplate(text, regex, " normalizedurl ");
        return applyNormalizationTemplate(text, regex, " ");
    }

    private static String removeNotLetters(String text) {
        return applyNormalizationTemplate(text, "[^a-zA-Z]", " ");
    }

    private static String normalizeNoise(String text) {
        text = text.replaceAll("([a-z])\\1\\1+", "$1");
        // text = text.replaceAll("@\\S+", " username ");
        text = text.replaceAll("@\\S+", " ");
        //text = text.replaceAll("#\\S+", " hashtag ");
        text = text.replaceAll("#\\S+", " ");
        return text;
    }

    public static List<String> nGram(List<String> words, int n) {
        return IntStream.range(0, words.size() - n + 1)
                .mapToObj(i -> new ArrayList<>(words.subList(i, i + n)).stream().collect(joining(" ")))
                .collect(toList());
    }

    public static String[] cleanTweet(String text) {
        String[] split = Optional.of(text.toLowerCase())
                .map(ShortFormNormalizer::normalizeText)
                .map(TextCleaner::normalizeNoise)
                .map(SentimentNormalizer::normalizeText)
                .map(TextCleaner::normalizeURL)
                .map(TextCleaner::normalizeCurrencySymbol)
                .map(TextCleaner::normalizeNumbers)
                .map(TextCleaner::removeNotLetters)
                .get().trim()
                .split("\\s+");
        return split;
    }

    public static List<String> tweetToTokens(String text) {
        List<String> uniGrams = Arrays.asList(cleanTweet(text));
        List<String> biGrams = nGram(uniGrams, 2);
        biGrams.addAll(uniGrams);
        return biGrams;
    }


}
