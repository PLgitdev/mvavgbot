package Live;

import lombok.Builder;

import java.io.IOException;
import java.net.*;

@Builder
public class Sell extends Transaction {


    @Override
    void fillOrKill() throws IOException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + mTwo + "-" + mOne +"?direction=SELL?quantity=" +
            quant + "?limit="+ limit + "?timeInForce=FILL_OR_KILL" + "?type=Limit");
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
    }
}
