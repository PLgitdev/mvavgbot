package com.example.movingaverage.Live;

import com.example.movingaverage.Global;
import com.example.movingaverage.Keys;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mongodb.util.JSON;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

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
    protected URL sendUri;

    public final Object send() throws IOException {
        String flatContent = mapToString(content);
        String jsonSearlize = JSON.serialize(content);
        try {
            setContentHash(jsonSearlize);
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("invalid algorithm " + e );
        }
        HttpContent httpContent = new JsonHttpContent(new JacksonFactory(),content);
        HttpHeaders headers = setHeaders();
        String signature = createSignature(headers);
        try {
            setSignatureH(signature);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("invalid algorithm or key " + e );
        }
        headers.set("Api-Signature", signatureH);
        HttpRequest request = Global.requestFactory.buildPostRequest(new GenericUrl(uri), httpContent).setHeaders(headers);
        return request.execute().getStatusCode();
    }

    final public String createHash(Object content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA512);
        byte[] messageDigest = md.digest(content.toString().getBytes());
        return hexEncode(Arrays.toString(messageDigest));
    }

    final public String createSecureHash(String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] secretKey = Keys.SECRET_API_KEY.getBytes();
        Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec kSpec = new SecretKeySpec(secretKey, HMAC_SHA512);
        sha512Hmac.init(kSpec);
        byte[] macData = sha512Hmac.doFinal(signature.getBytes());
        return hexEncode(Arrays.toString(macData));
    }

    final public String hexEncode(String s) {
        return String.format("%040x", new BigInteger(1, s.getBytes()));
    }


    final public HttpHeaders setHeaders() {
        HttpHeaders headers  = new HttpHeaders();
        headers.set("Api-Key", Keys.API_KEY);
        headers.set("Api-Timestamp", timestamp);
        headers.set("Api-Content-Hash", contentH);
        headers.set("Api-Subaccount-Id", subAccountId);
        return headers;
    }
    private String createSignature(HttpHeaders headers) {
        return headers.get("Api-TimeStamp") + uri.toString() + "POST" + headers.get("Api-Content-Hash") + subAccountId;
    }
    private void setContentHash(String content) throws NoSuchAlgorithmException {
        this.contentH = createHash(content);
    }

    private void setSignatureH(String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        this.signatureH = createSecureHash(signature);
    }
    private String mapToString(Map<Object,Object> map) {
        return map.keySet().stream()
            .map(key -> "\"" + key + "\"" + ":" + map.get(key))
            .collect(Collectors.joining(", ", "{", "}"));
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



