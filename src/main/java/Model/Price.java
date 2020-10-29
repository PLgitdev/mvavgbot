package Model;

import Controller.MongoCRUD;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode
@Builder
@Data
public class Price {
    private ArrayList<Double> priceShorter;
    private ArrayList<Double> priceLonger;
    private Double totalShorter;
    private Double totalLonger;
    //private Double price;
    private LocalDateTime timestamp;
    private LocalDateTime dateLimit;
    private Double avgShorter;
    private LinkedList<Double> avgShorterList;
    private Double avgLonger;
    private LinkedList<Double> avgLongerList;
    private Double emaMultiplier;
    private Double currentEMA;
    private ArrayList<Double> ema;

    public void addPriceShorter (Double price) {
        this.priceShorter.add(price);
    }
    public void addPriceLonger (Double price) {
        this.priceLonger.add(price);
    }

    public void addBothPrices(Double price) {
        this.priceShorter.add(price);
        this.priceLonger.add(price);
    }

    public void takeAvg() {
        totalShorter = 0.0;
        totalLonger = 0.0;
        priceShorter.forEach( (p) -> totalShorter = totalShorter + p);
        priceLonger.forEach((p) -> totalLonger = totalLonger + p);
        this.avgShorter = totalShorter / priceShorter.size();
        this.avgLonger = totalLonger / priceLonger.size();
        avgShorterList.add(avgShorter);
        avgLongerList.add(avgLonger);
    }

    public boolean validSMACrossover() {
        return validCrossover(avgShorter,avgLonger);
    }

    public void calculateCurrentEMA() {
        emaMultiplier = (2 / (double) (priceLonger.size() + 1));
        if (ema.size() > 0) {
            this.currentEMA = (priceLonger.get(priceLonger.size() - 1) * emaMultiplier) +
                (ema.get(ema.size() - 1) * (1 - emaMultiplier));
            ema.add(currentEMA);
        }
        else ema.add(priceLonger.get(priceLonger.size() -1));
    }

   public void dateLimitCheck(int x) {
       if (LocalDateTime.now().compareTo(dateLimit) > 0) {
           priceShorter.remove(priceShorter.size() - x);
       }
   }

   public void dateLimitCheckLonger(int x) {
       if (LocalDateTime.now().compareTo(dateLimit) > 0) {
            priceLonger.remove(priceLonger.size()-x);
       }
   }

   private boolean vaildExpansion(List<Double> ma, int amt) {
        int counter = 0;
        for (int i = 0; i < ma.size(); i++) {
            if (i > 0 && (ma.get(i) > ma.get(i - 1))) {
                counter++;
            }
        }
        return  counter == amt;
    }
   private boolean validContraction(LinkedList<Double> ma,int amt) {
        int counter = 0;
       for (int i = 0; i < ma.size(); i++) {
           if (i > 0 && (ma.get(i) < ma.get(i - 1))) {
               counter++;
           }
       }
       return counter == amt;

   }

    public boolean validCrossover(Double mas, Double mal) {
        if (mas != null &&  mal != null) {
            return mas > mal;
        }
        return false;
    }
}
