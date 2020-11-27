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
    private int smoothing;
    private Double currentPrice;
    private Double avgShorter;
    private Double avgLonger;
    private Double mACD;
    private Double sMACDEMA;
    private Double lMACDEMA;
    private Double prev;
    private LocalDateTime timestamp;
    private LocalDateTime dateLimit;
    private ArrayList<Double> priceShorter;
    private ArrayList<Double> priceLonger;
    private List<Double> signalLine;
    private List<Double> twelveDayMACDA;
    private List<Double> twentySixDayMACDA;
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
        if(this.lMACDEMA != null) {
            this.mACD =  this.sMACDEMA - this.lMACDEMA ;
        }
    }
    /* public void upadateSignalLine() {
        if(signalLine.size() < 9) {

            signalLine.add(); //create a signal line by making a EMA function
        }

    }
     */
    public void setSMACDEMA() {
        if(twelveDayMACDA.size() > 0) {
            prev = twentySixDayMACDA.get(twentySixDayMACDA.size() - 1);
            this.sMACDEMA = calculateEMA(currentPrice,prev,smoothing, 12);
            twelveDayMACDA.add((this.sMACDEMA));
        }
        else {
            twelveDayMACDA.add(calculateSMA(shortMACDPeriod));
        }
    }
    public void setLMACDEMA() {
        if(twentySixDayMACDA.size() > 0) {
            prev = twentySixDayMACDA.get(twentySixDayMACDA.size() - 1);
            this.lMACDEMA  = calculateEMA(currentPrice,prev,smoothing,26);
            twentySixDayMACDA.add((this.lMACDEMA));
        }
        else {
            twentySixDayMACDA.add(calculateSMA(longerMACDPeriod));
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
        return validShortCrossover(sMACDEMA, lMACDEMA);
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

    private double emaMultiplier(int smoothing, double period) {
        return (smoothing / (period + 1.0));
    }
    private double calculateSMA(List<Double> a) {
        Double sum = 0.0;
        for(Double x : a) {
            sum += x;
        }
        return sum / a.size();
    }
    private double calculateEMA(Double v, Double p, int smoothing, int period) {
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
        if (a != null &&  b != null) {
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
