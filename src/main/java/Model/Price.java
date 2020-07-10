package Model;

import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@EqualsAndHashCode
@Builder
public class Price {
    private ArrayList<Float> price30;
    private ArrayList<Float> price90;
    private Float total30;
    private Float total90;
    private Float price;
    private LocalDateTime now = LocalDateTime.now();
    private int dateLimit = now.plusHours(24).getNano();

    public void addPrice() {
        price30.add(price);
        price90.add(price);
    }
    public boolean validBuyCrossover() {
        price30.forEach( (price) -> total30 += price);
        price90.forEach((price) -> total90 += price);
        Float avg30 = ( total30 / price30.size() );
        Float avg90 = ( total90 / price90.size() );
        if (avg30 >= price.floatValue()) {

        }
        if (avg90)

   }
   public void dateLimitCheck() {
        if (LocalDateTime.now().getNano() > dateLimit) {
           price30.remove(price30.size()-1);
           price90.remove(price90.size()-1);
       }
   }
}
