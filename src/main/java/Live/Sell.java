package Live;

import com.example.movingaverage.Global;
import java.net.*;
import java.time.LocalDateTime;

public class Sell extends Transaction {
    private Double ceiling;
    private Sell(String type,Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.ceiling = Global.quant;
        this.timestamp = LocalDateTime.now();
        setContent();
        this.content.put("ceiling", ceiling);
        this.uri = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction="+ direction + "?ceiling=" +
            ceiling + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
    }

    public static Sell getInstance(String type, Double limit, String timeInForce, String direction) throws MalformedURLException {
        return new Sell(type, limit, timeInForce, direction);
    }
}
