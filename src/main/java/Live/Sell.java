package Live;

import lombok.Builder;

import java.io.IOException;
import java.net.*;

@Builder
public class Sell extends Transaction {
    private Double ceiling;
    private Sell(Double ceiling, String mOne, String mTwo, String type,
                Double limit, String timeInForce, String direction) {
        super();
        this.ceiling = ceiling;
    }

    public static Sell getInstance(Double ceiling, String mOne, String mTwo, String type,
                                  Double limit, String timeInForce, String direction) {
        return new Sell(ceiling, mOne, mTwo, type, limit, timeInForce, direction);
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
