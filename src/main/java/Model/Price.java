package Model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
        this.priceShorter.add(price);
    }
    public void addPriceLonger (Double price) {
        this.priceLonger.add(price);
    }

    public void addBothPrices(Double price) {
        this.priceShorter.add(price);
        this.priceLonger.add(price);
    }
    public boolean validBuyCrossover() {
        priceShorter.forEach( (p) -> totalShorter += p);
        priceLonger.forEach((p) -> totalLonger += p);
        avgShorter = ( totalShorter / priceShorter.size() );
        avgLonger = ( totalLonger / priceLonger.size() );

        return (avgShorter > avgLonger) && price != null;
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
}
