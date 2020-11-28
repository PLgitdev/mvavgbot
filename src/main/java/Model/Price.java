package Model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        if(lMACDEMA != null) {
            this.mACD =  sMACDEMA - lMACDEMA ;
        }
    }
    public void updateSignalLine() {
        if (mACD != null) {
            int n = signalLine.size();
            double prev = signalLine.get(n - 1);
            this.signal = calculateEMA(mACD, prev, smoothing, 9);
            signalLine.add(signal); //create a signal line by making a EMA function
        }
    }
    public void initializeSignalLine() {
        int n = nineDaysOfClose.size();
            for(int i = 1; i < n; i++) {
                 double value = calculateEMA(shortMACDPeriod.get(i),shortMACDPeriod.get(i - 1), smoothing, 12) -
                     calculateEMA(longerMACDPeriod.get(i), longerMACDPeriod.get(i - 1), smoothing, 26);
                 signalLine.add(value);
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
    public boolean validMACDCrossover() {
        return validShortCrossover(mACD, signalLine.get(signalLine.size() -1));
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

    private Double emaMultiplier(Double smoothing, int period) {
        return (smoothing / (period + 1d));
    }
    private double calculateSMA(List<Double> a) {
        double n = a.size();
        double sum = 0.0;
        for(Double x : a) {
            sum += x;
        }
        return sum / n;
    }
    private double calculateEMA(Double v, Double p, Double smoothing, int period) {
        Double m = emaMultiplier(smoothing, period);
        return (v * m) + (p * (1 - m));
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
}
