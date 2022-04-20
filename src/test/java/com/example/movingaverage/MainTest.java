package com.example.movingaverage;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    Deque<String> commands = new ArrayDeque<>(10);

    @Test
    void bootTest() {
        //Add The commands to the deque
        commands.add("boot");

    }

}