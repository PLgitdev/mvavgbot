package com.example.movingaverage.Live;


import com.example.movingaverage.Global;
import java.io.IOException;

import com.example.movingaverage.session.PriceSession;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import java.util.*;
import java.util.stream.Collectors;

public class DataFetch {

    final private String CLEAN_REGEX = "[\\[{\"]";
    final private String mOne;
    final private String mTwo;

    private static DataFetch soleInstanceDataFetch;

    private DataFetch() {
        this.mOne = PriceSession.mOne;
        this.mTwo = PriceSession.mTwo;
    }

    public static DataFetch getNewInstance() {
        return new DataFetch();
    }

    public Map<String, Object> marketDataFetch() throws IOException {
        String url = "https://api.bittrex.com/api/v1.1/public/getmarketsummary?market=" + mOne + "-" + mTwo;
        String content = fetch(url);
        String[] splitContent = content.split("\\[");
        String clean = splitContent[1].replaceAll(CLEAN_REGEX, "");
        return stringToMap(clean);
    }

    public LinkedList<Map<String, Object>> historicalDataFetcher(String s) throws IOException {
        LinkedList<Map<String,Object>> arr = new LinkedList<>();
        String url = ("https://api.bittrex.com/v3/markets/" + mTwo + "-" + mOne + "/candles/" + s + "/recent");
        String historicalData = fetch(url);
        String[] historicalSplit = historicalData.replaceAll(CLEAN_REGEX, "").split("},");
        for (String value : historicalSplit) {
            arr.push(stringToMap(value));
        }
        return arr;
    }

    public String fetch(String url) throws IOException {
        HttpRequest request = Global.requestFactory.buildGetRequest(
            new GenericUrl(url));
        return request.execute().parseAsString();
    }
    private Map<String, Object> stringToMap(String s) {
        return Arrays
            .stream(s.split( ",")).map(r -> r.split(":"))
            .collect(Collectors.toMap(a -> a[0], a ->  a[1]));
    }

    public boolean valid() {
        String marketV = "^(\\w|\\D|\\S){2,6}$";
        if (!mOne.matches(marketV)) return false;
        return mTwo.matches(marketV);
    }
}
