package com.example.movingaverage.session;

import com.example.movingaverage.Live.DataFetch;
import com.example.movingaverage.Model.Price;

import java.util.Map;

public class PriceSession {
    public static String candleType;
    public static int candleLength;
    public static int shortDaysInput;
    public static int longDaysInput;
    public static String calcStratInput;
    public static DataFetch sessionFetcher;
    public static boolean buyMode;
    public static Price currentPriceObject;
    public static boolean successfulBuy;
    public static String mOne;
    public static String mTwo;
}
