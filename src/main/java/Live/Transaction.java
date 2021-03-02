package Live;
import com.example.movingaverage.Global;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class Transaction implements Encryption, Communication{
    protected String mOne = Global.mOne;
    protected String mTwo = Global.mTwo;
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected String signatureH;
    protected String subAccountId = "";
    protected Map<Object, Object> content;
    protected String contentH;
    protected LocalDateTime timestamp;
    protected URL uri;

    public final int send() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        HttpURLConnection http = connect();
        setContentHash();
        setSignatureH();
        setHeaders(http);
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        return http.getResponseCode();
    }

    final public String createHash(Object content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA512);
        byte[] messageDigest = md.digest(content.toString().getBytes());
        return zeroPad(convertBytes(messageDigest), 32).toString();
    }

    final public String createSecureHash() throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] secretKey = KEYS.SECRET_API_KEY.getBytes(StandardCharsets.UTF_8);
        Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec kSpec = new SecretKeySpec(secretKey, HMAC_SHA512);
        sha512Hmac.init(kSpec);
        String signature = createSignature();
        byte[] macData = sha512Hmac.doFinal(signature.getBytes(StandardCharsets.UTF_8));
        return zeroPad(convertBytes(macData), 32).toString();
    }

    final public StringBuilder convertBytes(byte[] message) {
        BigInteger signumRep = new BigInteger(1, message);
        return new StringBuilder(signumRep.toString(16));
    }

    final public StringBuilder zeroPad(StringBuilder hash, int totalBits) {
        while (hash.length() < 32) {
            hash.insert(0, "0");
        }
        return hash;
    }

    private String createSignature() {
        return timestamp.toString() + uri.toString() + "POST" + contentH + subAccountId;
    }


    final public HttpURLConnection connect() throws IOException {
        URLConnection con = uri.openConnection();
        return (HttpURLConnection) con;
    }

    final public void setHeaders(HttpURLConnection http) {
        http.setRequestProperty("Api-Key", "API-KEY");
        http.setRequestProperty("Api-Timestamp", timestamp.toString());
        http.setRequestProperty("Api-Content-Hash", contentH);
        http.setRequestProperty("Api-Signature", signatureH);
        http.setRequestProperty("Api-Subaccount-Id", subAccountId);
    }

    private void setContentHash() throws NoSuchAlgorithmException {
        this.contentH = createHash(this.content);
    }

    private void setSignatureH() throws NoSuchAlgorithmException, InvalidKeyException {
        this.signatureH = createSecureHash();
    }
}



