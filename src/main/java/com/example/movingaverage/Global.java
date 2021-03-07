package com.example.movingaverage;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;

public class Global {
    public static String mOne;
    public static String mTwo;
    public static String orderTimeInForce = "FILL_OR_KILL";
    public static double quant;
    public static String close = null;
    public static HttpRequestFactory requestFactory
        = new NetHttpTransport().createRequestFactory();

}
