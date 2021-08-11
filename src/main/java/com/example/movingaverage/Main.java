package com.example.movingaverage;

import com.example.movingaverage.DAO.MongoCRUD;
import com.example.movingaverage.Live.Buy;
import com.example.movingaverage.Live.DataFetch;
import com.example.movingaverage.Live.Sell;
import com.example.movingaverage.Live.Transaction;
import com.example.movingaverage.Model.Price;
import com.example.movingaverage.session.PriceSession;
import com.example.movingaverage.strategy.MACDSignalLineCrossover;
import com.example.movingaverage.strategy.TradingStrategy;

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
        Deque<String> commandHistory = new ArrayDeque<>(10);
        //Global.quant = .0400000; hard coded to avoid accidental purchase
        // Insert this into the prices value for the candle tick function Double.valueOf(liveMarketData.get("Last").toString()))
        System.out.println(
                "The commands are as following; " +
                        "\n boot : This will start the bot if the config is set " +
                        "\n market: this will bind a market combination to a session." +
                        "\n sync: this will set the length of the session candle length." +
                        "\n test : run system tests" +
                        "\n practice : takes you to a no risk environment"
        );
        // Boot up
        commandHistory.push(sc.next());
        while (!commandHistory.isEmpty()) {
            String last = commandHistory.peek();
            if (last.matches("^boot$")) {
                Price.PriceBuilder builder = priceBuilderInit(1.0);
                boot(mongoCRUD, sc, builder);
            }
            // You are able to switch markets
            if (last.matches("^market\\s?.*(\\w|\\D|\\S){2,6}$")) {
                marketPivot(mongoCRUD, sc);
                PriceSession.sessionFetcher = DataFetch.getNewInstance();
            }
            // You are able to switch candle sync modes
            if (last.matches("^sync\\s?.*(0-3)$")) {
                dropDB(mongoCRUD);
                PriceSession.candleType = sc.toString().split("\\d")[0];
                setHistoricalData(mongoCRUD);
            }
            if (last.matches("^run$")) {
                //abstract factory
                runCommand(mongoCRUD);
            }
            if (commandHistory.size() >= 10) {
                commandHistory.removeLast();
            }
            else {
                commandHistory.push(sc.next());
            }
        }
    }

    // Implement threading
    public static void runCommand(MongoCRUD mongoCRUD) throws IOException, InterruptedException {

        Price snapShot = PriceSession.currentPriceObject;
        Map<Object, Object> polledData = poll();

        double lastDouble = Double.parseDouble(polledData.get("Last").toString());
        double askDouble = Double.parseDouble(polledData.get("Ask").toString());
        double bidDouble = Double.parseDouble(polledData.get("Bid").toString());

        snapShot.priceListener(lastDouble);

        snapShot.init();
        saveMarketPoll(polledData, mongoCRUD);
        if (candleCheck(LocalDateTime.now())) {
            candleTick(snapShot, Double.valueOf(lastDouble));
        }
        //function check for what type of indicator is happening
        if (PriceSession.currentPriceObject.validMACDCrossover()) {
            // Possible abstract factory or string based factory
            runMACDSignalLineCrossoverStrategy(lastDouble, askDouble, bidDouble);
        }
    }
    private static void marketPivot(MongoCRUD mongoCRUD, Scanner sc) throws IOException, InterruptedException {

        dropDB(mongoCRUD);
        marketSelect(sc);
        setHistoricalData(mongoCRUD);

        PriceSession.currentPriceObject = priceCreator(mongoCRUD, priceBuilderInit(1.0));
    }

    public static void runMACDSignalLineCrossoverStrategy(
            Double lastDouble, Double askDouble, Double bidDouble) throws IOException, InterruptedException {

        MACDSignalLineCrossover signalLineCrossoverStrategy =
                MACDSignalLineCrossover
                        .createMACDSignalLineCrossoverStrategy(
                                PriceSession.currentPriceObject, lastDouble, askDouble, bidDouble
                        );

        signalLineCrossoverStrategy.setBuyBidMode();

        System.out.println("Waiting for a buy");
        while (!signalLineCrossoverStrategy
                    .buyResponseHandling(sendOrder(createOrder(
                            signalLineCrossoverStrategy.setBuyBidMode().doubleValue(), "buy"
                    ))) ?
                    signalLineCrossoverStrategy.sellHodlSet()
                    :
                    signalLineCrossoverStrategy.buyResponseHandling(sendOrder(createOrder(
                            signalLineCrossoverStrategy.buyGate().doubleValue(), "buy"
                    ))) // || buyTimeout()
        ) {
            System.out.print(".");
        }
        PriceSession.successfulBuy = true;
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
        if (response.statusCode() == 401) {
            System.out.println("Unauthorized 401 body is" + response.body());
        } else {
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
        if (sell.doubleValue() < bidDouble) {
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

    public static String sync(String candleType) {
        int candleLengthM = Global.rateLimit / 1000;
        String queryParameter;
        switch (candleType) {
            case "0":
                queryParameter = "MINUTE_1";
                PriceSession.candleLength = 60 * candleLengthM;
                break;
            case "1":
                queryParameter = "MINUTE_5";
                PriceSession.candleLength = (60 * 5) * candleLengthM;
                break;
            case "2":
                queryParameter = "HOUR_1";
                PriceSession.candleLength = (60 * 60) * candleLengthM;
                break;
            case "3":
                queryParameter = "DAY_1";
                PriceSession.candleLength = 86400 * candleLengthM;
                break;
            default:
                throw new IllegalArgumentException();
        }
        // Fetch historical data
        //return this function callback
        return queryParameter;
    }

    public static List<Map<Object, Object>> fetchHistoricalDataByMarket(DataFetch fetcher, String queryParam)
            throws InterruptedException, IOException {
        Thread.sleep(Global.rateLimit);
        return fetcher.historicalDataFetcher(queryParam);
    }

    public static void marketSelect(Scanner sc) {
        String markets = "";
        String[] marketSplit;
        System.out.println("Please enter markets separated by comma, or clear");
        markets = sc.next();
        marketSplit = markets.split(",");
        PriceSession.mOne = marketSplit[0].toUpperCase();
        PriceSession.mTwo = marketSplit[1].toUpperCase();
    }

    public static Map<Object, Object> poll() throws IOException {
        return PriceSession.sessionFetcher.marketDataFetcher();
    }

    public static void saveMarketPoll(Map<Object, Object> polledData, MongoCRUD mongoCRUD) {
        mongoCRUD.createMarketData(polledData, Global.MARKET_SUMMARY);
    }

    public static void candleTick(Price priceObj, Double prices) {
        if (priceObj.getPriceLonger().size() % PriceSession.candleLength == 0 &&
                priceObj.getPriceShorter().size() % PriceSession.candleLength == 0) {
            priceObj.setPrices(prices);
            setIndicators(priceObj);
            System.out.println("Candle created: \n" + priceObj.toString());
        }
    }

    public static void strategySelection(){}

    public static void boot(MongoCRUD mongoCRUD, Scanner sc, Price.PriceBuilder builder) throws IOException, InterruptedException {
        dropDB(mongoCRUD);
        System.out.println("Please enter a valid market");
        marketSelect(sc);

        System.out.println("Welcome please enter a candle length" +
                " 0 = MINUTE_1, 1 = MINUTE_5, 2 = HOUR_1, 3 = DAY_1");
        PriceSession.candleType = sc.next();
        sync(PriceSession.candleType);
        System.out.println("Please enter day count for the short moving avg up to 365 days");
        PriceSession.shortDaysInput = sc.nextInt();

        System.out.println("Please enter day count for the int moving avg up to one year, 365 days");
        PriceSession.longDaysInput = sc.nextInt();


        System.out.println("Please enter a calculation strategy high-low = 0, open-close = 1, " +
                "close = 2");
        PriceSession.calcStratInput = sc.next();

        System.out.println("Fetching session...");
        PriceSession.sessionFetcher = DataFetch.getNewInstance();
        setHistoricalData(mongoCRUD);
        System.out.println("Session fetched");

        System.out.println("Creating Price");
        querySwitchAssembler(PriceSession.calcStratInput, mongoCRUD, builder);
        PriceSession.currentPriceObject = priceCreator(mongoCRUD, builder);
        System.out.println("Price created");
    }

    public static void setHistoricalData(MongoCRUD mongoCRUD) throws IOException, InterruptedException {
        List<Map<Object, Object>> historicalData = fetchHistoricalDataByMarket(PriceSession.sessionFetcher, sync(PriceSession.candleType));
        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, Global.HISTORICAL_DATA));
    }

    public static void querySwitchAssembler(String calculationStrategy, MongoCRUD mongoCRUD, Price.PriceBuilder builder) {
        switch (calculationStrategy) {
            case "0":
                builder.priceShorter(
                        dayDataAggregation("high", "low", mongoCRUD, PriceSession.shortDaysInput)
                );
                builder.priceLonger(
                        dayDataAggregation("high", "low", mongoCRUD, PriceSession.longDaysInput)
                );
                break;
            case "1":
                builder.priceShorter(
                        dayDataAggregation("open", "close", mongoCRUD, PriceSession.shortDaysInput)
                );
                builder.priceLonger(
                        dayDataAggregation("open", "close", mongoCRUD, PriceSession.longDaysInput)
                );
                break;
            case "2":
                builder.priceShorter(
                        dayDataAggregation("close", "close", mongoCRUD, PriceSession.shortDaysInput)
                );
                builder.priceLonger(
                        dayDataAggregation("close", "close", mongoCRUD, PriceSession.longDaysInput)
                );
                break;
        }
    }

    public static void historicalDataTimeoutReset(Price priceObj, MongoCRUD mongoCRUD, LocalDateTime start) {
        if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(PriceSession.shortDaysInput))
                || priceObj.getPriceShorter().size() >= Integer.MAX_VALUE - 1) {
            priceObj.getPriceShorter().clear();
            mongoCRUD.retrieveMarketDataByDays(
                    Global.MARKET_SUMMARY, PriceSession.shortDaysInput - 1,"TimeStamp","Last"
            ).forEach(priceObj::addPriceShorter);
        }
        if (LocalDateTime.now().equals(priceObj.getTimestamp().plusDays(PriceSession.longDaysInput))
                || priceObj.getPriceShorter().size() >= Integer.MAX_VALUE - 1) {
            priceObj.getPriceLonger().clear();
            mongoCRUD.retrieveMarketDataByDays(
                    Global.MARKET_SUMMARY, PriceSession.longDaysInput - 1, "TimeStamp", "Last"
            ).forEach(priceObj::addPriceLonger);

            priceObj.setTimestamp(LocalDateTime.now());
        }
        //if it has not reset fully then subtract 1 a day
        if (!start.equals(start.plusDays(PriceSession.shortDaysInput))) {
            priceObj.dateLimitCheck(1);
        }
        if (!start.equals(start.plusDays(PriceSession.longDaysInput))) {
            priceObj.dateLimitCheckLonger(1);
        }
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
        List<Double> resultTwo = mongoCRUD
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

    public static Price priceObjectBuild(Price.PriceBuilder builder) {
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

    public static Price.PriceBuilder priceBuilderInit(Double smoothing) {
        return Price.builder().smoothing(smoothing);
    }

    public static Price priceCreator(MongoCRUD mongoCRUD, Price.PriceBuilder priceBuilder) {
        // MACD build function
        mACDBuilder(priceBuilder, mongoCRUD);
        // Build Function
        priceBuilder.twelveDayRibbons(new ArrayList<>(0))
                .twentySixDayRibbons(new ArrayList<>(0))
                .signalLine(new ArrayList<>(0))
                .timestamp(LocalDateTime.now())
                .dateLimit(LocalDateTime.now().plusHours(24));
        return priceObjectBuild(priceBuilder);
    }

    public static boolean candleCheck(LocalDateTime now) {
        boolean candleCreated = false;
        switch (PriceSession.candleLength) {
            case 0:
                candleCreated = now.getSecond() == 0;
                Global.rateLimit = 1000;
                break;
            case 1:
                candleCreated = now.getMinute() % 5 == 0;
                Global.rateLimit = 1000 * 60;
                break;
            case 2:
                candleCreated = now.getHour() == 0;
                Global.rateLimit = (1000 * 60) * 60;
                break;
            case 3:
                candleCreated = now.getHour() % 24 == 0;
                Global.rateLimit = (((1000 * 60) * 60) * 24);
                break;
            default:
        }
        return candleCreated;
    }
}
    /* pulbic static HttpResponse<String>
response = sendOrder(createOrder(buy.doubleValue(), "BUY"));
        responseCode = response.statusCode();
        } catch (Exception e) {
        System.out.println("IO Exception : " + e + "\n" + "response: " + responseCode);
        }
        }
     */