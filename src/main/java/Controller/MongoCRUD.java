package Controller;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.Map;

public class MongoCRUD {
    private  MongoDatabase db;

    private static MongoCRUD soleInstanceMongoCRUD;

    private MongoCRUD(){
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        this.db = mongoClient.getDatabase("mvavgbotdb");
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

    /*
    public void deleteAllMarketData(String collection) {
        db.getCollection(collection).deleteMany({});

    }
    */
}
