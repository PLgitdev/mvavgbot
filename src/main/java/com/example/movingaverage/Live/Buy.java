package com.example.movingaverage.Live;

import com.example.movingaverage.Global;
import com.example.movingaverage.Keys;
import com.google.api.client.http.HttpHeaders;

import java.net.*;
import java.time.Instant;
import java.util.HashMap;

public class Buy extends Transaction {
    private Double quant;
    private Buy(String type,
                 Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.content = new HashMap<>();
        this.quant = Global.quant;
        this.content.put("marketSymbol", Global.mOne + "-" + Global.mTwo);
        this.content.put("direction", direction);
        this.content.put("type", type);
        this.content.put("quantity", quant);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.sendUri = new URL("https://api.bittrex.com/v3/orders");
        this.uri = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + Global.mOne + "-" + Global.mTwo +"&direction=" + direction + "&type=" + type + "&quantity=" +
            quant + "&limit="+ limit + "&timeInForce="+ timeInForce );
    }

    public static Buy getInstance(String type, Double limit,
                                  String timeInForce, String direction) throws MalformedURLException {
        return new Buy(type,limit,timeInForce, direction);
    }
}
