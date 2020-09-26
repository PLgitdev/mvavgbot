package com.example.movingaverage;

import Controller.MongoCRUD;
import Live.DataFetch;
import Model.Price;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
         LocalDateTime start = LocalDateTime.now();
         boolean buyMode = true;

        System.out.println("Please enter markets separated by comma, or stop");
        while (!"stop".equalsIgnoreCase(markets)) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                String mOne = marketSplit[0].toUpperCase();
                String mTwo = marketSplit[1].toUpperCase();
                fetcher = DataFetch.getInstance(mOne,mTwo);
                ArrayList<Double> pricesS = new ArrayList<>();
                ArrayList<Double>  pricesL = new ArrayList<>();
                if (fetcher.valid()) {
                    //grab all needed historical data
                    try {
                        ArrayList<LinkedHashMap<?, ?>> historicalData = fetcher.historicalDataFetcher();
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, "historicaldata"));
                        System.out.println("Please enter start day for the short moving avg");
                        inputL = sc.nextLong();
                        ArrayList<Map<?, ?>> shorterDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata",
                                inputL-1,
                                "startsAt",
                                "close"
                            );
                        System.out.println("Please enter start day for the long moving avg up to one year, 365 days");
                        inputL2 = sc.nextLong();
                        ArrayList<Map<?, ?>> longerDaysData = mongoCRUD
                            .retrieveMarketDataByDays("historicaldata",
                                inputL2-1,
                                "startsAt",
                                "close"
                            );
                        shorterDaysData.forEach( (map) -> pricesS.add(Double.valueOf((String) map.get("close"))));
                        longerDaysData.forEach( (map)-> pricesL.add(Double.valueOf((String) map.get("close"))));
                        Price priceObj = Price.builder().priceShorter(pricesS)
                            .priceLonger(pricesL)
                            .totalShorter(0.0).totalLonger(0.0)
                            .timestamp(LocalDateTime.now())
                            .dateLimit(LocalDateTime.now().plusHours(24))
                            .build();
                        while (buyMode) {
                            liveMarketData = fetcher.marketDataFetcher();
                            ArrayList<?> result = (ArrayList<?>) liveMarketData.get("result");
                            Map<?, ?> resultM = (Map<?, ?>) result.get(0);
                            resultM.forEach( (key,value) -> System.out.println(key + ":"+  value));
                            System.out.println(inputL + " day avg, shorter:" + priceObj.getAvgShorter());
                            System.out.println(inputL2 + " day avg, longer:" + priceObj.getAvgLonger());
                            mongoCRUD.createMarketData(resultM, "marketsummary");
                            priceObj.addBothPrices((Double) resultM.get("Last"));
                            //check average inequality
                            if (priceObj.validBuyCrossover()) {
                                System.out.println("\n" + "BUY at "
                                    + priceObj.getPriceLonger().get(priceObj.getPriceLonger().size() - 1));
                                buyMode = false;
                                //send a buy request then either scale profits or sell at crossover
                                //check out v1 and look at buy request as well as the profit scaling
                                //check out new version of api as well and decide if you want to use that
                            }

                            //reset the historical data
                            if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(inputL))) {
                                priceObj.getPriceShorter().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays("marketsummary",
                                        inputL-1,
                                        "TimeStamp",
                                        "Last").forEach((data) -> priceObj
                                            .addPriceShorter((Double) data.get("Last")));
                            }
                            if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(inputL2))) {
                                priceObj.getPriceLonger().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays("marketsummary",
                                        inputL2-1,
                                        "TimeStamp",
                                        "Last").forEach((data) -> priceObj
                                            .addPriceLonger((Double) (data.get("Last"))));
                                priceObj.setTimestamp(LocalDateTime.now());
                            }
                            //if it has not reset fully then subtract 1 a day
                            if (!start.equals(start.plusDays(inputL))) { priceObj.dateLimitCheck(1); }
                            if (!start.equals(start.plusDays(inputL2))) { priceObj.dateLimitCheckLonger(1); }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //drop database
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
            } finally {
                //drop database
                mongoCRUD.deleteAllMarketData("marketsummary");
                mongoCRUD.deleteAllMarketData("historicaldata");
            }
        }
    }
}

