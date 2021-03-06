package com.example.movingaverage.Live;


import com.example.movingaverage.Global;
import com.fasterxml.jackson.databind.*;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataFetch {

    private String mOne;
    private String mTwo;
    private static DataFetch soleInstanceDataFetch;

    private DataFetch() {
        this.mOne = Global.mOne;
        this.mTwo = Global.mTwo;
    }

    public static DataFetch getInstance() {
        if (soleInstanceDataFetch == null) {
            soleInstanceDataFetch = new DataFetch();
        }
        return soleInstanceDataFetch;
    }

    public Map<?, ?> marketDataFetcher() throws IOException, InterruptedException {
        URL url =
            new URL("https://api.bittrex.com/api/v1.1/public/getmarketsummary?market=" + mOne + "-" + mTwo);
        StringBuilder content = fetch(url);
        return stringToMap(content.toString());
        }

    public ArrayList<Map<Object, Object>> historicalDataFetcher(String s) throws IOException, InterruptedException {
        Pattern clean ="\"";
        ArrayList<Map<Object, Object>> arr = new ArrayList<>();
        URL url =
            new URL("https://api.bittrex.com/v3/markets/" + mTwo + "-" + mOne + "/candles/" + s +
                "/recent");
        StringBuilder historicalData = fetch(url);
        String[] historicalSplit = historicalData.toString().replaceAll(clean).split("},");

        for (String value : historicalSplit) {
            arr.add(stringToMap(value));
        }
        return arr;
    }

    public StringBuilder fetch(URL url) throws IOException {
        StringBuilder data = new StringBuilder();
        try (InputStream is = url.openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String inputLine = br.readLine();
            data.append(inputLine);
        }
        return data;
    }
    private Map<Object, Object> stringToMap(String s) {
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
