package com.example.movingaverage.Live;

import com.example.movingaverage.Keys;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Encryption {
     String HMAC_SHA512 = "HmacSHA512";
     String SHA512 = "SHA-512";
     String createHash(Object content) throws NoSuchAlgorithmException;
     String createSecureHash() throws NoSuchAlgorithmException, InvalidKeyException;
     String convertBytes(byte[] message);
     String byteToHex(byte num);

}
