package com.example.movingaverage.Live;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

public interface Communication {
     Object send() throws IOException, InterruptedException;
     URLConnection connect() throws IOException;
     void setHeaders(HttpURLConnection http);
}
