package com.example.movingaverage.Live;

import java.io.IOException;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;

public interface Communication {
     Object send() throws IOException, InterruptedException;
     HttpHeaders setHeaders();
}
