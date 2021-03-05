package com.example.movingaverage.Live;

import com.example.movingaverage.Global;

import java.net.*;
import java.util.HashMap;

public class Buy extends Transaction {
    private Double quant;
    private Buy(String type,
                 Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.content = new HashMap<>();
        this.quant = Global.quant;
        this.timestamp = System.currentTimeMillis() / 1000L;
        this.content.put("quantity", quant);
        this.content.put("marketSymbol", Global.mOne + "-" + Global.mTwo);
        this.content.put("direction", direction);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.content.put("type", type);
        this.uri = new URL ("https://api.bittrex.com/v3/orders?marketSymbol="
            + Global.mTwo + "-" + Global.mOne +"?direction=" + direction + "?quantity=" +
            quant + "?limit="+ limit + "?timeInForce="+ timeInForce + "?type=" + type);
    }

    public static Buy getInstance(String type, Double limit,
                                  String timeInForce, String direction) throws MalformedURLException {
        return new Buy(type,limit,timeInForce, direction);
    }
}
