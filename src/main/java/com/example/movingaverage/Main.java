package com.example.movingaverage;

import Controller.MongoCRUD;
import Live.DataFetch;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
         MongoCRUD mongoCRUD = MongoCRUD.getInstance();
         Map<?, ?> marketData;
         String [] marketSplit;
         DataFetch fetcher;
         String markets = "";

        System.out.println("Please enter markets separated by comma, or stop");
        while (!markets.equalsIgnoreCase("stop")) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                fetcher = DataFetch.getInstance(marketSplit[0].toUpperCase(),
                    marketSplit[1].toUpperCase());

                if (fetcher.valid()) {
                    try {
                        ArrayList<Map<?, ?>> historicalData = fetcher.historicalDataFetcher();
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, "historicaldata"));
                        marketData = fetcher.marketDataFetcher();
                        ArrayList<?> result = (ArrayList<?>) marketData.get("result");
                        Map<?, ?> resultM = (Map<?, ?>) result.get(0);
                        mongoCRUD.createMarketData(resultM, "marketsummary");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        }
    }
}

