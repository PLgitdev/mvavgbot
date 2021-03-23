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
    ArrayList<Double> pricesShorter = new ArrayList<>(Arrays.asList(.01,.02,.01));
    ArrayList<Double> pricesLonger = new ArrayList<>(Arrays.asList(.01, .02, .04, .06));
    Price priceObj = Price.builder().priceShorter(pricesShorter)
        .priceLonger(pricesLonger)
        .smoothing(2.0)
        .build();
 @Test
    void calculateSMATest() {
     priceObj.setSMA();
     assertEquals(answerOne,priceObj.getAvgShorter());
     assertEquals(answerTwo, priceObj.getAvgLonger());
    }
}