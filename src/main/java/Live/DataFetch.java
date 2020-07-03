package Live;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class DataFetch {

    private ObjectMapper objectMapper;
    private String mOne;
    private String mTwo;
    private static  DataFetch soleInstanceDataFetch;

    private DataFetch(String mOne, String mTwo) {
        this.objectMapper = new ObjectMapper();
        this.mOne = mOne;
        this.mTwo = mTwo;
    }

    public static DataFetch getInstance(String mOne, String mTwo) {
        if (soleInstanceDataFetch == null) {
            soleInstanceDataFetch = new DataFetch(mOne,mTwo);
        }
        return soleInstanceDataFetch;
    }

    public Map<?, ?> marketDataFetcher() throws IOException, InterruptedException {
        URL url =
            new URL("https://api.bittrex.com/api/v1.1/public/getmarketsummary?market=" + mOne + "-" + mTwo);
        StringBuilder content = fetch(url);
        return objectMapper.readValue(content.toString(), new TypeReference<Map<?,?>>(){});
    }

    public ArrayList<Map<?,?>> historicalDataFetcher() throws IOException, InterruptedException {
        String dayOne = "DAY_1";
        URL url =
            new URL( "https://api.bittrex.com/v3/markets/" + mOne + "-" + mTwo + "/candles/" + dayOne +
                "/recent");
        StringBuilder historicalData = fetch(url);
        return objectMapper.readValue(historicalData.toString(), new TypeReference<ArrayList<Map<?,?>>>(){});
    }

    public StringBuilder fetch(URL url) throws InterruptedException {
        StringBuilder data = new StringBuilder();
        try (InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
             String inputLine = br.readLine();
             data.append(inputLine);
             } catch (IOException ex) {
                ex.printStackTrace();
             }
        Thread.sleep(5000);
        return data;
    }
    public boolean valid() {
        String marketV = "^(\\w|\\D|\\S){3}$";
        if (!mOne.matches(marketV)) return false;
        return mTwo.matches(marketV);
    }
}
