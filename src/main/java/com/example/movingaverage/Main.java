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
        while (!"stop".equalsIgnoreCase(markets)) {
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
                        ArrayList<Map<?, ?>> shorterDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", inputL, "startsAt", "close");
                        System.out.println("Please enter start day for the long moving avg up to one year, 365 days");
                        inputL2 = sc.nextLong();
                        ArrayList<Map<?, ?>> longerDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata", inputL2, "startsAt", "close");
                        ArrayList<Double> pricesS = new ArrayList<>();
                        shorterDaysData.forEach( (map) -> pricesS.add(Double.valueOf((String) map.get("close"))));
                        ArrayList<Double>  pricesL = new ArrayList<>();
                        longerDaysData.forEach( (map)-> pricesL.add(Double.valueOf((String) map.get("close"))));
                        Price priceObj = Price.builder().priceShorter(pricesS)
                            .priceLonger(pricesL)
                            .totalShorter(0.0).totalLonger(0.0)
                            .timestamp(LocalDateTime.now())
                            .dateLimit(LocalDateTime.now().plusDays(24).getNano())
                            .build();
                        while (true) {
                            liveMarketData = fetcher.marketDataFetcher();
                            ArrayList<?> result = (ArrayList<?>) liveMarketData.get("result");
                            Map<?, ?> resultM = (Map<?, ?>) result.get(0);
                            System.out.println(resultM.toString());
                            mongoCRUD.createMarketData(resultM, "marketsummary");
                            priceObj.addPrice((Double) resultM.get("Last"));
                            //check average inequality
                            if (priceObj.validBuyCrossover()) {
                                System.out.println("BUY at " + priceObj.getPrice());

                            }
                            //reset the historical data
                            if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(inputL))) {
                                priceObj.getPriceShorter().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays("marketSummary",
                                        inputL, "Timestamp","Last").forEach( (data) -> priceObj
                                            .addPriceShorter((Double) data.get("Last")));
                            if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(inputL2)))
                                priceObj.getPriceLonger().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays("marketSummary",
                                        inputL2, "Timestamp","Last").forEach( (data) -> priceObj
                                            .addPriceLonger((Double)(data.get("Last"))));
                                priceObj.setTimestamp(LocalDateTime.now());

                            priceObj.dateLimitCheck();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mongoCRUD.deleteAllMarketData("marketsummary");
                    mongoCRUD.deleteAllMarketData("historicaldata");
                    break;
                    } else {
                    System.out.println("Market entry invalid, please try again");
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("You have entered an entry too short, or have forgotten a comma" +
                    ", please enter your market");
                e.printStackTrace();
            }
            mongoCRUD.deleteAllMarketData("marketsummary");
            mongoCRUD.deleteAllMarketData("historicaldata");
        }
    }
}

