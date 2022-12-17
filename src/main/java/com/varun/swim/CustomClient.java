package com.varun.swim;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CustomClient {
    private static final String LOCALHOST = "127.0.0.1";
    private final Socket socket;
    private final PrintWriter writer;

    public CustomClient(int port) throws IOException {
        this.socket = new Socket(LOCALHOST, port);
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message) throws IOException {
        this.writer.println(message);
    }

    public void close() throws IOException {
        this.writer.close();
        this.socket.close();
    }
}
