package com.varun.swim;

import com.varun.swim.message.ServerMessage;
import com.varun.swim.message.ServerMessageFactory;
import com.varun.swim.task.BroadcastTask;
import com.varun.swim.task.PingTask;
import com.varun.swim.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {

    private static final int PING_INTERVAL = 5;
    private static final int BROADCAST_INTERVAL = 7;


    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Server started on port: %d\n", port);
            FileUtil.persistServerConfig(port);
            ServerSyncState serverSyncState = new ServerSyncState();

            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

            PingTask pingTask = new PingTask(serverSyncState, port);
            scheduledExecutorService.scheduleAtFixedRate(pingTask, PING_INTERVAL, PING_INTERVAL, TimeUnit.SECONDS);

            BroadcastTask broadcastTask = new BroadcastTask(serverSyncState, port);
            scheduledExecutorService.scheduleAtFixedRate(broadcastTask, BROADCAST_INTERVAL, BROADCAST_INTERVAL, TimeUnit.SECONDS);

            while (true) {
                Socket client = serverSocket.accept();
                handleClient(client, port, serverSyncState);
            }
        }
    }

    private static void handleClient(Socket client, int port, ServerSyncState serverSyncState) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String input = reader.readLine().strip();
        if (!input.isEmpty()) {
            ServerMessage message = ServerMessageFactory.parseServerMessage(input, port, serverSyncState);
            message.interpret();
        }
        reader.close();
    }
}
