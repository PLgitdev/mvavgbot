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
    final Double emaShorterAnswerOne = 0.01593467923076923;
    final Double emaShorterAnswerTwo = 0.018136128579881658;
    final Double eMALongerAnswerOne = 0.01458583148148148;
    final Double eMALongerAnswerTwo = 0.015745703223593963;
    final Double currentPrice = 0.03024210;
    ArrayList<Double> pricesShorter = new ArrayList<>(Arrays.asList(.01, .02, .01));
    ArrayList<Double> pricesLonger = new ArrayList<>(Arrays.asList(.01, .02, .04, .06));
    List<Double> ninedaysofclose = new ArrayList<>(Arrays.asList(.01, .03));
    List<Double> shortMACDPeriod = new ArrayList<>(Arrays.asList(.01, .02, .01));
    List<Double> longMACDPeriod = new ArrayList<>(Arrays.asList(.01, .02, .04, .06));
    List<Double> signaLine = new ArrayList<>(0);
    List<Double> twelveDayRibbons = new ArrayList<>(0);
    List<Double> twentyDayRibbons = new ArrayList<>(0);
    Price priceObj = Price.builder().priceShorter(pricesShorter)
        .priceLonger(pricesLonger)
        .shortMACDPeriod(shortMACDPeriod)
        .longerMACDPeriod(longMACDPeriod)
        .currentPrice(currentPrice)
        .twelveDayRibbons(twelveDayRibbons)
        .twentySixDayRibbons(twentyDayRibbons)
        .nineDaysOfClose(ninedaysofclose)
        .signalLine(signaLine)
        .smoothing(2.0)
        .build();

    @Test
    void initTest() {
        // First iteration
        assertEquals(0, priceObj.getTwelveDayRibbons().size());
        assertEquals(0, priceObj.getTwentySixDayRibbons().size());
        priceObj.init();
        // Did it add to ribbons
        assertEquals(1, priceObj.getTwelveDayRibbons().size());
        // Did it add to ribbons
        assertEquals(1, priceObj.getTwentySixDayRibbons().size());
        // Is it the correct value
        assertEquals(emaShorterAnswerOne,priceObj.getTwelveDayRibbons().get(0));
        // Is it the correct value
        assertEquals(eMALongerAnswerOne,priceObj.getTwentySixDayRibbons().get(0));
    }

    @Test
    void calculateSMATest() {
        priceObj.setSMA();
        assertEquals(sMAAnswerOne, priceObj.getAvgShorter());
        assertEquals(sMAAnswerTwo, priceObj.getAvgLonger());
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
        priceObj.init();
        //second iteration
        priceObj.setCurrentPrice(priceObj.getCurrentPrice() +.000002);
        priceObj.setLMACDEMA();
        assertEquals(2, priceObj.getTwentySixDayRibbons().size());
        assertEquals(eMALongerAnswerTwo, priceObj.getTwentySixDayRibbons().get(1));
    }
    @Test
    void setSMACDEMATest() {
        priceObj.init();
        //second iteration
        priceObj.setCurrentPrice(priceObj.getCurrentPrice() +.000002);
        priceObj.setSMACDEMA();
        assertEquals(2, priceObj.getTwelveDayRibbons().size());
        assertEquals(emaShorterAnswerTwo, priceObj.getTwelveDayRibbons().get(1));
    }
}
