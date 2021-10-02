package com.example.movingaverage.DAO;

import com.mongodb.Block;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Projections.include;

public class MongoCRUD {
    private final  MongoDatabase db;
    private final  LocalDateTime now;

    private static MongoCRUD soleInstanceMongoCRUD;

    private MongoCRUD() {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        this.db = mongoClient.getDatabase("mvavgbotdb");
        this.now = LocalDateTime.now();
    }

    public static MongoCRUD getInstance(){
        if (soleInstanceMongoCRUD == null) {
            soleInstanceMongoCRUD = new MongoCRUD();
        }
        return soleInstanceMongoCRUD;
    }

    public void createMarketData(Map<Object,Object> data, String collection) {
       Document doc = new Document();
       data.forEach( (key, value) -> doc.append(String.valueOf(key), value));
       db.getCollection(collection).insertOne(doc);
    }

    public void deleteAllMarketData(String collection) {
        db.getCollection(collection).drop();
    }


    public List<Double> retrieveMarketDataByDays(String collection, int days, String q, String p) {
        List<Double> marketDataCollection = new ArrayList<>(){};
        MongoCollection<Document> dbCollection = db.getCollection(collection);
        FindIterable<Document> iterableDocument= dbCollection.find(gte(q, now.minusDays(days).toString()))
            .projection(include(p));
        iterableDocument.forEach((Block<? super Document>) (value) -> marketDataCollection.add(
                Double.valueOf(value.get(p).toString())));
        return marketDataCollection;

    }
}
