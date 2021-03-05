package com.example.movingaverage.Live;

import com.example.movingaverage.Keys;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Encryption {
     String HMAC_SHA512 = "HmacSHA512";
     char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
     String SHA512 = "SHA-512";
     String createHash(Object content) throws NoSuchAlgorithmException;
     String createSecureHash() throws NoSuchAlgorithmException, InvalidKeyException;
     String byteToHex(byte[] num);

}
