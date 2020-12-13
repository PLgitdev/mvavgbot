package com.example.movingaverage;

import Controller.MongoCRUD;
import Live.DataFetch;
import Model.Price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

public class Main {

    public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
         MongoCRUD mongoCRUD = MongoCRUD.getInstance();
         Map<?, ?> liveMarketData;
         String [] marketSplit;
         DataFetch fetcher;
         String markets = "";
         int inputL;
         int inputL2;
         String inputS;
         LocalDateTime start = LocalDateTime.now();
         boolean buyMode = true;

        System.out.println("Please enter markets separated by comma, or clear");
        while (!"clear".equalsIgnoreCase(markets)) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                String mOne = marketSplit[0].toUpperCase();
                String mTwo = marketSplit[1].toUpperCase();
                fetcher = DataFetch.getInstance(mOne,mTwo);
                if (fetcher.valid()) {
                    try {
                        System.out.println("Welcome please enter a candle length" +
                            " 0 = MINUTE_1, 1 = MINUTE_5, 2 = HOUR_1, 3 = DAY_1");
                        int len = sc.nextInt();
                        String l = "";
                        switch (len % 10) {
                            case 0: l = "MINUTE_1";
                                break;
                            case 1: l = "MINUTE_5";
                                break;
                            case 2: l = "HOUR_1";
                                break;
                            case 3: l = "DAY_1";
                                break;
                        }
                        ArrayList<Map<?, ?>> historicalData = fetcher.historicalDataFetcher(l);
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, "historicaldata"));
                        System.out.println("Please enter day count for the short moving avg up to 365 days");
                        inputL = sc.nextInt();
                        System.out.println("Please enter day count for the int moving avg up to one year, 365 days");
                        inputL2 = sc.nextInt();
                        System.out.println("Please enter a calculation strategy high-low = 0, open-close = 1, close = 2");
                        inputS = sc.next();
                        ArrayList<Map<?, ?>> shorterDaysDataClose;
                        ArrayList<Map<?, ?>> shorterDaysDataOpen;
                        ArrayList<Map<?, ?>> longerDaysDataClose;
                        ArrayList<Map<?, ?>> longerDaysDataOpen;
                        ArrayList<Double> shortMacdPeriod = new ArrayList<>();
                        ArrayList<Double> longerMacdPeriod = new ArrayList<>();
                        ArrayList<Double> nineDayMACDPeriod = new ArrayList<>();
                        ArrayList<Double> shorterDaysDataOpenD = new ArrayList<>();
                        ArrayList<Double> shorterDaysDataCloseD = new ArrayList<>();
                        ArrayList<Double> longerDaysDataOpenD = new ArrayList<>();
                        ArrayList<Double> longerDaysDataCloseD = new ArrayList<>();
                        ArrayList<Double> pricesS = new ArrayList<>();
                        ArrayList<Double> pricesL = new ArrayList<>();

