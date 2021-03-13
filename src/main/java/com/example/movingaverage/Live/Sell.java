package com.example.movingaverage.Live;

import com.example.movingaverage.Global;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class Sell extends Transaction {
    private Double ceiling;
    private Sell(String type,Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.content = new HashMap<>();
        this.ceiling = Global.quant;
        this.content.put("marketSymbol", Global.mTwo + "-" + Global.mOne);
        this.content.put("direction", direction);
        this.content.put("type", type);
        this.content.put("ceiling", "0.00000100");
        this.content.put("limit", limit.toString());
        this.content.put("timeInForce", timeInForce);
        this.sendUri = new URL("https://api.bittrex.com/v3/orders");
    }

    public static Sell getInstance(String type, Double limit,
                                   String timeInForce, String direction) throws MalformedURLException {
        return new Sell(type, limit, timeInForce, direction);
    }
}
