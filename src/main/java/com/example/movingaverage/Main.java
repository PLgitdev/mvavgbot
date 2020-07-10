package com.example.movingaverage;

import Controller.MongoCRUD;
import Live.DataFetch;
import Model.Price;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
         MongoCRUD mongoCRUD = MongoCRUD.getInstance();
         Map<?, ?> liveMarketData;
         String [] marketSplit;
         DataFetch fetcher;
         String markets = "";
         Long inputL;

        System.out.println("Please enter markets separated by comma, or stop");
        while (!markets.equalsIgnoreCase("stop")) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                String mOne = marketSplit[0].toUpperCase();
                String mTwo = marketSplit[1].toUpperCase();
                fetcher = DataFetch.getInstance(mOne,mTwo);
                if (fetcher.valid()) {
                    //grab all needed historical data
                    try {
                        ArrayList<Map<?, ?>> historicalData = fetcher.historicalDataFetcher();
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, "historicaldata"));
                        System.out.println("Please enter days for the short moving avg");
                        inputL = sc.nextLong();
                        ArrayList<Map<?, ?>> thirtyDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", inputL);
                        System.out.println("Please enter days for the long moving avg");
                        inputL = sc.nextLong();
                        ArrayList<Map<?, ?>> ninetyDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", inputL);
                        ArrayList<Float> prices30 = new ArrayList<>();
                        thirtyDaysData.forEach( (day)
                            -> prices30.add(Float.parseFloat(day.values().toString())
                        ));

                        ArrayList<Float>  prices90 = new ArrayList<>();
                        ninetyDaysData.forEach( (day)
                            -> prices90.add(Float.parseFloat(day.values().toString()))
                        );
                        //create price object
                        Price priceObj = Price.builder().price30(prices30)
                            .price90(prices90)
                            .build();
                        while (true) {
                            liveMarketData = fetcher.marketDataFetcher();
                            ArrayList<?> result = (ArrayList<?>) liveMarketData.get("result");
                            Map<?, ?> resultM = (Map<?, ?>) result.get(0);
                            mongoCRUD.createMarketData(resultM, "marketsummary");
                            priceObj.addPrice((Float) resultM.get("Last"));
                            //check average inequality
                            if (priceObj.validBuyCrossover()) {

                            }
                            priceObj.dateLimitCheck();
                        }
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

