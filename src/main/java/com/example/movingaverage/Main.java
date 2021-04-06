package com.example.movingaverage;

import com.example.movingaverage.DAO.MongoCRUD;
import com.example.movingaverage.Live.Buy;
import com.example.movingaverage.Live.DataFetch;
import com.example.movingaverage.Live.Sell;
import com.example.movingaverage.Live.Transaction;
import com.example.movingaverage.Model.Price;
import lombok.Builder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
 /*  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 This program is an automated trading algorithm with a modular strategy based on common financial indicators.
 In this version the deciding indicators are the moving average convergence divergence, commonly known as MACD and
 the signal line which both are derived from historical data. These EMA or exponential moving averages are
 derived from the simple moving average determined by time period of historical data you select to use for analysis.
 The simple moving average is fundamental in calculating this weighted average which determines the buy sell decisions
 of the program. This program uses smoothing of 2.0 for this EMA multiplier which is a standard.
 *Caution this bot runs on a model currently that is preset for an optimal entry point in the market.
 if you feel like forking the project you would most likely want to add RSI
 this project is currently adding an indicator RSI which will extend the period of time the bot can be left
 unmonitored and reduce the amount of experience necessary with trading to make profit.


 - roundSquare

 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

public class Main {

    public static void main(String[] args) {
         Scanner sc = new Scanner(System.in);
         MongoCRUD mongoCRUD = MongoCRUD.getInstance();
         Map<Object, Object> liveMarketData;
         String [] marketSplit;
         DataFetch fetcher;
         String markets = "";
         int inputL;
         int inputL2;
         String inputS;

         BigDecimal profit;

         LocalDateTime start = LocalDateTime.now();
         Global.quant = .0400000;

        System.out.println("Please enter markets separated by comma, or clear");
        while (!"clear".equalsIgnoreCase(markets)) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                Global.mOne = marketSplit[0].toUpperCase();
                Global.mTwo = marketSplit[1].toUpperCase();
                int candleLengthM = Global.rateLimit / 1000;
                fetcher = DataFetch.getInstance();
                if (fetcher.valid()) {
                    try {
                        System.out.println("Welcome please enter a candle length" +
                            " 0 = MINUTE_1, 1 = MINUTE_5, 2 = HOUR_1, 3 = DAY_1");
                        // Select candle length using console input Integers 0-3
                        Global.len = sc.nextInt();
                        String l;
                        switch (Global.len) {
                            case 0:
                                l = "MINUTE_1";
                                Global.candleLength = 60 * candleLengthM;
                                break;
                            case 1:
                                l = "MINUTE_5";
                                Global.candleLength = (60 * 5) * candleLengthM;
                                break;
                            case 2:
                                l = "HOUR_1";
                                Global.candleLength = (60 * 60) * candleLengthM;
                                break;
                            case 3:
                                l = "DAY_1";
                                Global.candleLength = 86400 * candleLengthM;
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                        // Fetch historical data
                        ArrayList<Map<Object,Object>> historicalData = fetcher.historicalDataFetcher(l);
                        //rate limit is dynamic be careful adjusting Thread.sleep
                        Thread.sleep(Global.rateLimit);

                        //save it to the db
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, Global.HISTORICAL_DATA));

                        System.out.println("Please enter day count for the short moving avg up to 365 days");
                        inputL = sc.nextInt();

                        System.out.println("Please enter day count for the int moving avg up to one year, 365 days");
                        inputL2 = sc.nextInt();

                        System.out.println("Please enter a calculation strategy high-low = 0, open-close = 1, " +
                            "close = 2");
                        inputS = sc.next();
                        /* The Map<String,String> requires us to loop the through the value of the map to cast
                         them using Wrapper class Double .valueOf method. The resulting values will be placed into
                         an ArrayList then used to build the priceObj. The calculation used to determine the values
                         added to the priceObj will be decided by your previous inputS.

                         After the priceObject is built it will be initialized.
                         */

                        Price.PriceBuilder priceBuilder = Price.builder().smoothing(2.0);
                        ArrayList<Double> shortMACDPeriod = new ArrayList<>();
                        ArrayList<Double> longerMACDPeriod = new ArrayList<>();
                        ArrayList<Double> nineDayMACDPeriod = new ArrayList<>();
                        ArrayList<Double> shorterDaysDataOpenD = new ArrayList<>();
                        ArrayList<Double> shorterDaysDataCloseD = new ArrayList<>();
                        ArrayList<Double> longerDaysDataOpenD = new ArrayList<>();
                        ArrayList<Double> longerDaysDataCloseD = new ArrayList<>();

                        switch (inputS) {
                            case "0":
                                List<Map<?, ?>> shorterDaysDataHigh = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL - 1,
                                        "startsAt",
                                        "high");
                                List<Map<?, ?>> shorterDaysDataLow = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL - 1,
                                        "startsAt",
                                        "low");
                                List<Map<?, ?>> longerDaysDataHigh = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL2 - 1,
                                        "startsAt",
                                        "high");
                                List<Map<?, ?>> longerDaysDataLow = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL2 - 1,
                                        "startsAt",
                                        "low");

                                // Has to be a better way to do this parsing on another day
                                ArrayList<Double> shorterDaysDataHighD = new ArrayList<>();
                                ArrayList<Double> shorterDaysDataLowD = new ArrayList<>();
                                ArrayList<Double> longerDaysDataHighD = new ArrayList<>();
                                ArrayList<Double> longerDaysDataLowD = new ArrayList<>();
                                shorterDaysDataHigh.forEach((map) ->
                                    shorterDaysDataHighD.add(Double.valueOf((String) map.get("high"))));
                                shorterDaysDataLow.forEach((map) ->
                                    shorterDaysDataLowD.add(Double.valueOf((String) map.get("low"))));
                                longerDaysDataHigh.forEach((map) ->
                                    longerDaysDataHighD.add(Double.valueOf((String) map.get("high"))));
                                longerDaysDataLow.forEach((map) ->
                                    longerDaysDataLowD.add(Double.valueOf((String) map.get("low"))));
                                priceBuilder.priceShorter(takeAvg(shorterDaysDataHigh,
                                    shorterDaysDataHighD, shorterDaysDataLowD));
                                priceBuilder.priceLonger(takeAvg(longerDaysDataHigh,
                                    longerDaysDataHighD, longerDaysDataLowD));
                                break;
                            case "1":
                                List<Map<?, ?>> shorterDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL - 1,
                                        "startsAt",
                                        "close");
                                List<Map<?, ?>> shorterDaysDataOpen = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL - 1,
                                        "startsAt",
                                        "open"
                                    );
                                List<Map<?, ?>> longerDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL2 - 1,
                                        "startsAt",
                                        "close"
                                    );
                                List<Map<?, ?>> longerDaysDataOpen = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL2 - 1,
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
                                priceBuilder.priceShorter(takeAvg(shorterDaysDataOpen, shorterDaysDataOpenD, shorterDaysDataCloseD));
                                priceBuilder.priceLonger(takeAvg(longerDaysDataOpen, longerDaysDataOpenD, longerDaysDataCloseD));
                                break;
                            case "2":
                                List<Double> pricesS = new ArrayList<>();
                                List<Double> pricesL = new ArrayList<>();
                                shorterDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL - 1,
                                        "startsAt",
                                        "close"
                                    );
                                longerDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                                        inputL2 - 1,
                                        "startsAt",
                                        "close"
                                    );
                                shorterDaysDataClose.forEach((map) ->
                                    pricesS.add(Double.valueOf((String) map.get("close"))));
                                longerDaysDataClose.forEach((map) ->
                                    pricesL.add(Double.valueOf((String) map.get("close"))));
                                priceBuilder.priceShorter(pricesS);
                                priceBuilder.priceLonger(pricesL);
                                break;
                        }

                        List<Map<?, ?>> nineDayPeriod = mongoCRUD.retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                            9,
                            "startsAt",
                            "close");
                        List<Map<?, ?>> twelveDayPeriod = mongoCRUD.retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                            12,
                            "startsAt",
                            "close");
                        List<Map<?, ?>> twentySixDayPeriod = mongoCRUD.retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                            26,
                            "startsAt",
                            "close");

                        nineDayPeriod.forEach((map) ->
                            nineDayMACDPeriod.add(Double.parseDouble((String) map.get("close"))));
                        twelveDayPeriod.forEach((map) ->
                            shortMACDPeriod.add(Double.parseDouble((String) map.get("close"))));
                        twentySixDayPeriod.forEach((map) ->
                            longerMACDPeriod.add(Double.parseDouble((String) map.get("close"))));

                        // Finish building the priceObj and init
                        Price priceObj = priceBuilder.nineDaysOfClose(nineDayMACDPeriod)
                            .shortMACDPeriod(shortMACDPeriod)
                            .longerMACDPeriod(longerMACDPeriod)
                            .twelveDayRibbons(new ArrayList<>(0))
                            .twentySixDayRibbons(new ArrayList<>(0))
                            .signalLine(new ArrayList<>(0))
                            .timestamp(LocalDateTime.now())
                            .dateLimit(LocalDateTime.now().plusHours(24)).build();
                        priceObj.init();
                        BigDecimal buy = new BigDecimal(0);
                        BigDecimal sell; // If it says not initialized try setting to zero
                        double profitPercentageTotals = 0.0;
                        boolean successfulBuy = false;
                        boolean sellBidMode = true;
                        boolean buyBidMode = false;
                        boolean hold;
                        //Loop to poll for market data
                        while (!markets.equalsIgnoreCase("clear")) {
                            // Fetch the data
                            liveMarketData = fetcher.marketDataFetcher();
                            Thread.sleep(Global.rateLimit);

                            // Set values to the price object
                            priceObj.setPrices(Double.valueOf(liveMarketData.get("Last").toString()));

                            // If the incoming size reaches a factor of a candle length create a candle
                            if(priceObj.getPriceLonger().size() % Global.candleLength == 0 &&
                                priceObj.getPriceShorter().size() % Global.candleLength == 0) {
                                createCandle(priceObj);
                                System.out.println("Candle created: \n" + priceObj.toString());
                            }
                            mongoCRUD.createMarketData(liveMarketData, Global.MARKET_SUMMARY);

                            /*  If you want to check every iteration
                              liveMarketData.forEach( (key,value) -> System.out.println(key + ":"+  value)); */

                            // Might have to go back to Wrappers after live testing
                            double lastDouble = Double.parseDouble(liveMarketData.get("Last").toString());
                            double askDouble = Double.parseDouble(liveMarketData.get("Ask").toString());
                            double bidDouble = Double.parseDouble(liveMarketData.get("Bid").toString());
                            System.out.println(liveMarketData.get("Last") + "\n" +
                                "Total percentage gain/loss : " + profitPercentageTotals + "\n" + "Bank : "
                                + (Global.quant + (Global.quant * (profitPercentageTotals) / 100d)));
                            //check average inequality
                            boolean buyMode = priceObj.validMACDCrossover();
                            System.out.println(buyMode);
                            int responseCode = 0;
                            if (buyMode && !successfulBuy) {
                                if (askDouble <= lastDouble) {
                                    buy = BigDecimal.valueOf(askDouble);
                                    System.out.println("Take the ask at " + buy);
                                    try {
                                        HttpResponse<String> response
                                            = sendOrder(createOrder(buy.doubleValue(), "BUY"));
                                        responseCode = response.statusCode();
                                    }
                                    catch (IOException e) {
                                        System.out.println("IO Exception : " + e + "\n" + "response: " + responseCode);
                                    }
                                }
                                else {
                                    buy = BigDecimal.valueOf(bidDouble);
                                    buyBidMode = true;
                                }
                                System.out.println("BUY at " + buy);
                                liveMarketData.forEach((key, value) -> System.out.println(key + ":" + value));
                                //maybe use response = 0?
                                if (buyBidMode && responseCode != 201) {
                                    buy = buy.setScale(8, RoundingMode.HALF_UP);
                                    try {
                                        if (buy.doubleValue() >= askDouble) {
                                            buy = BigDecimal.valueOf(askDouble).add(BigDecimal.valueOf(0.00000002));
                                            System.out.println("add to the ask for " + buy);
                                        }
                                        if (buy.doubleValue() > lastDouble) {
                                            buy = BigDecimal.valueOf(lastDouble)
                                                .add(BigDecimal.valueOf(0.00000002));
                                            System.out.println("Take the last at " + buy);
                                        } else {
                                            buy = BigDecimal.valueOf(askDouble);
                                            System.out.println("Take the ask at " + buy);
                                        }

                                        HttpResponse<String> response
                                            = sendOrder(createOrder(buy.doubleValue(), "BUY"));
                                        responseCode = response.statusCode();
                                    } catch (IOException e) {
                                        System.out.print("There was an IOException " + e + "\n" + "response : " +
                                            responseCode);
                                    }
                                }
                                if(responseCode == 201) {
                                        //buyMode = false;
                                    successfulBuy = true;
                                    buyBidMode = false;
                                        System.out.println("Successful Buy 201 at " + buy + "\n" +
                                            "this is the response " + responseCode);
                                        responseCode = 0;
                                }
                            }
                            if (successfulBuy && lastDouble <
                                //sensitivity
                                buy.subtract(buy.multiply(BigDecimal.valueOf(0.025))).doubleValue()) {
                                sell = BigDecimal.valueOf(bidDouble);
                                try {
                                    HttpResponse<String> response = sendOrder(sellRoutine(sell, bidDouble));
                                    responseCode = response.statusCode();
                                }
                                catch (IOException e) {
                                    System.out.print("There was an IOException " + e + "\n" + "response : " +
                                        responseCode);

                                }
                                sellBidMode = false;
                                System.out.println("Sell exited because last price dropped to low");
                            }
                            else if (successfulBuy && sellBidMode) {
                                if (bidDouble < lastDouble) {
                                    hold = false;
                                    sell = BigDecimal.valueOf(lastDouble);
                                    sell = sell.subtract(BigDecimal.valueOf(.0000005));
                                    sellBidMode = true;
                                    System.out.println("Last was chosen then subtracted from");
                                }
                                else if (bidDouble > askDouble) {
                                    hold = false;
                                    sell = BigDecimal.valueOf(bidDouble);
                                    sellBidMode = true;
                                    System.out.println("Bid was chosen");
                                }
                                else {
                                    hold = false;
                                    sell = BigDecimal.valueOf(askDouble);
                                    sell = sell.subtract(BigDecimal.valueOf(.0000005));
                                    System.out.println("Ask was chosen then subtracted from");
                                }
                                if (sell.doubleValue() < buy.add(buy.multiply(BigDecimal.valueOf(.015)))
                                        .doubleValue() && sell.doubleValue() != 0) {
                                    hold = true;
                                    System.out.println("Hold missed sell wait due to not enough profit");
                                }
                                if (buyMode) {
                                    hold = true;
                                    System.out.println("buy signal not sell signal HOLD");
                                }
                                System.out.println("\n" + "Sell at " + sell + " vs bid " + liveMarketData.get("Bid"));
                                if (!hold) {
                                    // if the Bid is more than the last use the Last
                                    sell = sell.subtract(BigDecimal.valueOf(.00000001));
                                    sell = sell.setScale(8, RoundingMode.HALF_UP);
                                    //if no sell successful
                                    System.out.println("\n Cancel last sell and Sell at " + sell + " bid is " +
                                        liveMarketData.get("Bid"));
                                    try {
                                        HttpResponse<String> response = sendOrder(sellRoutine(sell, bidDouble));
                                        responseCode = response.statusCode();
                                    } catch (IOException e) {
                                        System.out.print("There was an IOException " + e + "\n response : " +
                                            responseCode);
                                    }
                                    if (responseCode == 201) {
                                        //? and valid MACDCrossover?
                                        profit = sell.subtract(buy);
                                        if (profit.doubleValue() > 0d) {
                                            profit = profit.divide(buy, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100.0));
                                        }
                                        profitPercentageTotals += profit.doubleValue();
                                        System.out.println("Sell successful at " + sell + " " + "profit percent : " +
                                            profit + "%" + "\n response: " + responseCode);
                                        sellBidMode = false;
                                        successfulBuy = false;
                                    }
                                }
                            }

                            //reset the historical data
                            if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(inputL))) {
                                priceObj.getPriceShorter().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays(Global.MARKET_SUMMARY,
                                        inputL-1,
                                        "TimeStamp",
                                        "Last").forEach((data) -> priceObj
                                            .addPriceShorter((Double) data.get("Last")));
                            }
                            if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(inputL2))) {
                                priceObj.getPriceLonger().clear();
                                mongoCRUD
                                    .retrieveMarketDataByDays(Global.MARKET_SUMMARY,
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
                    mongoCRUD.deleteAllMarketData(Global.MARKET_SUMMARY);
                    mongoCRUD.deleteAllMarketData(Global.HISTORICAL_DATA);
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

                mongoCRUD.deleteAllMarketData(Global.MARKET_SUMMARY);
                mongoCRUD.deleteAllMarketData(Global.HISTORICAL_DATA);
            }
        }
    }
  
    //Optimize this with a binary sum or statistics this needs to change
    public static List<Double> takeAvg(List<Map<?, ?>> maps,
                                        List<Double> arOne, List<Double> arTwo) {
        List<Double> avg = new LinkedList<>();
        for (int i = 0; i < maps.size(); i++) {
            avg.add((arOne.get(i) + arTwo.get(i)) / 2);
        }
        return avg;
    }

    public static Transaction createOrder(Double limit, String direction) throws MalformedURLException {
        return direction.equalsIgnoreCase("Buy") ?
            Buy.getInstance("LIMIT", limit, Global.orderTimeInForce, direction) :
            Sell.getInstance("LIMIT", limit, Global.orderTimeInForce, direction);
    }
    public static HttpResponse<String> sendOrder(Transaction order) throws IOException, InterruptedException {
        HttpResponse<String> response = order.send();
        if (response.statusCode() == 201) {
            System.out.println("Successful order");
        }
        if (response.statusCode() == 401)  {
            System.out.println("Unauthorized 401 body is" + response.body());
        }
        else {
            System.out.println("Response not 201: " + response);
        }
        Thread.sleep(1000);
        return response;
    }

    public static BigDecimal fixSell(Double bidDouble) {
        BigDecimal sell = BigDecimal.valueOf(bidDouble)
            .add(BigDecimal.valueOf(0.00000005));
        System.out.println("The sell was calculated lower than the bid, " + "\n" +
            "sell : " + sell);
        return sell;
    }

    public static Transaction sellRoutine(BigDecimal sell, Double bidDouble) throws IOException {
        if (sell.doubleValue() <  bidDouble) {
            BigDecimal fixedSell = fixSell(bidDouble).setScale(8, RoundingMode.HALF_UP);
            return createOrder(fixedSell.doubleValue(), "SELL");
        }
        return createOrder(sell.doubleValue(), "SELL");
    }

    public static void createCandle(Price priceObj) {
        priceObj.setSMA();
        priceObj.setSMACDEMA();
        priceObj.setLMACDEMA();
        priceObj.setMACD();
        priceObj.setSignalLine();
    }
}