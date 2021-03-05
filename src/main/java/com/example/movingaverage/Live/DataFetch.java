package com.example.movingaverage.Live;


import com.example.movingaverage.Global;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class DataFetch {

    private ObjectMapper objectMapper;
    private String mOne;
    private String mTwo;
    private static  DataFetch soleInstanceDataFetch;

    private DataFetch() {
        this.objectMapper = new ObjectMapper();
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
        StringBuffer content = fetch(url);
        return objectMapper.readValue(content.toString(), new TypeReference<Map<?,?>>(){});
    }

    public ArrayList<Map<?,?>> historicalDataFetcher(String s) throws IOException, InterruptedException {
        URL url =
            new URL( "https://api.bittrex.com/v3/markets/" + mTwo + "-" + mOne + "/candles/" + s +
                "/recent");
        StringBuffer historicalData = fetch(url);
        return objectMapper.readValue(historicalData.toString(), new TypeReference<ArrayList<Map<?,?>>>(){});
    }

    public StringBuffer fetch(URL url) throws IOException {
        StringBuffer data = new StringBuffer();
        try (InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
             String inputLine = br.readLine();
             data.append(inputLine);
             //both requests are received as a single line
        }
        return data;
    }
    public boolean valid() {
        String marketV = "^(\\w|\\D|\\S){2,6}$";
        if (!mOne.matches(marketV)) return false;
        return mTwo.matches(marketV);
    }
}
