package com.example.movingaverage;

import Controller.MongoCRUD;
import Live.DataFetch;

import java.util.*;

public class Main {

    public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
         MongoCRUD mongoCRUD = MongoCRUD.getInstance();
         Map<?, ?> liveMarketData;
         String [] marketSplit;
         DataFetch fetcher;
         String markets = "";

        System.out.println("Please enter markets separated by comma, or stop");
        while (!markets.equalsIgnoreCase("stop")) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                String mOne = marketSplit[0].toUpperCase();
                String mTwo = marketSplit[1].toUpperCase();
                fetcher = DataFetch.getInstance(mOne,mTwo);
                if (fetcher.valid()) {
                    try {
                        ArrayList<Map<?, ?>> historicalData = fetcher.historicalDataFetcher();
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, "historicaldata"));
                        ArrayList thirtyDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", (long) 30);
                        ArrayList ninetyDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", (long) 90);
                        liveMarketData = fetcher.marketDataFetcher();
                        ArrayList<?> result = (ArrayList<?>) liveMarketData.get("result");
                        Map<?, ?> resultM = (Map<?, ?>) result.get(0);
                        mongoCRUD.createMarketData(resultM, "marketsummary");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mongoCRUD.deleteAllMarketData("marketsummary", mOne, mTwo);
                    break;
                } else {
                    System.out.println("Market entry invalid, please try again");
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("You have entered an entry too short, or have forgotten a comma" +
                    ", please enter your market");
                e.printStackTrace();
            }
        }
    }
}

