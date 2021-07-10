package com.example.movingaverage.strategy;

import com.example.movingaverage.Model.Price;

import java.math.BigDecimal;
import java.util.Map;

public class TradingStrategy {
    public Price priceObj;
    protected BigDecimal profit;
    protected BigDecimal buy = new BigDecimal(0);
    protected BigDecimal sell; // If it says not initialized try setting to zero
    protected double profitPercentageTotals = 0.0;
    protected Map<Object, Object> liveMarketData;
}
