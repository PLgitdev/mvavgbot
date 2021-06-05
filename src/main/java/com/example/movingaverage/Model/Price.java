package com.example.movingaverage.Model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 - This is the price class. Once created the price object is in charge of calculating the indicators from the
 - incoming data.
 -
 -
 - Current Indicators:
 -
 - Simple moving average                     - Average calculated for each of the time periods set by the console input
 - The moving average convergence divergence - A weighted average using a classical 12, 26, close strategy
 - The signal line                           - Take the EMA of 9 days of the closing price
 -
 - This object will be updated by adding Last prices from incoming HLOC data from the server each iteration it will then
 - create values that define indicators based on candle size global input.
 -
 - Once the data has has expired it will be removed
 -     For example:  the program has been running longer than one day and needs to recalculate the indicators
 -
 - This object uses methods that return boolean values based on inequities between the indicators
 -     For example: Buy signal will be affected by the boolean value between the signal line and the MACD (mACD)
 -
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

@EqualsAndHashCode
@Builder
@Data
public class Price {
    private Double smoothing;
    private Double currentPrice;
    private Double avgShorter;
    private Double avgLonger;
    private Double mACD;
    private Double sMACDEMA;
    private Double lMACDEMA;
    private Double signal;
    private LocalDateTime timestamp;
    private LocalDateTime dateLimit;
    private List<Double> priceShorter;
    private List<Double> priceLonger;
    private List<Double> signalLine;
    private List<Double> nineDaysOfClose;
    private List<Double> twelveDayRibbons;
    private List<Double> twentySixDayRibbons;
    private List<Double> shortMACDPeriod;
    private List<Double> longerMACDPeriod;

    public void init() {
        //To calculate EMA you must use the SMA using close of last period as the initial value
        Deque<Double> sMACD = new LinkedList<>(shortMACDPeriod);
        Deque<Double> lMACD = new LinkedList<>(longerMACDPeriod);
        Deque<Double> temp = new LinkedList<>();
        double previousShortSMA = calculateSMA(shortMACDPeriod);
        double previousLongSMA = calculateSMA(shortMACDPeriod);
        double currentLongValue;
        double currentShortValue;
        double tempValue;
        double value;
        double signal;

        //set up the initial short EMA
        this.sMACDEMA = calculateEMA(currentPrice, previousShortSMA, smoothing,12);
        this.twelveDayRibbons.add((this.sMACDEMA));

        //set up the initial long EMA
        this.lMACDEMA = calculateEMA(currentPrice, previousLongSMA, smoothing, 26);
        this.twentySixDayRibbons.add(this.lMACDEMA);

        //initialize the signal line.. beware the min and 5 min candle cannot hold all the data in one List
        while (temp.size() < nineDaysOfClose.size() - 1) {
            currentShortValue = sMACD.pop();
            currentLongValue = lMACD.pop();
            value = calculateEMA(currentShortValue, sMACD.peek(), smoothing, 12) -
                calculateEMA(currentLongValue, lMACD.peek(), smoothing, 26);
            temp.push(value);
        }
        while (temp.size() > 1) {
            tempValue = temp.pop();
            signal = calculateEMA(tempValue,temp.peek(),smoothing,9);
            this.signalLine.add(signal);
        }
    }

    //Add a price every iteration individually
    public void addPriceShorter(Double price) {
        this.priceShorter.add(price);
    }
    public void addPriceLonger(Double price) {
        this.priceLonger.add(price);
    }

    //Set all prices at once
    public void setPrices(Double price) {
        this.priceShorter.add(price);
        this.priceLonger.add(price);
        this.currentPrice = price;
    }

    public void setMACD() {
        if (lMACDEMA != null) {
            this.mACD =  sMACDEMA - lMACDEMA ;
        }
    }

    public void setSMACDEMA() {
        Deque<Double> twelveDayDeque = new ArrayDeque<>(twelveDayRibbons);
        this.sMACDEMA = calculateEMA(currentPrice, twelveDayDeque.peek(), smoothing,12);
        this.twelveDayRibbons.add((sMACDEMA));
    }

    public void setLMACDEMA() {
        Deque<Double> twentySixDayDeque = new ArrayDeque<>(twentySixDayRibbons);
        this.lMACDEMA  = calculateEMA(currentPrice, twentySixDayDeque.peek(), smoothing,26);
        this.twentySixDayRibbons.add(this.lMACDEMA);
    }

    public void setSMA() {
        this.avgShorter = calculateSMA(priceShorter);
        this.avgLonger =  calculateSMA(priceLonger);
    }

    public void setSignalLine() {
        Deque<Double> signalDeque = new LinkedList<>(signalLine);

        if (this.mACD != null) {
            this.signal = calculateEMA(mACD, signalDeque.peek(), smoothing, 9);
            this.signalLine.add(signal); //create a signal line
        }
    }
    //RSI !


    //check for expired data
    public void dateLimitCheck(int x) {
        if (LocalDateTime.now().getDayOfWeek().compareTo(dateLimit.getDayOfWeek()) > 0) {
            priceShorter.remove(priceShorter.size() - x);
        }
    }

    public void dateLimitCheckLonger(int x) {
        if (LocalDateTime.now().getDayOfWeek().compareTo(dateLimit.getDayOfWeek()) > 0) {
            priceLonger.remove(priceLonger.size() - x);
        }
    }

    public boolean validMACDCrossover() {
        return validShortCrossover(this.mACD, this.signal);
    }

    // Unused in current strategy

    public boolean validSMACrossover() {
        return validShortCrossover(this.avgShorter,this.avgLonger);
    }

    public boolean validSMABackCross() { return  validLongerCrossover(this.avgShorter,this.avgLonger); }

    public boolean validMACDBackCross() {
        return validLongerCrossover(this.mACD, this.signal);
    }

    //-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private Double emaMultiplier(Double smoothing, int period) {
        return (smoothing / (period + 1d));
    }

    private double calculateSMA(List<Double> data) {
        int n = data.size();
        Double[] a = new Double[n];
        a = data.toArray(a);
        double sum = binarySum(a, 0, a.length - 1);
        BigDecimal bigSum = BigDecimal.valueOf(sum);
        return bigSum.divide(BigDecimal.valueOf(n), 8,  RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateEMA(Double current, Double previousEMA, Double smoothing, int period) {
        Double multiplier = emaMultiplier(smoothing, period);
        return (current * multiplier) + (previousEMA * (1 - multiplier));
    }

    private boolean validShortCrossover(Double a, Double b) {
        if (a != null && b != null) {
            return a > b;
        }
        return false;
    }

    private boolean validLongerCrossover(Double a, Double b) {
        if (a != null &&  b != null) {
            return a < b;
        }
        return false;
    }

    private Double binarySum(Double[] data, int b, int e) {
        Arrays.sort(data);
        if (b > e) {
            throw new IllegalArgumentException();
        }
        else if (b == e) {
            return data[b];
        }
        else {
            int m = (b + e) / 2;
            return binarySum(data, b, m) + binarySum(data, m + 1, e);
        }
    }

    @Override
    public String toString() {
        return "Price{" +
            "smoothing=" + smoothing +
            ", currentPrice=" + currentPrice +
            ", avgShorter=" + avgShorter +
            ", avgLonger=" + avgLonger +
            ", mACD=" + mACD +
            ", signal=" + signal +
            ", timestamp=" + timestamp +
            ", dateLimit=" + dateLimit +
            '}';
    }
}
