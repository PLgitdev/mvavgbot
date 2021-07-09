package com.example.movingaverage;

import com.example.movingaverage.DAO.MongoCRUD;
import com.example.movingaverage.Live.Buy;
import com.example.movingaverage.Live.DataFetch;
import com.example.movingaverage.Live.Sell;
import com.example.movingaverage.Live.Transaction;
import com.example.movingaverage.Model.Price;
import com.example.movingaverage.session.PriceObjectSession;

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
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        LocalDateTime start = LocalDateTime.now();
        MongoCRUD mongoCRUD = MongoCRUD.getInstance();
        Map<Object, Object> liveMarketData;
        boolean runMode = false;
        //Global.quant = .0400000; hard coded to avoid accidental purchase

        System.out.println("The commands are as following; " +
                "\n boot : This will start the bot if the config is set " +
                "\n market\\s?.*=(\\w|\\D|\\S){2,6}$ : this will bind a market combination to a session.\n" +
                "\n sync\\s?.*(0-3)$ : this will set the length of the session candle length\n");
        // Use recursive function mayb?
        while (true) {
            // Run function
            Price.PriceBuilder priceBuilder = Price.builder().smoothing(1.0);
            // MACD build function
            mACDBuilder(priceBuilder, mongoCRUD);
            // Build Function
            priceBuilder.twelveDayRibbons(new ArrayList<>(0))
                    .twentySixDayRibbons(new ArrayList<>(0))
                    .signalLine(new ArrayList<>(0))
                    .timestamp(LocalDateTime.now())
                    .dateLimit(LocalDateTime.now().plusHours(24));
            Price priceObject = priceObjectBuild(priceBuilder);
            // Init object
            priceObject.init();
            // Run strategy
            //runStrategy();

            //to set up the configuration you will issue a series of commands these can be changed during runtime
            if (sc.hasNext("^boot$")) {
                boot(mongoCRUD, sc, priceBuilder);
            }
            // You are able to switch markets
            if (sc.hasNext("^market\\s?.*(\\w|\\D|\\S){2,6}$")) {
                dropDB(mongoCRUD);
                System.out.println("Please enter markets separated by comma, or clear");
                marketSelect(sc);
                // Calculate MACD
                PriceObjectSession.sessionFetcher = DataFetch.getNewInstance();
            }
            // You are able to switch candle sync modes
            if (sc.hasNext("^sync\\s?.*(0-3)$")) {
                try {
                    dropDB(mongoCRUD);
                    PriceObjectSession.calcStratInput = sc.toString().split("\\d")[0];
                    resetHistoricalData(mongoCRUD);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception on thread sleep" + Arrays.toString(e.getStackTrace()));
                }
                catch (IOException e){
                    System.out.println("Interrupted exception on thread sleep" + Arrays.toString(e.getStackTrace()));
                }
            }
        }
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

    public static void setIndicators(Price priceObj) {
        priceObj.setSMA();
        priceObj.setSMACDEMA();
        priceObj.setLMACDEMA();
        priceObj.setMACD();
        priceObj.setSignalLine();
    }

    public static String sync(String reSyncValue) {
        int candleLengthM = Global.rateLimit / 1000;
        String queryParameter;
        switch (reSyncValue) {
            case "0":
                queryParameter = "MINUTE_1";
                PriceObjectSession.candleLength = 60 * candleLengthM;
                break;
            case "1":
                queryParameter = "MINUTE_5";
                PriceObjectSession.candleLength = (60 * 5) * candleLengthM;
                break;
            case "2":
                queryParameter = "HOUR_1";
                PriceObjectSession.candleLength = (60 * 60) * candleLengthM;
                break;
            case "3":
                queryParameter = "DAY_1";
                PriceObjectSession.candleLength = 86400 * candleLengthM;
                break;
            default:
                throw new IllegalArgumentException();
        }
        // Fetch historical data
        //return this function callback
        return queryParameter;
    }
    public static ArrayList<Map<Object, Object>> fetchHistoricalDataByMarket(DataFetch fetcher, String queryParam) throws InterruptedException, IOException {
        Thread.sleep(Global.rateLimit);
        return fetcher.historicalDataFetcher(queryParam);
    }
    public static void marketSelect(Scanner sc) {
        String markets = "";
        String [] marketSplit;
        System.out.println("Please enter markets separated by comma, or clear");
        markets = sc.next();
        marketSplit = markets.split(",");
        Global.mOne = marketSplit[0].toUpperCase();
        Global.mTwo = marketSplit[1].toUpperCase();
    }
    public static void runStrategy(MongoCRUD mongoCRUD, Price priceObj) throws IOException, InterruptedException {
        BigDecimal profit;
        BigDecimal buy = new BigDecimal(0);
        BigDecimal sell; // If it says not initialized try setting to zero
        double profitPercentageTotals = 0.0;
        boolean successfulBuy = false;
        boolean sellBidMode = true;
        boolean buyBidMode = false;
        boolean hold;
        // Fetch the data
        Map<Object, Object> liveMarketData = PriceObjectSession.sessionFetcher.marketDataFetcher();
        Thread.sleep(Global.rateLimit);
        // Set values to the price object
        priceObj.setPrices(Double.valueOf(liveMarketData.get("Last").toString()));

        // If the incoming size reaches a factor of a candle length set indicators
        if (priceObj.getPriceLonger().size() % PriceObjectSession.candleLength == 0 &&
                priceObj.getPriceShorter().size() % PriceObjectSession.candleLength == 0) {
            setIndicators(priceObj);
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
        // Check average inequality or add more indicators boolean array?
        boolean buyMode = priceObj.validMACDCrossover();
        System.out.println(buyMode);
        int responseCode = 0;
        /* If buy mode is true and we have not yet placed an order (one order at a time FOK) we start
           trying to enter the market based upon our programed indicators and the current
           inequality or relationship between the indicators. Proper function excludes possibility
           of stacking buys it if buys it must sell if then it sells it is able to buy again.
           This is where calculations and decisions are made based on incoming HLOC data.
           At first we check if the ask is larger than the last if we are in buyMode.
           We obtain these values from the HLOC data and we continue if the last is not larger
           or equal to the ask. Different conditions based on the data will lead to HTTP calls
           to the server at different prices based on the best possible entry or exit for the
           market.
         */
        if (buyMode && !successfulBuy) {
            if (askDouble <= lastDouble) {
                buy = BigDecimal.valueOf(askDouble);
                System.out.println("Take the ask at " + buy);
                try {
                    HttpResponse<String> response
                            = sendOrder(createOrder(buy.doubleValue(), "BUY"));
                    responseCode = response.statusCode();
                } catch (IOException | InterruptedException e) {
                    System.out.println("IO Exception : " + e + "\n" + "response: " + responseCode);
                }
            } else {
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
            if (responseCode == 201) {
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
            } catch (IOException e) {
                System.out.print("There was an IOException " + e + "\n" + "response : " +
                        responseCode);

            }
            sellBidMode = false;
            System.out.println("Sell exited because last price dropped to low");
        } else if (successfulBuy && sellBidMode) {
            if (bidDouble < lastDouble) {
                hold = false;
                sell = BigDecimal.valueOf(lastDouble);
                sell = sell.subtract(BigDecimal.valueOf(.0000005));
                sellBidMode = true;
                System.out.println("Last was chosen then subtracted from");
            } else if (bidDouble > askDouble) {
                hold = false;
                sell = BigDecimal.valueOf(bidDouble);
                sellBidMode = true;
                System.out.println("Bid was chosen");
            } else {
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
    }
    public static void boot(MongoCRUD mongoCRUD, Scanner sc, Price.PriceBuilder builder) throws IOException, InterruptedException {
        System.out.println("Welcome please enter a candle length" +
                " 0 = MINUTE_1, 1 = MINUTE_5, 2 = HOUR_1, 3 = DAY_1");
        //candle length function
        System.out.println("Please enter day count for the short moving avg up to 365 days");
        PriceObjectSession.shortDaysInput = sc.nextInt();

        System.out.println("Please enter day count for the int moving avg up to one year, 365 days");
        PriceObjectSession.longDaysInput = sc.nextInt();

        System.out.println("Please enter a calculation strategy high-low = 0, open-close = 1, " +
                "close = 2");
        PriceObjectSession.calcStratInput = sc.next();
        System.out.println("You have entered an entry too short, or have forgotten a comma" +
                ", please enter your market");
        resetHistoricalData(mongoCRUD);
        querySwitchAssembler(PriceObjectSession.calcStratInput, mongoCRUD, builder);

    }
    public static void resetHistoricalData(MongoCRUD mongoCRUD) throws IOException, InterruptedException {
        ArrayList<Map<Object, Object>> historicalData = fetchHistoricalDataByMarket(DataFetch.getNewInstance(), sync(PriceObjectSession.calcStratInput));
        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, Global.HISTORICAL_DATA));
    }
    public static void querySwitchAssembler(String calculationStrategy, MongoCRUD mongoCRUD, Price.PriceBuilder builder) {
        switch (calculationStrategy) {
            case "0":
                builder.priceShorter(dayDataAggregation("high", "low", mongoCRUD, PriceObjectSession.shortDaysInput));
                builder.priceLonger(dayDataAggregation("high", "low", mongoCRUD, PriceObjectSession.longDaysInput));
                break;
            case "1":
                builder.priceShorter(dayDataAggregation("open", "close", mongoCRUD, PriceObjectSession.shortDaysInput));
                builder.priceLonger(dayDataAggregation("open", "close", mongoCRUD, PriceObjectSession.longDaysInput));
                break;
            case "2":
                builder.priceShorter(dayDataAggregation("close", "close", mongoCRUD, PriceObjectSession.shortDaysInput));
                builder.priceLonger(dayDataAggregation("close", "close", mongoCRUD, PriceObjectSession.longDaysInput));
                break;
        }
    }
    public static void historicalDataTimeoutReset(Price priceObj, MongoCRUD mongoCRUD, LocalDateTime start) {
        if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(PriceObjectSession.shortDaysInput))
                || priceObj.getPriceShorter().size() >= Integer.MAX_VALUE - 1) {
            priceObj.getPriceShorter().clear();
            mongoCRUD
                    .retrieveMarketDataByDays(Global.MARKET_SUMMARY,
                            PriceObjectSession.shortDaysInput-1,
                            "TimeStamp",
                            "Last").forEach((data) -> priceObj
                    .addPriceShorter((Double) data.get("Last")));
        }
        if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(PriceObjectSession.longDaysInput))
                || priceObj.getPriceShorter().size() >= Integer.MAX_VALUE - 1) {
            priceObj.getPriceLonger().clear();
            mongoCRUD
                    .retrieveMarketDataByDays(Global.MARKET_SUMMARY,
                            PriceObjectSession.longDaysInput-1,
                            "TimeStamp",
                            "Last").forEach((data) -> priceObj
                    .addPriceLonger((Double) (data.get("Last"))));
            priceObj.setTimestamp(LocalDateTime.now());
        }
        //if it has not reset fully then subtract 1 a day
        if (!start.equals(start.plusDays(PriceObjectSession.shortDaysInput))) { priceObj.dateLimitCheck(1); }
        if (!start.equals(start.plusDays(PriceObjectSession.longDaysInput))) { priceObj.dateLimitCheckLonger(1); }
    }
    public static void dropDB(MongoCRUD mongoCRUD) {
        mongoCRUD.deleteAllMarketData(Global.MARKET_SUMMARY);
        mongoCRUD.deleteAllMarketData(Global.HISTORICAL_DATA);
    }
    public static List<Double> dayDataAggregation(String projectionOne, String projectionTwo, MongoCRUD mongoCRUD, int period) {

        List<Double> resultOne = mongoCRUD
                .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                        period,
                        "startsAt",
                        projectionOne);
        List<Double> resultTwo  = mongoCRUD
                .retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                        period,
                        "startsAt",
                        projectionTwo);

        return takeAvg(resultOne, resultTwo);
    }
    public static List<Double> takeAvg(List<Double> arOne, List<Double> arTwo) {
        List<Double> avg = new LinkedList<>();
        for (int i = 0; i < arOne.size(); i++) {
            avg.add((arOne.get(i) + arTwo.get(i)) / 2);
        }
        return avg;
    }
    public static Price priceObjectBuild(Price.PriceBuilder builder){
        return builder.build();
    }
    //mayb a private function of some thing
    public static void mACDBuilder(Price.PriceBuilder builder, MongoCRUD mongoCRUD) {

        List<Double> nineDayPeriod = mongoCRUD.retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                9,
                "startsAt",
                "close");
        List<Double> twelveDayPeriod = mongoCRUD.retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                12,
                "startsAt",
                "close");
        List<Double> twentySixDayPeriod = mongoCRUD.retrieveMarketDataByDays(Global.HISTORICAL_DATA,
                26,
                "startsAt",
                "close");

        builder.nineDaysOfClose(nineDayPeriod)
                .shortMACDPeriod(twelveDayPeriod)
                .longerMACDPeriod(twentySixDayPeriod);
    }
}