package com.example.movingaverage.Live;

import com.example.movingaverage.Global;

import java.net.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Sell extends Transaction {
    private Double ceiling;
    private Sell(String type,Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.content = new HashMap<>();
        this.ceiling = Global.quant;
        this.timestamp = System.currentTimeMillis() / 1000L;
        this.content.put("ceiling", ceiling);
        this.content.put("marketSymbol", Global.mOne + "-" + Global.mTwo);
        this.content.put("direction", direction);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.content.put("type", type);
        this.sendUri = new URL("https://api.bittrex.com/v3/orders");
        this.uri = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + Global.mTwo + "-" + Global.mOne +"&direction="+ direction + "&ceiling=" +
            ceiling + "&limit="+ limit + "&timeInForce="+ timeInForce + "&type=" + type);
    }

    public static Sell getInstance(String type, Double limit,
                                   String timeInForce, String direction) throws MalformedURLException {
        return new Sell(type, limit, timeInForce, direction);
    }
}
