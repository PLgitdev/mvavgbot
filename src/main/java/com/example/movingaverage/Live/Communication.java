package com.example.movingaverage.Live;

import java.io.IOException;


public interface Communication {
     Object send() throws IOException, InterruptedException;
}
