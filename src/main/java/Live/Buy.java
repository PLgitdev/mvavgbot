package Live;

import net.bytebuddy.implementation.bind.annotation.Super;

import java.io.IOException;
import java.net.*;

public class Buy extends Transaction {
    private Buy(Double quant, String mOne, String mTwo, String type,
               Double limit, String timeInForce) {
        super();
    }

    public static Buy getInstance(Double quant, String mOne, String mTwo, String type,
                                  Double limit, String timeInForce) {
        return new Buy(quant,mOne,mTwo,type,limit,timeInForce);
    }
    @Override
    public int fillOrKill() throws IOException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
                + mTwo + "-" + mOne +"?direction=BUY?quantity=" +
                quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }
}
