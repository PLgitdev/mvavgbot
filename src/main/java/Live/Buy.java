package Live;

import com.example.movingaverage.Global;
import java.net.*;
import java.time.LocalDateTime;

public class Buy extends Transaction {
    private Double quant;
    private Buy(String type,
                 Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.quant = Global.quant;
        this.timestamp = LocalDateTime.now();
        setContent();
        this.content.put("quantity", quant);
        this.uri = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction=" + direction + "?quantity=" +
            quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
    }

    public static Buy getInstance(String type, Double limit,
                                  String timeInForce, String direction) throws MalformedURLException {
        return new Buy(type,limit,timeInForce, direction);
    }
}
