package Live;

import lombok.Builder;

import java.io.IOException;
import java.net.*;

@Builder
public class Sell extends Transaction {
    private Sell(Double quant, String mOne, String mTwo, String type,
                Double limit, String timeInForce, String direction) {
        super();
    }

    public static Sell getInstance(Double quant, String mOne, String mTwo, String type,
                                  Double limit, String timeInForce, String direction) {
        return new Sell(quant, mOne, mTwo, type, limit, timeInForce, direction);
    }
    @Override
    public int fillOrKill() throws IOException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction="+ direction + "?quantity=" +
            quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }
}
