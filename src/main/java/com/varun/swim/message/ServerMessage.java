package com.varun.swim.message;

import com.varun.swim.logging.CustomLogger;

import java.io.IOException;

public interface ServerMessage {

    CustomLogger logger = new CustomLogger();

    void interpret() throws IOException;
}
