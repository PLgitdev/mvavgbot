package com.example.movingaverage.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
This is the price object. The price object is in charge of calculating the indicators from the incoming data.

Current Indicators:

Simple moving average                     - Average calculated for each of the time periods set by the console input
The moving average convergence divergence - A weighted average using a classical 12, 26, close strategy derived from SMA
The signal line                           - Take the EMA of 9 days of the closing price

This object will be updated by adding Last prices from incoming HOLC data from the server every iteration it will then
make candles based on candle size global input.

Once the data has has expired it will be removed
    For example:  the program has been running longer than one day and needs to recalculate the indicators

This object uses methods that return boolean values based on inequities between the indicators
    For example: Buy signal will be affected by the boolean value between the signal line and the MACD (mACD)
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
    private ArrayList<Double> priceShorter;
    private ArrayList<Double> priceLonger;
    private List<Double> signalLine;
    private List<Double> nineDaysOfClose;
    private List<Double> twelveDayRibbons;
    private List<Double> twentySixDayRibbons;
    private List<Double> shortMACDPeriod;
    private List<Double> longerMACDPeriod;

    public void addPriceShorter (Double price) {
        this.priceShorter.add(price);
    }
    public void addPriceLonger (Double price) {
        this.priceLonger.add(price);
    }

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
        //ArrayDeque chosen to reduce space complexity required by a D Linked List
        Deque<Double> twelveDayDeque = new ArrayDeque<>(twelveDayRibbons);
        if (!this.twelveDayRibbons.isEmpty()) {
            double prev = twelveDayDeque.pop();
            this.sMACDEMA = calculateEMA(currentPrice, prev, smoothing,12);
            this.twelveDayRibbons.add((sMACDEMA));
        }
        else {
            this.twelveDayRibbons.add(calculateSMA(shortMACDPeriod));
        }
    }

    public void setLMACDEMA() {
        Deque<Double> twentyDayDeque = new ArrayDeque<>(twentySixDayRibbons);
        if (!this.twentySixDayRibbons.isEmpty()) {
            double prev = twentyDayDeque.pop();
            this.lMACDEMA  = calculateEMA(currentPrice, prev, smoothing,26);
            this.twentySixDayRibbons.add((lMACDEMA));
        }
        else {
            this.twentySixDayRibbons.add(calculateSMA(longerMACDPeriod));
        }
    }

    public void setSMA() {
        this.avgShorter = calculateSMA(priceShorter);
        this.avgLonger =  calculateSMA(priceLonger);
    }

    public void initializeSignalLine() {
        int n = nineDaysOfClose.size();
        // MACD periods 12, 26
        LinkedList<Double> sMACD = new LinkedList<>(shortMACDPeriod);
        LinkedList<Double> lMACD = new LinkedList<>(longerMACDPeriod);
        LinkedList<Double> temp = new LinkedList<>();

        for (int i = 1; i < n; i++) {
            double value = calculateEMA(sMACD.get(i),sMACD.get(i - 1), smoothing, 12) -
                calculateEMA(lMACD.get(i), lMACD.get(i - 1), smoothing, 26);
            temp.add(value);
        }
        for (int i = 1; i < temp.size(); i++) {
            Double s = calculateEMA(temp.get(i),temp.get(i - 1),smoothing,9);
            this.signalLine.add(s);
        }
    }

    public void updateSignalLine() {
        if (this.mACD != null) {
            //LIFO linked list
            Deque<Double> linkedSignalLine = new LinkedList<>(signalLine);
            double prev = linkedSignalLine.pop();
            this.signal = calculateEMA(mACD, prev, smoothing, 9);
            this.signalLine.add(signal); //create a signal line by calculating the EMA function
        }
    }

    public void dateLimitCheck(int x) {
        if(LocalDateTime.now().getDayOfWeek().compareTo(dateLimit.getDayOfWeek()) < 0) {
            priceShorter.remove(priceShorter.size() - x);
        }
    }

    public void dateLimitCheckLonger(int x) {
        if(LocalDateTime.now().getDayOfWeek().compareTo(dateLimit.getDayOfWeek()) < 0) {
            priceLonger.remove(priceLonger.size() - x);
        }
    }


    // Unused in current strategy

    public boolean validSMACrossover() {
        return validShortCrossover(avgShorter,avgLonger);
    }

    public boolean validSMABackCross() { return  validLongerCrossover(avgShorter,avgLonger); }

    public boolean validMACDCrossover() {
        return validShortCrossover(this.mACD, this.signal);
    }

    public boolean validMACDBackCross() {
        return validLongerCrossover(this.mACD, this.signal);
    }

    //-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    private Double emaMultiplier(Double smoothing, int period) {
        return (smoothing / (period + 1d));
    }

    private double calculateSMA(List<Double> l) {
        int n = l.size();
        Double[] a = new Double[n];
        a = l.toArray(a);
        double sum = binarySum(a, 0, a.length - 1);
        BigDecimal bigSum = BigDecimal.valueOf(sum);
        return bigSum.divide(BigDecimal.valueOf(n), 8,  RoundingMode.HALF_UP).doubleValue();
    }

    private double calculateEMA(Double current, Double previous, Double smoothing, int period) {
        Double multiplier = emaMultiplier(smoothing, period);
        return (current * multiplier) + (previous * (1 - multiplier));
    }
    private boolean validShortCrossover(Double a, Double b) {
        if (a != null &&  b != null) {
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
            ", sMACDEMA=" + sMACDEMA +
            ", lMACDEMA=" + lMACDEMA +
            ", signal=" + signal +
            ", timestamp=" + timestamp +
            ", dateLimit=" + dateLimit +
            ", priceShorter=" + priceShorter +
            ", priceLonger=" + priceLonger +
            ", signalLine=" + signalLine +
            ", nineDaysOfClose=" + nineDaysOfClose +
            ", twelveDayRibbons=" + twelveDayRibbons +
            ", twentySixDayRibbons=" + twentySixDayRibbons +
            ", shortMACDPeriod=" + shortMACDPeriod +
            ", longerMACDPeriod=" + longerMACDPeriod +
            '}';
    }
}
