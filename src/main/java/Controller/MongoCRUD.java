package Controller;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Projections.*;
import static com.sun.tools.doclint.Entity.and;
import static org.hibernate.criterion.Restrictions.eq;

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

    public void deleteAllMarketData(String collection, String mOne, String mTwo) {
        Map <String, String> stringMap = null;
        Document doc = new Document();
        stringMap.put(mOne,mTwo);
        stringMap.forEach(doc::append);
        db.getCollection(collection).deleteMany(doc);
    }


    public List<Map<?,?>> retrieveMarketDataByDays(String collection,Long days) {
        List<Map<?,?>> marketDataList = null;
        MongoCollection dbCollection = db.getCollection(collection);
        marketDataList.add((Map<?, ?>) dbCollection.find(gte("startsAt",
            now.minusDays(days))).projection(fields(excludeId(),
            include("close"))));
        return marketDataList;

    }
}
