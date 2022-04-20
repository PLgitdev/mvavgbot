package com.example.movingaverage.actions;

import com.example.movingaverage.DAO.MongoCRUD;
import com.example.movingaverage.Global;
import com.example.movingaverage.Live.Transaction;
import com.example.movingaverage.Model.Price;
import com.example.movingaverage.session.PriceSession;
import com.example.movingaverage.strategy.MACDSignalLineCrossover;
import com.example.movingaverage.strategy.TradingStrategy;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Routine {
    MongoCRUD dao;
    TradingTree strategy;

    private Routine() {
        this.dao = Global.mongoCRUD;
    }

    public void runRoutine() throws IOException, InterruptedException {

        Price snapShot = PriceSession.currentPriceObject;
        Map<Object, Object> polledData = poll();

        double lastDouble = Double.parseDouble(polledData.get("Last").toString());
        double askDouble = Double.parseDouble(polledData.get("Ask").toString());
        double bidDouble = Double.parseDouble(polledData.get("Bid").toString());
        snapShot.priceListener(lastDouble);

        // Do we need to init every time takes so long maybe check if last is boot
        snapShot.init();
        saveMarketPoll(polledData, dao);
        if (candleCheck(LocalDateTime.now())) {
            candleTick(snapShot, Double.valueOf(lastDouble));
        }
        //function check for what type of indicator is happening
        if (PriceSession.currentPriceObject.validMACDCrossover()) {
            // Possible abstract factory or string based factory
            runMACDSignalLineCrossoverStrategy(lastDouble, askDouble, bidDouble);
        }
        long elapsedTime = (Global.start.minusNanos(LocalDateTime.now().getNano()).getNano()) / 1000000L;
        Thread.sleep(Global.rateLimit);
    }

    private Map<Object, Object> poll() throws IOException {
        return PriceSession.sessionFetcher.marketDataFetch();
    }

    private void saveMarketPoll(Map<Object, Object> polledData, MongoCRUD mongoCRUD) {
        mongoCRUD.createMarketData(polledData, Global.MARKET_SUMMARY);
    }

    private boolean candleCheck(LocalDateTime now) {
        boolean candleCreated = false;
        int difference;
        switch (PriceSession.candleLength) {
            case 0:
                //it takes too long to run the program right now to check every second....
                candleCreated = now.getSecond() == 0;
                //check every second if it has rolled over
                difference = now.getNano() / 1000000;
                Global.rateLimit = 1000 - difference;
                break;
            case 1:
                candleCreated = (now.getMinute() == 5 || now.getMinute() == 0) && now.getSecond() == 0;
                //check every min if it hits a factor of 5
                difference = now.getSecond() * 1000;
                Global.rateLimit = 60000 - difference;
                break;
            case 2:
                candleCreated = now.getHour() == 0 && now.getMinute() == 0 && now.getSecond() == 0;
                //check every min if the hour has rolled over
                difference = now.getSecond() * 1000;
                Global.rateLimit = 60000 - difference;
                break;
            case 3:
                candleCreated = now.getDayOfMonth() > Global.start.getDayOfMonth();
                // check every hour has the day rolled over
                Global.rateLimit = (((1000 * 60) * 60) * 60);
                break;
            default:
        }
        Global.start = now;
        return candleCreated;
    }

    private void candleTick(Price priceObj, Double prices) throws InterruptedException {
        priceObj.setPrices(prices);
        setIndicators(priceObj);
        System.out.println("The time is: " + LocalDateTime.now() + "\n Candle tick: " + priceObj.toString());
    }



    private void setIndicators(Price priceObj) {
        priceObj.setSMA();
        priceObj.setSMACDEMA();
        priceObj.setLMACDEMA();
        priceObj.setMACD();
        priceObj.setSignalLine();
    }

}

