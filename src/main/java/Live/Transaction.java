package Live;
import java.io.IOException;

public abstract class Transaction {
    protected String mOne;
    protected String mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;

    public abstract int fillOrKill() throws IOException;

}



