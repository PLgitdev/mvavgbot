package com.example.movingaverage;

import com.example.movingaverage.Controller.MongoCRUD;
import com.example.movingaverage.Live.Buy;
import com.example.movingaverage.Live.DataFetch;
import com.example.movingaverage.Live.Sell;
import com.example.movingaverage.Live.Transaction;
import com.example.movingaverage.Model.Price;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

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
         LocalDateTime start = LocalDateTime.now();
         boolean buyMode = true;
         Global.quant = .0300000;

        System.out.println("Please enter markets separated by comma, or clear");
        while (!"clear".equalsIgnoreCase(markets)) {
            markets = sc.next();
            try {
                marketSplit = markets.split(",");
                Global.mOne = marketSplit[0].toUpperCase();
                Global.mTwo = marketSplit[1].toUpperCase();
                fetcher = DataFetch.getInstance();
                if (fetcher.valid()) {
                    try {
                        System.out.println("Welcome please enter a candle length" +
                            " 0 = MINUTE_1, 1 = MINUTE_5, 2 = HOUR_1, 3 = DAY_1");
                        int len = sc.nextInt();
                        String l = "";
                        switch (len % 10) {
                            case 0:
                                l = "MINUTE_1";
                                break;
                            case 1:
                                l = "MINUTE_5";
                                break;
                            case 2:
                                l = "HOUR_1";
                                break;
                            case 3:
                                l = "DAY_1";
                                break;
                        }
                        ArrayList<Map<Object,Object>> historicalData = fetcher.historicalDataFetcher(l);
                        Thread.sleep(1000);
                        historicalData.forEach((data) -> mongoCRUD.createMarketData(data, "historicaldata"));
                        System.out.println("Please enter day count for the short moving avg up to 365 days");
                        inputL = sc.nextInt();
                        System.out.println("Please enter day count for the int moving avg up to one year, 365 days");
                        inputL2 = sc.nextInt();
                        System.out.println("Please enter a calculation strategy high-low = 0, open-close = 1, " +
                            "close = 2");
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

                        switch (inputS) {
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
                                takeAvg(shorterDaysDataHigh, shorterDaysDataHighD, shorterDaysDataLowD, pricesS);
                                takeAvg(longerDaysDataHigh, longerDaysDataHighD, longerDaysDataLowD, pricesL);
                                break;
                            case "1":
                                shorterDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL - 1,
                                        "startsAt",
                                        "close"
                                    );
                                shorterDaysDataOpen = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL - 1,
                                        "startsAt",
                                        "open"
                                    );
                                longerDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2 - 1,
                                        "startsAt",
                                        "close"
                                    );
                                longerDaysDataOpen = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
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
                                takeAvg(shorterDaysDataOpen, shorterDaysDataOpenD, shorterDaysDataCloseD, pricesS);
                                takeAvg(longerDaysDataOpen, longerDaysDataOpenD, longerDaysDataCloseD, pricesL);
                                break;
                            case "2":
                                shorterDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL - 1,
                                        "startsAt",
                                        "close"
                                    );
                                longerDaysDataClose = mongoCRUD
                                    .retrieveMarketDataByDays("historicaldata",
                                        inputL2 - 1,
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

                        List<Map<?, ?>> nineDayPeriod = mongoCRUD.retrieveMarketDataByDays("historicaldata",
                            9,
                            "startsAt",
                            "close");
                        List<Map<?, ?>> twelveDayPeriod = mongoCRUD.retrieveMarketDataByDays("historicaldata",
                            12,
                            "startsAt",
                            "close");
                        List<Map<?, ?>> twentySixDayPeriod = mongoCRUD.retrieveMarketDataByDays("historicaldata",
                            26,
                            "startsAt",
                            "close");
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
                        BigDecimal sell = new BigDecimal(0);
                        double profitPercentageTotals = 0.0;
                        boolean successfulBuy = false;
                        boolean sellGate = false;
                        boolean sellBidMode = false;
                        boolean buyBidMode = false;
                        boolean hold = false;
                        while (!markets.equalsIgnoreCase("clear")) {
                            liveMarketData = fetcher.marketDataFetcher();
                            Thread.sleep(1000);
                            priceObj.setPrices(Double.valueOf(liveMarketData.get("Last").toString()));
                            priceObj.setSMA();
                            priceObj.setSMACDEMA();
                            priceObj.setLMACDEMA();
                            priceObj.setMACD();
                            priceObj.updateSignalLine();
                            mongoCRUD.createMarketData(liveMarketData, "marketsummary");
                            //set the transaction obj
                            // liveMarketData.forEach( (key,value) -> System.out.println(key + ":"+  value));
                            Double lastDouble =Double.valueOf(liveMarketData.get("Last").toString());
                            Double askDouble = Double.valueOf(liveMarketData.get("Ask").toString());
                            Double bidDouble = Double.valueOf(liveMarketData.get("Bid").toString());
                            System.out.println(liveMarketData.get("Last") + "\n" +
                                "Total percentage gain/loss : " + profitPercentageTotals + "\n" + "Bank : "
                                + (Global.quant + (Global.quant * (profitPercentageTotals) / 100d)));
                            //check average inequality
                            int responseCode = 0;
                            if(priceObj.validMACDCrossover() && buyMode && !successfulBuy) {
                                    if (askDouble <= lastDouble) {
                                        buy = BigDecimal.valueOf(askDouble);
                                        System.out.println("Take the ask at " + buy);
                                        try {
                                            HttpResponse<String> response = sendOrder(createOrder(buy.doubleValue(), "BUY"));
                                            responseCode = response.statusCode();
                                        }
                                        catch(IOException e) {
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
                                                System.out.println("Take the ask at " + buy);
                                            }
                                            if (buy.doubleValue() > lastDouble) {
                                                buy = BigDecimal.valueOf(lastDouble)
                                                    .add(BigDecimal.valueOf(0.00000002));
                                                System.out.println("Take the last at " + buy);
                                            } else {
                                                buy = BigDecimal.valueOf(askDouble);
                                                System.out.println("Take the ask at " + buy);
                                            }
                                            HttpResponse<String> response = sendOrder(createOrder(buy.doubleValue(), "BUY"));
                                            responseCode = response.statusCode();
                                        } catch (IOException e) {
                                            System.out.print("There was an IOException " + e + "\n" + "response : " +
                                                responseCode);
                                        }
                                    }
                                    if(responseCode == 201) {
                                        successfulBuy = true;
                                        //buyMode = false;
                                        buyBidMode = false;
                                        System.out.println("Successful Buy 201 at " + buy + "\n" +
                                            "this is the response " + responseCode);
                                        responseCode = 0;
                                    }
                                }
                                if(priceObj.validMACDBackCross() && successfulBuy && !sellBidMode && !sellGate ) {
                                    buyMode = false;
                                    sellGate = true;
                                }
                            if(sellGate && lastDouble <
                                //sensitivity
                                buy.subtract(buy.multiply(BigDecimal.valueOf(0.001))).doubleValue()) {
                                sell = BigDecimal.valueOf(bidDouble);
                                try {
                                    HttpResponse<String> response = sendOrder(sellRoutine(sell, bidDouble));
                                    responseCode = response.statusCode();
                                }
                                catch (IOException e) {
                                    System.out.print("There was an IOException " + e + "\n" + "response : " +
                                        responseCode);
                                }
                                sellGate = false;
                                sellBidMode = false;
                                buyMode = true;
                                successfulBuy = false;
                                System.out.println("Sell exited because last price dropped to low");
                            }
                            if(sellGate && successfulBuy) {
                                if(bidDouble < lastDouble) {
                                    hold = false;
                                        sell = BigDecimal.valueOf(lastDouble);
                                        sell = sell.subtract(BigDecimal.valueOf(.0000005));
                                        sellBidMode = true;
                                        System.out.println("Last was chosen then subtracted from");
                                }
                                else if(bidDouble > askDouble) {
                                    hold = false;
                                    sell = BigDecimal.valueOf(bidDouble);
                                    sell = sell.subtract(BigDecimal.valueOf(.0000005));
                                    sellBidMode = true;
                                    System.out.println("Bid was chosen then subtracted from");
                                }
                                else {
                                    hold = false;
                                    sell = BigDecimal.valueOf(askDouble);
                                    sell = sell.subtract(BigDecimal.valueOf(.0000005));
                                    System.out.println("Ask was chosen then subtracted from");
                                }
                                if(sell.doubleValue() < buy.add(buy.multiply(BigDecimal.valueOf(.0001)))
                                        .doubleValue() && sell.doubleValue() != 0) {
                                    hold = true;
                                    //back to buy mode and threading?
                                    //adjust this i guess
                                    // if its about to pop only take big ones if its flat take small ones
                                    // if bid is a certian percent above last then take it..
                                    System.out.println("Hold missed sell wait due to not enuf profit");
                                }
                                System.out.println("\n" + "Sell at " + sell + " vs bid " + liveMarketData.get("Bid"));
                                }
                                if(!hold && sellGate) {
                                    // if the Bid is more than the last use the Last
                                    sell = sell.subtract(BigDecimal.valueOf(.00000001));
                                    sell = sell.setScale(8, RoundingMode.HALF_UP);
                                    //if no sell successful
                                    System.out.println("\n Cancel last sell and Sell at " + sell + " bid is " +
                                        liveMarketData.get("Bid"));
                                    try {
                                       HttpResponse<String> response = sendOrder(sellRoutine(sell,bidDouble));
                                       responseCode = response.statusCode();
                                    }
                                    catch (IOException e) {
                                        System.out.print("There was an IOException " + e + "\n response : " +
                                            responseCode);
                                    }
                                }
                                if(responseCode == 201 && !buyMode) {
                                    //? and valid MACDCrossover?
                                    BigDecimal profit = sell.subtract(buy);
                                    if(profit.doubleValue() > 0d) {
                                        profit = profit.divide(buy, RoundingMode.HALF_UP)
                                            .multiply(BigDecimal.valueOf(100.0));
                                    }
                                    profitPercentageTotals += profit.doubleValue();
                                    System.out.println("Sell successful at " + sell + " " + "profit percent : " +
                                        profit + "%" + "\n response: " + responseCode);
                                    sellGate = false;
                                    sellBidMode = false;
                                    buyMode = true;
                                    successfulBuy = false;
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

    /*public static Map<Object,?> liveMarketDataTransform(Map<Object, Object> map) {
        map.forEach((k,v) ->  {
            map.replace(k,k.toString());
            if(map.get(v).toString().matches("[.]\\d")) {
            map.replace(k, v, Double.valueOf(v.toString()));
            }
        });
        return map;
    }

     */
    public static void takeAvg (ArrayList<Map<?, ?>> maps,
                                ArrayList<Double> arOne, ArrayList<Double> arTwo,
                                ArrayList<Double> prices) {
        for (int i = 0; i < maps.size(); i++) {
            prices.add((arOne.get(i) + arTwo.get(i)) / 2);
        }
    }

    public static Transaction createOrder(Double limit, String direction) throws MalformedURLException {
        return direction.equalsIgnoreCase("Buy") ?
            Buy.getInstance("LIMIT", limit, Global.orderTimeInForce, direction) :
            Sell.getInstance("LIMIT", limit, Global.orderTimeInForce, direction);
    }
    public static HttpResponse<String> sendOrder(Transaction order) throws IOException, InterruptedException {
        HttpResponse<String> response = order.send();
        if(response.statusCode() == 201) {
            System.out.println("Successful order");
        }
        if(response.statusCode() == 401)  {
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
}
//take the avg of open and close
                                /*for (int i = 0; i < shorterDaysDataOpen.size(); i++) {
                                    pricesS.add((shorterDaysDataOpenD.get(i) + shorterDaysDataCloseD.get(i)) / 2);
                                } for (int i = 0; i < longerDaysDataOpen.size(); i++) {
                                    pricesL.add((longerDaysDataOpenD.get(i) + longerDaysDataCloseD.get(i)) / 2);
                                }
                                */
//if ask is more than last use ask
// ^^ oirg 0.00000010
// send new sells every time sell.subtract(BigDecimal.valueOf(0.00000001));
//sell multiplier should be related to volume
//if volume increases more than at the time of buy increase multiplier
//get out at bid or get out at scale option
//ask - an amount to try to get off the sale or bid plus amount
//bid + an amount
//only sell on a successful buy?
//sell = BigDecimal.valueOf(Double.valueOf(resultM.get("Bid").toString()));
//win loss ration, profit total
//resultM.forEach( (key,value) -> System.out.println(key + ":"+  value));
                                /*if(Double.valueOf(sell.toString()) < (Double) resultM.get("Last") &&
                                    !sell.equals(BigDecimal.valueOf(0.0)) && !buyMode) {
                                    System.out.println("Sucessfull SELL");
                                }
                                */
                            /*else if (!priceObj.validSMACrossover() && successfulBuy) {


                            }
                            else if (!priceObj.validSMACrossover() && successfulSell
                             */
//send a buy request then either scale profits or sell at crossover
//check out v1 and look at buy request as well as the profit scaling
//make buy order a limit buy that is a little less than the target. (safety)
//and successful buy
//if MACD crosses without successful buy reset to buy mode
//does it cancel current buy?
//scale profits ??? buy object sell object needed
//grab someones order out of the order book
//ask + 1?
//if it starts to dig a hole increase quanitity * by how many times it has looped
//try to use last as each base for bidding basically subtract from last each time not buy

//BigDecimal sellMultiplier = BigDecimal.valueOf(.04);
//sell = buy.multiply(sellMultiplier);
//if the bid is less than the buy during a sell mode you should respond with buy
//if bid is lower than price meet price
//if(resultM.get("Last") > )
//bidding storm increasing slightly every iteration?
// we need to make sure transaction went through to continue to sell mode
//manual sell button
//profit zone indicator
// if its in sell mode and encounters a crossover it should sell?
//stoploss at 3%
//less quantitiy at the beining more as time progresses to a point

                                /*if ((Double) resultM.get("Bid") >= sell.doubleValue()) {
                                    sell = BigDecimal.valueOf((Double) resultM.get("Bid"));
                                }
                                 */
//if ask less than sell make it ask
                                /*if ((Double) resultM.get("Last") < sell.doubleValue()) {
                                    sell = BigDecimal.valueOf((Double) resultM.get("Last"));
                                }
                                 */
                                /*if (sell.doubleValue() < (Double) resultM.get("Bid") ||
                                    (Double) resultM.get("Last") <= (Double) resultM.get("Bid")) {
                                    sell = BigDecimal.valueOf((Double) resultM.get("Bid"));
                                }
r                                */
                            /*if(!sellBidMode && successfulBuy && buy.doubleValue() < buy
                                .subtract(buy.multiply(BigDecimal.valueOf(0.01))).doubleValue()) {
                                sell = BigDecimal.valueOf((Double) resultM.get("Bid"));
                                System.out.println("Sell replaced with bid");
                            }
                             */
//stop loss for buys
//fixed scaled buys option
//what to do if buy is more than last but less than ask during buy
//if the bid
// buy less than price and less than ask good barder
                                /*else if ((Double) resultM.get("Last") > buy.doubleValue() &&
                                    (Double) resultM.get("Ask") > buy.doubleValue()) {
                                    buy = BigDecimal.valueOf((Double) resultM.get("Last"));
                                    buy = buy.add(buy.multiply(BigDecimal.valueOf(0.0001)));
                                }
                                 */
                               /* else if (buy.doubleValue() < (Double) resultM.get("Last")) {
                                }

                                */
                                /*else if((Double) resultM.get("Last") > buy.subtract(buy
                                    .multiply(BigDecimal.valueOf(0.001))).doubleValue()) {
                                     sell = BigDecimal.valueOf((Double) resultM.get("Bid"))
                                        .subtract(BigDecimal.valueOf(0.00000001));
                                }
                                /*else if ((Double) resultM.get("Bid") > buy.doubleValue()) {
                                    sell = BigDecimal.valueOf(Double.valueOf(resultM.get("Bid").toString()))}
                            /*if((Double) resultM.get("Bid") > (Double) resultM.get("Last") &&
                                sell.doubleValue() < (Double) resultM.get("Last") &&
                                !buyMode && sellBidMode && priceObj.validMACDCrossover()) {
                                sell =
                                    BigDecimal.valueOf((Double) resultM.get("Bid"))
                                        .subtract(BigDecimal.valueOf(0.00000005));
                                System.out.println("Sell is replaced with percentage of ask " + sell);
                            }

                             */
                            /*if(sell.doubleValue() > (Double) resultM.get("Last") && sellBidMode) {
                                sell = BigDecimal.valueOf((Double) resultM.get("Last"));
                            }

                             */

                                /*if((Double) resultM.get("Bid") > buy.doubleValue()) {
                                    if((Double) resultM.get("Bid") >= (Double) resultM.get("Last")) {
                                        sellBidMode = true;
                                    }
                                    else {
                                        sell = BigDecimal.valueOf((Double) resultM.get("Bid"))
                                                .subtract(BigDecimal.valueOf(0.00000005));
                                        System.out.println("Bid higher than buy and Bid lower than last");
                                    }

                                }
                                /*else if(sell.doubleValue() < buy.doubleValue()) {
                                    sell = BigDecimal.valueOf((Double) resultM.get("Ask"))
                                        .subtract(BigDecimal.valueOf(0.00000005));
                                    sellBidMode = true;
                                }

                                 */
                                /*else {
                                    if((Double) resultM.get("Last") > sell.doubleValue()) {
                                        if ((Double) resultM.get("Ask") > sell.doubleValue()) {
                                            sell = BigDecimal.valueOf((Double) resultM.get("Ask"))
                                                .subtract(BigDecimal.valueOf((Double) (resultM.get("Ask")))
                                                    .multiply(BigDecimal.valueOf(0.0001)));
                                            System.out.println("Sell is less than the Last " +
                                                "and ask is more than the sell replacing with percentage of ask ");
                                        }
                                        else {
                                            sell = BigDecimal.valueOf((Double) resultM.get("Last"));
                                            sell = sell.subtract(sell.multiply(BigDecimal.valueOf(.00001)));
                                            System.out.println("Sell will be subtracted by a multiple of .0001");
                                        }
                                    }

                                 */
//sell = sell.subtract(sell.multiply(BigDecimal.valueOf(.0000001)));
//System.out.println("Sell will be subtracted by a multiple of .0001");
// ^ big reduction here small during bid
// sell = sell.setScale(8, RoundingMode.HALF_UP);
                                /*jjjelse if (priceObj.validMACDBackCross()) {
                                    sell = BigDecimal.valueOf((Double) resultM.get("Bid"));
                                    sell = sell.subtract(BigDecimal.valueOf(0.00000002));
                                    System.out.println("Sell exited due to shift in MACD in real life " +
                                        "you could hold instead of sell");
                                }

                                 */
                                /*jjjelse if (priceObj.validMACDBackCross()) {
                                    sell = BigDecimal.valueOf((Double) resultM.get("Bid"));
                                    sell = sell.subtract(BigDecimal.valueOf(0.00000002));
                                    System.out.println("Sell exited due to shift in MACD in real life " +
                                        "you could hold instead of sell");
                                }

                                 */
//mayb take the below if statement out
                             /*else if (priceObj.validMACDBackCross()){
                                //cancel last buy
                                System.out.println("no buys / cancel ur buy");
                            }
                              */
                                /*else if (priceObj.validMACDCrossover()) {
                                    hold = true;
                                    System.out.println("Sell on hold due to MACD");
                                }

                                 */
//MACD gaurd on  off capability
//MACD gaurd hits early you need to keep ir from premptivly cutting you out
//if it hits a new low quantity up!
//new 24 hour low this multiplier below up!
//quanitity tied to overall score
//.00001 might be too sensitive
