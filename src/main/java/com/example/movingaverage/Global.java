package com.example.movingaverage;

import com.example.movingaverage.DAO.MongoCRUD;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.time.LocalDateTime;

public class Global {
    // if we abstract the markets now we will affect the DAO layer as well as the price object
    public static MongoCRUD mongoCRUD = MongoCRUD.getInstance();
    public static LocalDateTime start = null;
    public static String orderTimeInForce = "FILL_OR_KILL";
    public static double quant;
    public static String close = null;
    public static int len = Integer.MIN_VALUE;
    public static double candleLength;
    public static int rateLimit = 1000;
    final public static String HISTORICAL_DATA = "historicaldata";
    final public static String MARKET_SUMMARY = "marketsummary";
    public static HttpRequestFactory requestFactory
        = new NetHttpTransport().createRequestFactory();

}
