package Model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;

@EqualsAndHashCode
@Builder
@Data
public class Price {
    private ArrayList<Double> priceShorter;
    private ArrayList<Double> priceLonger;
    private Double totalShorter;
    private Double totalLonger;
    private Double price;
    private LocalDateTime timestamp;
    private LocalDateTime dateLimit;
    private Double avgShorter;
    private Double avgLonger;

    public void addPriceShorter (Double price) {
        priceShorter.add(price);
    }
    public void addPriceLonger (Double price) {
        priceLonger.add(price);
    }

    public void addPrice(Double price) {
        priceShorter.add(price);
        priceLonger.add(price);
    }
    public boolean validBuyCrossover() {
        priceShorter.forEach( (p) -> totalShorter += p);
        priceLonger.forEach((p) -> totalLonger += p);
        this.avgShorter = ( totalShorter / priceShorter.size() );
        this.avgLonger = ( totalLonger / priceLonger.size() );
        return avgShorter >= avgLonger;
   }


    /*public boolean runTha() {
        this.avgShorter
    }
     */

   public void dateLimitCheck(int x) {
        if (LocalDateTime.now().compareTo(dateLimit) > 0) {
           priceShorter.remove(priceShorter.size()-x);
           priceLonger.remove(priceLonger.size()-x);
       }
   }
}
