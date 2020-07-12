package com.example.movingaverage;

import Controller.MongoCRUD;
import Live.DataFetch;
import Model.Price;

import java.time.LocalDateTime;
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
         long inputL;
         long inputL2;

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
                        System.out.println("Please enter start day for the short moving avg");
                        inputL = sc.nextLong();
                        ArrayList<Map<?, ?>> thirtyDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", inputL, "startsAt", "close");
                        System.out.println("Please enter start day for the long moving avg up to one year, 365 days");
                        inputL2 = sc.nextLong();
                        ArrayList<Map<?, ?>> ninetyDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", inputL2, "startsAt", "close");
                        ArrayList<Float> prices30 = new ArrayList<>();
                        thirtyDaysData.forEach( (day) -> prices30.add(Float.parseFloat(day.values().toString())));
                        ArrayList<Float>  prices90 = new ArrayList<>();
                        ninetyDaysData.forEach( (day)-> prices90.add(Float.parseFloat(day.values().toString())));
                        //create price object
                        Price priceObj = Price.builder().priceShorter(prices30)
                            .priceLonger(prices90)
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
                            if (LocalDateTime.now().equals(priceObj.getNow().plusDays(inputL))) {
                                priceObj.getPriceShorter().clear();
                                priceObj.getPriceLonger().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays("marketSummary",
                                        (long) inputL, "Timestamp","Last").forEach( (data) -> priceObj
                                    .addPriceShorter(Float.parseFloat(data.get("Last").toString())));
                                mongoCRUD
                                    .retrieveMarketDataByDays("marketSummary",
                                        (long) inputL2, "Timestamp","Last").forEach( (data) -> priceObj
                                    .addPriceLonger(Float.parseFloat(data.get("Last").toString())));
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

