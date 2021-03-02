package Live;

import com.example.movingaverage.Keys;

import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Encryption {
     String HMAC_SHA512 = "HmacSHA512";
     Keys KEYS = new Keys();
     String SHA512 = "SHA512";
     String createHash(Object content) throws NoSuchAlgorithmException;
     String createSecureHash(Object signature) throws NoSuchAlgorithmException, InvalidKeyException;
     String createSignature();
     StringBuilder convertBytes(byte[] message);
     StringBuilder zeroPad(StringBuilder s, int totalBits);

}
