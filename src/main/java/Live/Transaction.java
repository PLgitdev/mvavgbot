package Live;
import com.example.movingaverage.Global;

import java.io.IOException;

public abstract class Transaction {
    protected String mOne = Global.mOne;
    protected String mTwo = Global.mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;

    public abstract int fillOrKill() throws IOException;

}



