package Live;

import com.example.movingaverage.Global;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Sell extends Transaction {
    private Double ceiling;
    private Sell(String type,Double limit, String timeInForce, String direction) {
        super();
        this.ceiling = Global.quant;
        this.timestamp = LocalDateTime.now();
        this.content.put("marketSymbol", mOne + "-" + mTwo );
        this.content.put("direction", direction);
        this.content.put("ceiling", ceiling);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.content.put("type", type);
    }

    public static Sell getInstance(String type,Double limit, String timeInForce, String direction) {
        return new Sell(type, limit, timeInForce, direction);
    }
    @Override
    public int fillOrKill() throws IOException, NoSuchAlgorithmException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction="+ direction + "?ceiling=" +
            ceiling + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        setContentHash();
        setHeaders(http);
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }
}
