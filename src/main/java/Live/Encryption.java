package Live;

import java.security.NoSuchAlgorithmException;

public interface Encryption {
     String HMAC_SHA512 = "HmacSHA512";
     String SHA512 = "SHA512";
     String create512Hash(Object content) throws NoSuchAlgorithmException;
}
