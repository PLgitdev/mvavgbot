package Live;

import com.example.movingaverage.Global;

import java.io.IOException;
import java.net.*;

public class Sell extends Transaction {
    private Double ceiling;
    private Sell(String type,Double limit, String timeInForce, String direction) {
        super();
        this.ceiling = Global.quant;
    }

    public static Sell getInstance(String type,Double limit, String timeInForce, String direction) {
        return new Sell(type, limit, timeInForce, direction);
    }
    @Override
    public int fillOrKill() throws IOException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction="+ direction + "?ceiling=" +
            ceiling + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }
}
