package com.varun.swim.message;

import java.io.IOException;

public interface ServerMessage {

    void interpret() throws IOException;
}
