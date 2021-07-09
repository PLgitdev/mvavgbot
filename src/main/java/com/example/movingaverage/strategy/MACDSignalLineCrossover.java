package com.example.movingaverage.strategy;

import com.example.movingaverage.Global;
import com.example.movingaverage.Model.Price;
import com.example.movingaverage.session.PriceObjectSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpResponse;
import java.util.Map;

public class MACDSignalLineCrossover extends TradingStrategy {
    boolean successfulBuy = false;
    boolean sellBidMode = true;
    boolean buyBidMode = false;
    boolean hold;
    private MACDSignalLineCrossover (Price priceObj, Map<Object, Object> liveMarketData) {
        super();
        this.priceObj = priceObj;
        this.liveMarketData = liveMarketData;
    }

        // Fetch the data
        // If the incoming size reaches a factor of a candle length set indicators
        /*  If you want to check every iteration
         liveMarketData.forEach( (key,value) -> System.out.println(key + ":"+  value)); */
        // Might have to go back to Wrappers after live testing
        double lastDouble = Double.parseDouble(liveMarketData.get("Last").toString());
        double askDouble = Double.parseDouble(liveMarketData.get("Ask").toString());
        double bidDouble = Double.parseDouble(liveMarketData.get("Bid").toString());
        System.out.println(liveMarketData.get("Last") + "\n" +
                "Total percentage gain/loss : " + this.profitPercentageTotals + "\n" + "Bank : "
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
                } catch (Exception e) {
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
