package com.example.movingaverage.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceTest {
    final Double sMAAnswerOne = 0.01333333;
    final Double sMAAnswerTwo = 0.0325;
    final Double eMAAnswerThree = 0.01574923550295858;
    final Double eMAAnswerFour = 0.011499414814814814;
    final Double eMAAnswerFive = 0.011499562962962962;
    final Double currentPrice = 0.03024210;
    ArrayList<Double> pricesShorter = new ArrayList<>(Arrays.asList(.01, .02, .01));
    ArrayList<Double> pricesLonger = new ArrayList<>(Arrays.asList(.01, .02, .04, .06));
    List<Double> shortMACDPeriod = new ArrayList<>(Arrays.asList(.01, .02, .01));
    List<Double> longMACDPeriod = new ArrayList<>(Arrays.asList(.01, .02, .04, .06));
    List<Double> twelveDayRibbons = new ArrayList<>(0);
    List<Double> twentyDayRibbons = new ArrayList<>(0);
    Price priceObj = Price.builder().priceShorter(pricesShorter)
        .priceLonger(pricesLonger)
        .shortMACDPeriod(shortMACDPeriod)
        .longerMACDPeriod(longMACDPeriod)
        .currentPrice(currentPrice)
        .twelveDayRibbons(twelveDayRibbons)
        .twentySixDayRibbons(twentyDayRibbons)
        .smoothing(2.0)
        .build();
    @Test
    void calculateSMATest() {
        priceObj.setSMA();
        assertEquals(sMAAnswerOne, priceObj.getAvgShorter());
        assertEquals(sMAAnswerTwo, priceObj.getAvgLonger());
    }
    @Test
    void calculateEMATest() {
        // The period is 12
        // This is the first round of incoming data
        priceObj.setSMACDEMA();
        // This simulates the second round of incoming data
        priceObj.setSMACDEMA();
        assertEquals(eMAAnswerThree, priceObj.getTwelveDayRibbons().get(1));
    }
    @Test
    void calculateDateLimitCheckTest() {
        priceObj.setDateLimit(LocalDateTime.now().minusDays(1));
        priceObj.dateLimitCheck(1);
        assertEquals(2, priceObj.getPriceShorter().size());

    }

    @Test
    void calculateDateLimitCheckLongerTest() {
        priceObj.setDateLimit(LocalDateTime.now().minusDays(1));
        priceObj.dateLimitCheckLonger(1);
        assertEquals(3, priceObj.getPriceLonger().size());
    }

    @Test
    void setLMACDEMATest() {
        //first iteration
        assertEquals(0, priceObj.getTwentySixDayRibbons().size());
        priceObj.setLMACDEMA();
        //did it add to ribbons
        assertEquals(1, priceObj.getTwentySixDayRibbons().size());
        //is it the correct value
        assertEquals(eMAAnswerFour,priceObj.getTwentySixDayRibbons().get(0));
        //second iteration
        priceObj.setCurrentPrice(priceObj.getCurrentPrice() +.000002);
        priceObj.setLMACDEMA();
        assertEquals(2, priceObj.getTwentySixDayRibbons().size());
        assertEquals(eMAAnswerFive, priceObj.getTwentySixDayRibbons().get(1));
    }
}
