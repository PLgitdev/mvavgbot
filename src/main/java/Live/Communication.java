package Live;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Communication {
     int send() throws IOException, NoSuchAlgorithmException, InvalidKeyException;
     HttpURLConnection connect() throws IOException;
     void setHeaders(HttpURLConnection http);
}
