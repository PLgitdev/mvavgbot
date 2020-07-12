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
    private ArrayList<Float> priceShorter;
    private ArrayList<Float> priceLonger;
    private Float totalShorter;
    private Float totalLonger;
    private Float price;
    private LocalDateTime timestamp;
    private LocalDateTime now = LocalDateTime.now();
    private int dateLimit = now.plusHours(24).getNano();

    public void addPriceShorter (Float price) {
        priceShorter.add(price);
    }
    public void addPriceLonger (Float price) {
        priceLonger.add(price);
    }

    public void addPrice(Float price) {
        priceShorter.add(price);
        priceLonger.add(price);
    }
    public boolean validBuyCrossover() {
        priceShorter.forEach( (price) -> totalShorter += price);
        priceLonger.forEach((price) -> totalLonger += price);
        float avgShorter = ( totalShorter / priceShorter.size() );
        float avgLonger = ( totalLonger / priceLonger.size() );
        return avgShorter <= avgLonger;
   }

  /*
    public boolean validSellCrossover() {
        priceShorter.forEach( (price) -> totalShorter += price);
        priceLonger.forEach((price) -> totalLonger += price);
        Float avgShorter = ( totalShorter / priceShorter.size() );
        Float avgLonger = ( totalLonger / priceLonger.size() );
        if (avgShorter <= avgLonger ) { return true; }
    }
    */
   public void dateLimitCheck() {
        if (LocalDateTime.now().getNano() > dateLimit) {
           priceShorter.remove(priceShorter.size()-1);
           priceLonger.remove(priceLonger.size()-1);
       }
   }
}
