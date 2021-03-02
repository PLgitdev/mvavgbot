package Live;
import com.example.movingaverage.Global;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class Transaction implements Encryption {
    protected String mOne = Global.mOne;
    protected String mTwo = Global.mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected Map<Object,Object> content;
    protected String contentH;
    protected LocalDateTime timestamp;
    protected URL uri;

    public abstract int send() throws IOException, NoSuchAlgorithmException;

    public final void setHeaders(HttpURLConnection http) {
        http.setRequestProperty("Api-Key","API-KEY");
        http.setRequestProperty("Api-Timestamp", timestamp.toString());
        http.setRequestProperty("Api-Content-Hash",contentH);
        http.setRequestProperty("Api-Signature","API-SIGNATURE");
    }

    public final void setContentHash() throws NoSuchAlgorithmException {
        this.contentH = create512Hash(this.content);
    }
    public final String create512Hash(Object content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA512);
        byte[] messageDigest = md.digest(content.toString().getBytes());
        BigInteger signumRep = new BigInteger(1, messageDigest);
        StringBuilder hash = new StringBuilder(signumRep.toString(16));
        while(hash.length() < 32)  {
            hash.insert(0, "0");
        }
        return hash.toString();
    }
}



