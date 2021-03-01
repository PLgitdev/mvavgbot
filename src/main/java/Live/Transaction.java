package Live;
import com.example.movingaverage.Global;
import jdk.jfr.Timestamp;

import javax.json.Json;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public abstract class Transaction {
    protected String mOne = Global.mOne;
    protected String mTwo = Global.mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected Map<Object,Object> content;
    protected LocalDateTime timestamp;

    public abstract int fillOrKill() throws IOException, NoSuchAlgorithmException;

    public abstract void setHeaders(HttpURLConnection http, String hash);

    public abstract String contentHash() throws NoSuchAlgorithmException;
}