                        switch (inputS){
                            case "0":
                                ArrayList<Map<?, ?>> shorterDaysDataHigh = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL - 1,
                                        "startsAt",
                                        "high");
                                ArrayList<Map<?, ?>> shorterDaysDataLow = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL - 1,
                                        "startsAt",
                                        "low");
                                ArrayList<Map<?, ?>> longerDaysDataHigh = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2 - 1,
                                        "startsAt",
                                        "high");
                                ArrayList<Map<?, ?>> longerDaysDataLow = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2 - 1,
                                        "startsAt",
                                        "low");
                                ArrayList<Double> shorterDaysDataHighD =  new ArrayList<>();
                                ArrayList<Double> shorterDaysDataLowD = new ArrayList<>();
                                ArrayList<Double> longerDaysDataHighD =  new ArrayList<>();
                                ArrayList<Double> longerDaysDataLowD = new ArrayList<>();
                                shorterDaysDataHigh.forEach((map) ->
                                    shorterDaysDataHighD.add(Double.valueOf((String) map.get("high"))));
                                shorterDaysDataLow.forEach((map) ->
                                    shorterDaysDataLowD.add(Double.valueOf((String) map.get("low"))));
                                longerDaysDataHigh.forEach((map) ->
                                    longerDaysDataHighD.add(Double.valueOf((String) map.get("high"))));
                                longerDaysDataLow.forEach((map) ->
                                    longerDaysDataLowD.add(Double.valueOf((String) map.get("low"))));
                                takeAvg(shorterDaysDataHigh,shorterDaysDataHighD,shorterDaysDataLowD,pricesS);
                                takeAvg(longerDaysDataHigh,longerDaysDataHighD,longerDaysDataLowD, pricesL);
                                break;
                            case "1":
                                shorterDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL-1,
                                        "startsAt",
                                        "close"
                                    );
                                 shorterDaysDataOpen = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL-1,
                                        "startsAt",
                                        "open"
                                    );
                                longerDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2-1,
                                        "startsAt",
                                        "close"
                                    );
                                longerDaysDataOpen = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2-1,
                                        "startsAt",
                                        "open"
                                    );
                                shorterDaysDataOpen.forEach((map) ->
                                    shorterDaysDataOpenD.add(Double.valueOf((String) map.get("open"))));
                                shorterDaysDataClose.forEach((map) ->
                                    shorterDaysDataCloseD.add(Double.valueOf((String) map.get("close"))));
                                longerDaysDataOpen.forEach((map) ->
                                    longerDaysDataOpenD.add(Double.valueOf((String) map.get("open"))));
                                longerDaysDataClose.forEach((map) ->
                                    longerDaysDataCloseD.add(Double.valueOf((String) map.get("close"))));
                                takeAvg(shorterDaysDataOpen,shorterDaysDataOpenD,shorterDaysDataCloseD,pricesS);
                                takeAvg(longerDaysDataOpen,longerDaysDataOpenD,longerDaysDataCloseD,pricesL);
                                break;
                            case "2":
                                shorterDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL-1,
                                        "startsAt",
                                        "close"
                                    );
                                longerDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2-1,
                                        "startsAt",
                                        "close"
                                    );
                                shorterDaysDataClose.forEach((map) ->
                                    pricesS.add(Double.valueOf((String) map.get("close"))));
                                longerDaysDataClose.forEach((map) ->
                                    pricesL.add(Double.valueOf((String) map.get("close"))));
                                break;

                        }
                        // fixed macd on a set period. no matter the strategy the macd will be taken from a days period

                        List<Map<?,?>> nineDayPeriod = mongoCRUD.retrieveMarketDataByDays("historicaldata",
                            9,
                            "startsAt",
                            "close" );
                        List<Map<?,?>> twelveDayPeriod = mongoCRUD.retrieveMarketDataByDays("historicaldata",
                            12,
                            "startsAt",
                            "close" );
                        List<Map<?,?>> twentySixDayPeriod = mongoCRUD.retrieveMarketDataByDays("historicaldata",
                            26,
                            "startsAt",
                            "close" );
                        nineDayPeriod.forEach((map) ->
                            nineDayMACDPeriod.add(Double.parseDouble((String) map.get("close"))));
                        twelveDayPeriod.forEach((map) ->
                            shortMacdPeriod.add(Double.parseDouble((String) map.get("close"))));
                        twentySixDayPeriod.forEach((map) ->
                            longerMacdPeriod.add(Double.parseDouble((String) map.get("close"))));
                        Price priceObj = Price.builder().priceShorter(pricesS)
                            .priceLonger(pricesL)
                            .smoothing(2.0)
                            .nineDaysOfClose(nineDayMACDPeriod)
                            .shortMACDPeriod(shortMacdPeriod)
                            .longerMACDPeriod(longerMacdPeriod)
                            .twelveDayRibbons(new ArrayList<>(0))
                            .twentySixDayRibbons(new ArrayList<>(0))
                            .signalLine(new ArrayList<>(0))
                            .timestamp(LocalDateTime.now())
                            .dateLimit(LocalDateTime.now().plusHours(24))
                            .build();
                        priceObj.initializeSignalLine();
                        BigDecimal buy = new BigDecimal(0);
                        while (!markets.equalsIgnoreCase("clear")) {
                            liveMarketData = fetcher.marketDataFetcher();
                            ArrayList<?> result = (ArrayList<?>) liveMarketData.get("result");
                            Map<?, ?> resultM = (Map<?, ?>) result.get(0);
                            priceObj.setPrices((Double) resultM.get("Last"));
                            priceObj.setSMA();
                            priceObj.setSMACDEMA();
                            priceObj.setLMACDEMA();
                            priceObj.setMACD();
                            priceObj.updateSignalLine();
                            mongoCRUD.createMarketData(resultM, "marketsummary");
                            // resultM.forEach( (key,value) -> System.out.println(key + ":"+  value));
                            System.out.println(resultM.get("Last"));
                            //check average inequality
                            if(priceObj.validSMACrossover()) {
                                System.out.println(("valid SMA crossover "));
                                if(priceObj.validMACDCrossover() && buyMode) {
                                    buy = BigDecimal.valueOf(Double.valueOf(resultM.get("Bid").toString()));
                                    buy = buy.add(BigDecimal.valueOf(0.00000005));
                                    buy = buy.setScale(8, RoundingMode.HALF_UP);
                                    // we need to make sure transaction went through to continue to sell mode
                                    //stoploss at 3%
                                    System.out.println("BUY at " + buy);
                                    buyMode = false;
                                    resultM.forEach( (key,value) -> System.out.println(key + ":"+  value));
                                }
                                if(priceObj.validMACDBackCross() && !buyMode ||
                                    priceObj.validSMABackCross() && !buyMode) {
                                    //and successful buy
                                    //if MACD crosses without successful buy reset to buy mode
                                    //does it cancel current buy?
                                    //scale profits ??? buy object sell object needed
                                    //grab someones order out of the order book
                                    //if sell mode and no response after 4 percent increase in price then cancel buy
                                    BigDecimal sellMultiplier = BigDecimal.valueOf(.03);
                                    BigDecimal sell = BigDecimal.valueOf(Double.valueOf(resultM.get("Ask").toString()));
                                    sell = sell.subtract(BigDecimal.valueOf(0.00000010));
                                    // send new sells every time sell.subtract(BigDecimal.valueOf(0.00000001));
                                    //sell multiplier should be related to volume
                                    //if volume increases more than at the time of buy increase multiplier
                                    //get out at bid or get out at scale option
                                    //ask - an amount to try to get off the sale or bid plus amount
                                    //bid + an amount
                                    sell = sell.setScale(8, RoundingMode.HALF_UP);
                                    System.out.println("\n" + "Sell at " + sell);
                                   buyMode = true;
                                }
                            }
                            //send a buy request then either scale profits or sell at crossover
                            //check out v1 and look at buy request as well as the profit scaling
                            //make buy order a limit buy that is a little less than the target. (safety)
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

    public static void takeAvg (ArrayList<Map<?, ?>> maps,
                                ArrayList<Double> arOne, ArrayList<Double> arTwo,
                                ArrayList<Double> prices) {
        for (int i = 0; i < maps.size(); i++) {
            prices.add((arOne.get(i) + arTwo.get(i)) / 2);
        }
    }
}
//take the avg of open and close
                                /*for (int i = 0; i < shorterDaysDataOpen.size(); i++) {
                                    pricesS.add((shorterDaysDataOpenD.get(i) + shorterDaysDataCloseD.get(i)) / 2);
                                } for (int i = 0; i < longerDaysDataOpen.size(); i++) {
                                    pricesL.add((longerDaysDataOpenD.get(i) + longerDaysDataCloseD.get(i)) / 2);
                                }
                                */
