package com.example.movingaverage.com;

import com.example.movingaverage.Live.Transaction;
import com.example.movingaverage.session.PriceSession;
import com.example.movingaverage.strategy.MACDSignalLineCrossover;

import java.io.IOException;
import java.net.http.HttpResponse;

public class TradingTree {
    private void runMACDSignalLineCrossoverStrategy(
            Double lastDouble, Double askDouble, Double bidDouble) throws IOException, InterruptedException {

        this.strategy =
                MACDSignalLineCrossover
                        .createMACDSignalLineCrossoverStrategy(
                                PriceSession.currentPriceObject, lastDouble, askDouble, bidDouble
                        );

        this.strategy.setBuyBidMode();

        System.out.println("Waiting for a buy");
        //could be a decision / binary tree
        while (!this.strategy
                .buyResponseHandling(sendOrder(createOrder(
                        this.strategy.setBuyBidMode().doubleValue(), "buy"
                ))) ?
                this.strategy.sellHodlSet()
                :
                this.strategy.buyResponseHandling(sendOrder(createOrder(
                        this.strategy.buyGate().doubleValue(), "buy"
                ))) // || buyTimeout()
        ) {
            System.out.print(".");
        }
        PriceSession.successfulBuy = true;
    }
    //This goes in the binary tree
    private HttpResponse<String> sendOrder(Transaction order) throws IOException, InterruptedException {

        HttpResponse<String> response = order.send();

        if (response.statusCode() == 201) {
            System.out.println("Successful order");
        }
        if (response.statusCode() == 401) {
            System.out.println("Unauthorized 401 body is" + response.body());
        } else {
            System.out.println("Response not 201: " + response);
        }

        Thread.sleep(1000);

        return response;
    }
}
