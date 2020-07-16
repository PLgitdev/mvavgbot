package Controller;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Projections.include;

public class MongoCRUD {
    private  MongoDatabase db;
    private  LocalDateTime now;

    private static MongoCRUD soleInstanceMongoCRUD;

    private MongoCRUD(){
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

    public void createMarketData(Map<?, ?> data, String collection) {
       Document doc = new Document();
       data.forEach( (key, value) -> doc.append(String.valueOf(key), value));
       db.getCollection(collection).insertOne(doc);
    }

    public void deleteAllMarketData(String collection) {
        db.getCollection(collection).drop();
    }


    public ArrayList<Map<?, ?>> retrieveMarketDataByDays(String collection, Long days, String q, String p) {
        ArrayList<Map<?, ?>> marketDataCollection = new ArrayList<Map<?, ?>>(){};
        MongoCollection<Document> dbCollection = db.getCollection(collection);
        dbCollection .find(gte(q, now.minusDays(days).toString()))
            .projection(include(p))
            .forEach((Consumer<? super Document>) marketDataCollection::add);
        return marketDataCollection;

    }
}
