package com.example.movingaverage.strategy;

import com.example.movingaverage.Global;
import com.example.movingaverage.Model.Price;
import com.example.movingaverage.session.PriceObjectSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/* If buy mode is true and we have not yet placed an order (one order at a time FOK) we start
   trying to enter the market based upon our programed indicators and the current
   inequality or relationship between the indicators. Proper function excludes possibility
   of stacking buys it if buys it must this.sell if then it this.sells it is able to buy again.
   This is where calculations and decisions are made based on incoming HLOC data.
   At first we check if the ask is larger than the last if we are in buyMode.
   We obtain these values from the HLOC data and we continue if the last is not larger
   or equal to the ask. Different conditions based on the data will lead to HTTP calls
   to the server at different prices based on the best possible entry or exit for the
   market.
 */

// Fetch the data
// If the incoming size reaches a factor of a candle length set indicators

/*  If you want to check every iteration
    liveMarketData.forEach( (key,value) -> System.out.println(key + ":"+  value)); */
// Might have to go back to Wrappers after live testing

public class MACDSignalLineCrossover extends TradingStrategy {
    boolean buyBidMode = false;
    double lastDouble;
    double askDouble;
    double bidDouble;
    boolean buyMode = false;
    boolean successfullyBuy = false;

    private MACDSignalLineCrossover(Price priceObj, Double ask, Double bid, Double last) {
        super();
        this.buyMode = PriceObjectSession.buyMode;
        this.priceObj = priceObj;
        this.lastDouble = last;
        this.askDouble = ask;
        this.bidDouble = bid;
        // priceObj.validMACDCrossover();
    }
    public static MACDSignalLineCrossover createMACDSignalLineCrossoverStrategy(Price priceObj, Double ask, Double bid, Double last) {
        return new MACDSignalLineCrossover(priceObj, ask, bid, last);
    }

    public void setBuyMode() {
        this.buyMode = PriceObjectSession.buyMode;
    }

    public void profitStatus() {
        System.out.println(liveMarketData.get("Last") + "\n" +
                "Total percentage gain/loss : " + this.profitPercentageTotals + "\n" + "Bank : "
                + (Global.quant + (Global.quant * (profitPercentageTotals) / 100d)));
    }

    // Check average inequality or add more indicators boolean array?
        //liveMarketData.forEach((key, value) -> System.out.println(key + ":" + value));

    public BigDecimal setBuyBidMode() {
        if (buyMode && !successfullyBuy)
        if (this.askDouble <= this.lastDouble) {
            this.buy = BigDecimal.valueOf(this.askDouble);
            System.out.println("Take the ask at " + this.buy);
        } else {
            this.buy = BigDecimal.valueOf(this.bidDouble);
            this.buyBidMode = true;
        }
        System.out.println("BUY Attempt at " + buy);
        return this.buy;
    }

    public boolean isBuyBidMode() {
        return buyBidMode;
    }

    // If buy bid mode true then run function below if not then skip it
    public BigDecimal buyGate() {
        //repeat until the buy goes through if buy bit mode on
        this.buy = buy.setScale(8, RoundingMode.HALF_UP);
        if (this.buy.doubleValue() >= this.askDouble) {
            this.buy = BigDecimal.valueOf(this.askDouble).add(BigDecimal.valueOf(0.00000002));
            System.out.println("add to the ask for " + this.buy);
            return this.buy;
        }
        if (this.buy.doubleValue() > this.lastDouble) {
            this.buy = BigDecimal.valueOf(this.lastDouble)
                    .add(BigDecimal.valueOf(0.00000002));
            System.out.println("Take the last at " + this.buy);
        } else {
            this.buy = BigDecimal.valueOf(this.askDouble);
            System.out.println("Take the ask at " + this.buy);
        }
        return this.buy;
        // Send the signal to the bittrex server and then check response
    }

    public void buyResponseHandling(int code) {
        if (code == 201) {
            /*
            this.buyMode = false;
            this.successfulBuy = true;
            this.buyBidMode = false;

             */
            System.out.println("Successful Buy 201 at " + this.buy + "\n" +
                    "this is the response " + code);
        }
    }

    public BigDecimal sellExit() {
        this.sell = BigDecimal.valueOf(bidDouble);
        /*
        this.sellBidMode = false;
         */
        System.out.println("Sell exited because last price dropped to low");
        return this.sell;
    }
    //if the bid double is less than the last double
    public boolean sellHodlSet() {
        // if (this.bidDouble < this.lastDouble) {
        String logs = hold() ? "buy signal not this.sell signal HOLD" : "no hodl";
        System.out.println(logs);
        return hold();
        // System.out.println("\n" + "Sell at " + this.sell + " vs bid " + liveMarketData.get("Bid"));
    }
        //System.out.println("\n Cancel last this.sell and Sell at " + this.sell + " bid is " +
          //  liveMarketData.get("Bid"));
    public BigDecimal sellCalculation() {
        // if the Bid is more than the last use the Last
        this.sell = this.sell.subtract(BigDecimal.valueOf(.00000001));
        this.sell = this.sell.setScale(8, RoundingMode.HALF_UP);
        //if no this.sell successful
        //send order
        return this.sell;
    }
    public boolean sellResponseHandling(int code) {
        if (code == 201) {
            //? and valid MACDCrossover?
            this.profit = this.sell.subtract(buy);
            if (this.profit.doubleValue() > 0d) {
                this.profit = this.profit.divide(buy, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100.0));
            }
            this.profitPercentageTotals += profit.doubleValue();
            System.out.println("Sell successful at " + this.sell + " " + "this.profit percent : " +
                    this.profit + "%" + "\n response: " + code);
            return true;
        }
        return false;
    }
    private boolean hold() {
        this.sell = BigDecimal.valueOf(this.lastDouble);
        this.sell = this.sell.subtract(BigDecimal.valueOf(.0000005));
        System.out.println("Last was chosen then subtracted from");
        if (this.bidDouble > this.askDouble) {
            this.sell = BigDecimal.valueOf(this.bidDouble);
            System.out.println("Bid was chosen");
        } else {
            this.sell = BigDecimal.valueOf(askDouble);
            this.sell = this.sell.subtract(BigDecimal.valueOf(.0000005));
            System.out.println("Ask was chosen then subtracted from");
        }
        if (this.sell.doubleValue() < buy.add(buy.multiply(BigDecimal.valueOf(.015)))
                .doubleValue() && this.sell.doubleValue() != 0) {
            System.out.println("Hold missed this.sell wait due to not enough profit");
            return false;
        }
        return true;
    }
}
/*send sell
try {
HttpResponse<String> response = sendOrder(this.sellRoutine(this.sell, bidDouble));
responseCode = response.statusCode();
} catch (IOException e) {
System.out.print("There was an IOException " + e + "\n" + "response : " +
responseCode);
      }

    try {
        HttpResponse<String> response = sendOrder(this.sellRoutine(this.sell, bidDouble));
        responseCode = response.statusCode();
    } catch (IOException e) {
        System.out.print("There was an IOException " + e + "\n response : " +
                responseCode);
    }
     */
