package Live;

import lombok.Builder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

@Builder
public abstract class Transaction {
    protected Double quant;
    protected String mOne;
    protected String mTwo;
    protected String direction;
    protected Double type;
    protected Double limit;
    protected String timeInForce;
    protected String clientOrderId;
    protected Boolean useAwards;

    abstract void fillOrKill() throws IOException;
}
