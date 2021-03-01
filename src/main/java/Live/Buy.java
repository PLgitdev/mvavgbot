package Live;

import com.example.movingaverage.Global;
import org.aspectj.bridge.Message;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Buy extends Transaction {
    private Double quant;
    private Buy(String type,
                 Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.quant = Global.quant;
        this.timestamp = LocalDateTime.now();
        this.content.put("marketSymbol", mOne + "-" + mTwo );
        this.content.put("direction", direction);
        this.content.put("quantity", quant);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.content.put("type", type);
        this.uri = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction=" + direction + "?quantity=" +
            quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
    }

    public static Buy getInstance(String type, Double limit, String timeInForce, String direction) throws MalformedURLException {
        return new Buy(type,limit,timeInForce, direction);
    }

    @Override
    public int fillOrKill() throws IOException, NoSuchAlgorithmException {
        URLConnection con = uri.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        setContentHash();
        setHeaders(http);
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }
}
