package Live;

import java.security.NoSuchAlgorithmException;

public interface Encryption {
     String HMAC_SHA512 = "HmacSHA512";
     String SHA512 = "SHA512";
     String createHash(Object content)  throws NoSuchAlgorithmException;
     String createSecureHash(Object content);
     StringBuilder zeroPad(StringBuilder s, int totalBits);

}
