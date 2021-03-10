package com.example.movingaverage.Live;

import com.example.movingaverage.Global;
import com.example.movingaverage.Keys;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Transaction implements Encryption, Communication{
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected String signatureH;
    protected String subAccountId = "";
    protected Map<String,String> content;
    protected String contentH;
    protected URL sendUri;

    public final Object send() throws IOException {
        JsonHttpContent jsonHttpContent = new JsonHttpContent( new JacksonFactory(), content);
        HttpRequest request = Global.requestFactory.buildPostRequest(new GenericUrl(sendUri.toString()), jsonHttpContent);
        HttpHeaders headers = new HttpHeaders();
        try {
            setContentHash(mapToString(content));
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("invalid algorithm " + e );
        }
        setHeaders(headers);
        String signature = createSignature(headers);
        try {
            setSignatureH(signature);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("invalid algorithm or key " + e );
        }
        headers.set("Api-Signature", signatureH);
        request.setHeaders(headers);
        return request.execute();
    }
    final public void setHeaders(HttpHeaders headers) {
        headers.set("Api-Key", Keys.API_KEY);
        headers.set("Api-Timestamp", String.valueOf(Instant.now().getEpochSecond()));
        headers.set("Api-Content-Hash", contentH);
    }

    final public String createHash(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(SHA512);
        byte[] digest = md.digest(content.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    final public String createSecureHash(String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] secretKey = Keys.SECRET_API_KEY.getBytes();
        Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec kSpec = new SecretKeySpec(secretKey, HMAC_SHA512);
        sha512Hmac.init(kSpec);
        byte[] macData = sha512Hmac.doFinal(signature.getBytes(StandardCharsets.UTF_8));
        return BaseEncoding.base16().lowerCase().encode(macData);
    }

    private String createSignature(HttpHeaders headers) {
        return headers.get("Api-TimeStamp").toString() + sendUri.toString() + "POST" + headers.get("Api-Content-Hash") + subAccountId;
    }
    private void setContentHash(String content) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.contentH = createHash(content);
    }

    private void setSignatureH(String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        this.signatureH = createSecureHash(signature);
    }
    private String mapToString(Map<String, String> map) {
        return map.keySet().stream()
            .map(key -> "\"" + key + "\"" + ":" + "\"" + map.get(key) + "\"")
            .collect(Collectors.joining(",", "{", "}"));
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



