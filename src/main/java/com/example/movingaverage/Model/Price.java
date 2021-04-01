package com.example.movingaverage.Model;
import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode
@Builder
@Data
@VisibleForTesting
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
        if(lMACDEMA != null) {
            this.mACD =  sMACDEMA - lMACDEMA ;
        }
    }
    public void updateSignalLine() {
        if(this.mACD != null) {
            int n = signalLine.size();
            double prev = signalLine.get(n - 1);
            this.signal = calculateEMA(mACD, prev, smoothing, 9);
            signalLine.add(signal); //create a signal line by making a EMA function
        }
    }
    public void setSMACDEMA() {
        if(twelveDayRibbons.size() > 0) {
            double prev = twelveDayRibbons.get(twelveDayRibbons.size() - 1);
            this.sMACDEMA = calculateEMA(currentPrice,prev,smoothing,12);
            twelveDayRibbons.add((sMACDEMA));
        }
        else {
            twelveDayRibbons.add(calculateSMA(shortMACDPeriod));
        }
    }
    //LinkedList!!!!
    public void setLMACDEMA() {
        if(twentySixDayRibbons.size() > 0) {
            double prev = twentySixDayRibbons.get(twentySixDayRibbons.size() - 1);
            this.lMACDEMA  = calculateEMA(currentPrice,prev,smoothing,26);
            twentySixDayRibbons.add((lMACDEMA));
        }
        else {
            twentySixDayRibbons.add(calculateSMA(longerMACDPeriod));
        }
    }
    public void setSMA() {
        this.avgShorter = calculateSMA(priceShorter);
        this.avgLonger =  calculateSMA(priceLonger);
    }
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
    public void dateLimitCheck(int x) {
        if(LocalDateTime.now().compareTo(dateLimit) > 0) {
            priceShorter.remove(priceShorter.size() - x);
        }
    }
    public void dateLimitCheckLonger(int x) {
        if (LocalDateTime.now().compareTo(dateLimit) > 0) {
            priceLonger.remove(priceLonger.size() - x);
        }
    }
    //LinkedList ????
    public void initializeSignalLine() {
        int n = nineDaysOfClose.size();
        ArrayList<Double> temp = new ArrayList<>();
        for(int i = 1; i < n; i++) {
            double value = calculateEMA(shortMACDPeriod.get(i),shortMACDPeriod.get(i - 1), smoothing, 12) -
                calculateEMA(longerMACDPeriod.get(i), longerMACDPeriod.get(i - 1), smoothing, 26);
                temp.add(value);
        }
        for(int i = 1; i < temp.size(); i++) {
            Double s = calculateEMA(temp.get(i),temp.get(i - 1),smoothing,9);
            signalLine.add(s);
        }
    }

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
    private double calculateEMA(Double b, Double a, Double smoothing, int period) {
        Double m = emaMultiplier(smoothing, period);
        return (b * m) + (a * (1 - m));
    }
    private boolean validShortCrossover(Double a, Double b) {
        if(a != null &&  b != null) {
            return a > b;
        }
        return false;
    }
    private boolean validLongerCrossover(Double a, Double b) {
        if(a != null &&  b != null) {
            return a < b;
        }
        return false;
    }
    private Double binarySum(Double[] data, int b, int e) {
        Arrays.sort(data);
        if(b > e) {
            throw new IllegalArgumentException();
        }
        else if(b == e) {
            return data[b];
        }
        else {
            int m = (b + e) / 2;
            return binarySum(data, b, m) + binarySum(data, m + 1, e);
        }
    }
    //what if it kept trying different amts
    //when there is a valid contraction after a valid sma crossover
// ?? random idea i had basically will reach a vaLid expansion for a certain amount set

    /*
    private boolean validExpansion(List<Double> ma, int amt) {
        int counter = 0;
        for (int i = 0; i < ma.size(); i++) {
            if (i > 0 && (ma.get(i) > ma.get(i - 1))) {
                counter++;
            }
        }
        return  counter == amt;
    }
    private boolean validContraction(List<Double> ma,int amt) {
        int counter = 0;
       for (int i = 0; i < ma.size(); i++) {
           if (i > 0 && (ma.get(i) < ma.get(i - 1))) {
               counter++;
           }
       }
       return counter == amt;

   }
   */
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
