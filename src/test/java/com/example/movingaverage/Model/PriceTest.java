package com.example.movingaverage.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceTest {
    final Double answerOne = 0.01333333;
    final Double answerTwo = 0.0325;
    final Double answerThree = 0.01593467923076923;
    final Double currentPrice = 0.03024210;
    ArrayList<Double> pricesShorter = new ArrayList<>(Arrays.asList(.01,.02,.01));
    ArrayList<Double> pricesLonger = new ArrayList<>(Arrays.asList(.01, .02, .04, .06));
    List<Double> shortMACDPeriod = new ArrayList<>(Arrays.asList(.01,.02,.01));
    List<Double> twelveDayRibbons = new ArrayList<>(0);
    Price priceObj = Price.builder().priceShorter(pricesShorter)
        .priceLonger(pricesLonger)
        .shortMACDPeriod(shortMACDPeriod)
        .currentPrice(currentPrice)
        .twelveDayRibbons(twelveDayRibbons)
        .smoothing(2.0)
        .build();
    @Test
    void calculateSMATest() {
        priceObj.setSMA();
        assertEquals(answerOne, priceObj.getAvgShorter());
        assertEquals(answerTwo, priceObj.getAvgLonger());
    }
    @Test
    void calculateEMATest() {
        // The period is 12
        // This is the first round of incoming data
        priceObj.setSMACDEMA();
        // This simulates the second round of incoming data
        priceObj.setSMACDEMA();
        assertEquals(answerThree, priceObj.getTwelveDayRibbons().get(1));
    }
}
