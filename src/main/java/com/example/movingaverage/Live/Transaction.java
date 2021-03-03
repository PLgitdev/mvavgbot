package com.example.movingaverage.Live;

import com.example.movingaverage.Keys;
import com.mongodb.util.JSON;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public abstract class Transaction implements Encryption, Communication{
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected String signatureH;
    protected String subAccountId = "";
    protected HashMap<Object, Object> content;
    protected String contentH;
    protected Long timestamp;
    protected URL uri;

    public final int send() throws IOException, InterruptedException {
        HttpURLConnection http = connect();
        String jsonBodyString = JSON.serialize(content);
        try {
            setContentHash(jsonBodyString);
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("invalid algorithm " + e );
        }
        try {
            setSignatureH();
        }
        catch (InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("invalid algorithm or key " + e );
        }
        setHeaders(http);
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setFixedLengthStreamingMode(jsonBodyString.getBytes().length);
        http.getOutputStream().write(jsonBodyString.getBytes());
        Map<?,?> s = http.getHeaderFields();
        Thread.sleep(10000);
        return http.getResponseCode();
    }

    final public String createHash(Object content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA512);
        byte[] messageDigest = md.digest(content.toString().getBytes());
        return convertBytes(messageDigest);
    }

    final public String createSecureHash() throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] secretKey = Keys.SECRET_API_KEY.getBytes();
        Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec kSpec = new SecretKeySpec(secretKey, HMAC_SHA512);
        sha512Hmac.init(kSpec);
        String signature = createSignature();
        byte[] macData = sha512Hmac.doFinal(signature.getBytes());
        return convertBytes(macData);
    }

    final public String convertBytes(byte[] message) {
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : message) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }
    final public String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }
    final public HttpURLConnection connect() throws IOException {
        URLConnection con = uri.openConnection();
        return (HttpURLConnection) con;
    }

    final public void setHeaders(HttpURLConnection http) {
        http.setRequestProperty("Api-Key", Keys.API_KEY);
        http.setRequestProperty("Api-Timestamp", timestamp.toString());
        http.setRequestProperty("Api-Content-Hash", contentH);
        http.setRequestProperty("Api-Signature", signatureH);
        http.setRequestProperty("Api-Subaccount-Id", subAccountId);
        http.setRequestProperty("Content-Type", "application/json");
    }
    private String createSignature() {
        return timestamp.toString() + uri.toString() + "POST" + contentH + subAccountId;
    }
    private void setContentHash(String content) throws NoSuchAlgorithmException {
        this.contentH = createHash(content);
    }

    private void setSignatureH() throws NoSuchAlgorithmException, InvalidKeyException {
        this.signatureH = createSecureHash();
    }
    /*final void setContent() {
        this.content.put("marketSymbol", Global.mOne + "-" + Global.mTwo);
        this.content.put("direction", direction);
        this.content.put("limit", limit);
        this.content.put("timeInForce", timeInForce);
        this.content.put("type", type);
    }

     */
}



