package com.example.movingaverage.Live;

import com.example.movingaverage.Keys;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/*  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 - This is the Transaction abstract class which handles HTTP requests to the Bittrex server that need the be
 - authenticated specifically in the case of a transaction.
 -
 - Transaction has two children Buy and Sell.
 -
 - A transaction requires a SHA512 encryption as well as a HMAC SHA 512 Keyed encryption authentication for the v3 api.

 - The headers require the following values
 -     Api-key
 -     Api-Timestamp - in unix time of the current time
 -     Api-Content   - Hash a SHA512 hex encoded value created from the content of the request
 -     Content type  - Must be application/json
 -     Api-Signature - This is a HMAC SHA512 hex encoded value created from the concatenation of time, uri,
 -                     type of request ie. POST, and the content hash
 -
 - The content hash must be calculated before the signature and a builder can be used to achieve this.
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
public abstract class Transaction implements Encryption, Communication {
    protected String type;
    protected Double limit;
    protected String timeInForce;
    protected String direction;
    protected String timestamp;
    protected String signatureH;
    protected String subAccountId = "";
    protected Map<String,String> content;
    protected String contentH;
    protected URL sendUri;

    public final HttpResponse<String> send() throws IOException, InterruptedException {
        Gson gSon = new Gson();
        String requestBody = gSon.toJson(content);
        HttpClient client = HttpClient.newHttpClient();
        try {
            setContentHash(requestBody);
        }
        catch (NoSuchAlgorithmException e){
            System.out.println("invalid algorithm " + e );
        }
        HttpRequest.Builder request = HttpRequest.newBuilder()
            .uri(URI.create(sendUri.toString()))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .header("Api-Key", Keys.API_KEY)
            .header("Api-Timestamp", String.valueOf(System.currentTimeMillis()))
            .header("Api-Content-Hash", contentH)
            .header("Content-Type", "application/json");
        String signature = createSignature(request);
        try {
            setSignatureH(signature);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("invalid algorithm or key " + e );
        }
        HttpRequest requestBuilt = request.header("Api-Signature", signatureH).build();
        HttpResponse<String> response = client.send(requestBuilt,
            HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        System.out.println(response.statusCode());
        return response;
    }

    final public String createHash(String content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(SHA512);
        byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
        return BaseEncoding.base16().lowerCase().encode(digest);
    }

    final public String createSecureHash(String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        final byte[] secretKey = Keys.SECRET_API_KEY.getBytes();
        Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
        SecretKeySpec kSpec = new SecretKeySpec(secretKey, HMAC_SHA512);
        sha512Hmac.init(kSpec);
        byte[] macData = sha512Hmac.doFinal(signature.getBytes(StandardCharsets.UTF_8));
        return BaseEncoding.base16().lowerCase().encode(macData);
    }

    private String createSignature(HttpRequest.Builder request) {
        HttpRequest temp  = request.build();
        String timeContent = temp.headers().allValues("Api-Timestamp").get(0);
        return timeContent + sendUri.toString() + "POST" + contentH + subAccountId;
    }
    private void setContentHash(String content) throws NoSuchAlgorithmException {
        this.contentH = createHash(content);
    }

    private void setSignatureH(String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        this.signatureH = createSecureHash(signature);
    }
}



