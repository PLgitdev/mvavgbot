package com.example.movingaverage.session;

import com.example.movingaverage.Live.DataFetch;
import com.example.movingaverage.Model.Price;

import java.util.Map;

public class PriceObjectSession {
    public static int candleLength;
    public static int shortDaysInput;
    public static int longDaysInput;
    public static String calcStratInput;
    public static DataFetch sessionFetcher;
    public static boolean buyMode;
    public static Price currentPriceObject;
}
