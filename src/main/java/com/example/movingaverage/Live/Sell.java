package com.example.movingaverage.Live;

import com.example.movingaverage.Global;
import com.example.movingaverage.Keys;
import com.google.api.client.http.HttpHeaders;

import java.net.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Sell extends Transaction {
    private Double ceiling;
    private Sell(String type,Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.content = new HashMap<>();
        this.ceiling = Global.quant;
        this.content.put("marketSymbol", Global.mOne + "-" + Global.mTwo);
        this.content.put("direction", direction);
        this.content.put("type", type);
        this.content.put("celing", ceiling.toString());
        this.content.put("limit", limit.toString());
        this.content.put("timeInForce", timeInForce);
        this.sendUri = new URL("https://api.bittrex.com/v3/orders");
    }

    public static Sell getInstance(String type, Double limit,
                                   String timeInForce, String direction) throws MalformedURLException {
        return new Sell(type, limit, timeInForce, direction);
    }
}
