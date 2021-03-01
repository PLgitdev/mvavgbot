package Live;
import com.example.movingaverage.Global;
import jdk.jfr.Timestamp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.Date;

public abstract class Transaction {
    protected String mOne = Global.mOne;
    protected String mTwo = Global.mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected LocalDateTime timestamp;

    public abstract int fillOrKill() throws IOException;

    public abstract void setHeaders(HttpURLConnection http);
}



