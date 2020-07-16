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
    private int dateLimit;

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
        Double avgShorter = ( totalShorter / priceShorter.size() );
        Double avgLonger = ( totalLonger / priceLonger.size() );
        return avgShorter >= avgLonger;
   }


    public boolean validSellCrossover() {
        priceShorter.forEach( (price) -> totalShorter += price);
        priceLonger.forEach((price) -> totalLonger += price);
        Double avgShorter = ( totalShorter / priceShorter.size() );
        Double avgLonger = ( totalLonger / priceLonger.size() );
        return  (avgShorter <= avgLonger );
    }

   public void dateLimitCheck(int x) {
        if (LocalDateTime.now().getNano() > dateLimit) {
           priceShorter.remove(priceShorter.size()-x);
           priceLonger.remove(priceLonger.size()-x);
       }
   }
}
