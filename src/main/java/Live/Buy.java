package Live;

import com.example.movingaverage.Global;

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;

public class Buy extends Transaction {
    private Double quant;
    private Buy(String type,
                 Double limit, String timeInForce, String direction) {
        super();
        this.quant = Global.quant;
        this.timestamp = LocalDateTime.now();
    }

    public static Buy getInstance(String type, Double limit, String timeInForce, String direction) {
        return new Buy(type,limit,timeInForce, direction);
    }
    @Override
    public int fillOrKill() throws IOException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
                + mTwo + "-" + mOne +"?direction=" + direction + "?quantity=" +
                quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        setHeaders(http);
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }

    @Override
    public void setHeaders(HttpURLConnection http) {

        http.setRequestProperty("Api-Key","API-KEY");
        http.setRequestProperty("Api-Timestamp", timestamp.toString());
        http.setRequestProperty("Api-Content-Hash","API-CONTENT-HASH");
        http.setRequestProperty("Api-Signature","API-SIGNATURE");
    }
}
