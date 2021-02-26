package Live;

import lombok.Builder;

import java.io.IOException;
import java.net.*;

@Builder
public class Buy extends Transaction {


    @Override
    void fillOrKill() throws IOException {
        URL fOKURL = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
                + mTwo + "-" + mOne +"?direction=BUY?quantity=" +
                quant + "?limit="+ limit + "?timeInForce=FILL_OR_KILL");
        URLConnection con = fOKURL.openConnection();
        HttpURLConnection http = (HttpURLConnection)con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
    }
}
