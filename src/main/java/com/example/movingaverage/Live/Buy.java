package com.example.movingaverage.Live;

import com.example.movingaverage.Global;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

public class Buy extends Transaction {
    private Double quant;
    private Buy(String type,
                 Double limit, String timeInForce, String direction) throws MalformedURLException {
        super();
        this.content = new LinkedHashMap<>();
        this.quant = Global.quant;
        this.content.put("marketSymbol", Global.mTwo + "-" + Global.mOne);
        this.content.put("direction", direction);
        this.content.put("type", type);
        this.content.put("quantity", "0.00000000");
        this.content.put("limit", limit.toString());
        this.content.put("timeInForce", timeInForce);
        this.sendUri = new URL("https://api.bittrex.com/v3/orders");
    }

    public static Buy getInstance(String type, Double limit,
                                  String timeInForce, String direction) throws MalformedURLException {
        return new Buy(type,limit,timeInForce, direction);
    }
}
