package Live;
import java.io.IOException;

public abstract class Transaction {
    protected Double quant;
    protected String mOne;
    protected String mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;

    abstract int fillOrKill() throws IOException;

}



