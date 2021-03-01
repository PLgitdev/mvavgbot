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
                 Double limit, String timeInForce, String direction) {
        super();
        this.quant = Global.quant;
        this.timestamp = LocalDateTime.now();
        this.content.put("marketSymbol", mOne + "-" + mTwo );
        this.content.put("direction", direction);
        this.content.put("quantity", quant);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.content.put("type", type);
    }

    public static Buy getInstance(String type, Double limit, String timeInForce, String direction) {
        return new Buy(type,limit,timeInForce, direction);
    }
    @Override
    public int fillOrKill() throws IOException, NoSuchAlgorithmException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
                + mTwo + "-" + mOne +"?direction=" + direction + "?quantity=" +
                quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        String contentH = contentHash();
        setHeaders(http, contentH);
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }

    @Override
    public void setHeaders(HttpURLConnection http, String contentH) {
        http.setRequestProperty("Api-Key","API-KEY");
        http.setRequestProperty("Api-Timestamp", timestamp.toString());
        http.setRequestProperty("Api-Content-Hash",contentH);
        http.setRequestProperty("Api-Signature","API-SIGNATURE");
    }

    @Override
    public String contentHash() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] messageDigest = md.digest(content.toString().getBytes());
        BigInteger signumRep = new BigInteger(1, messageDigest);
        StringBuilder hash = new StringBuilder(signumRep.toString(16));
        while(hash.length() < 32)  {
            hash.insert(0, "0");
        }
        return hash.toString();
    }

}
