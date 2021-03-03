package com.example.movingaverage.Live;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface Communication {
     int send() throws IOException;
     HttpURLConnection connect() throws IOException;
     void setHeaders(HttpURLConnection http);
}
