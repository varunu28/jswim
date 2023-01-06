package com.varun.swim;

import com.varun.swim.logging.CustomLogger;
import com.varun.swim.message.ServerMessage;
import com.varun.swim.message.ServerMessageFactory;
import com.varun.swim.task.FailureDetectionTask;
import com.varun.swim.task.PingRequestTask;
import com.varun.swim.task.PingTask;
import com.varun.swim.task.SuspectDetectionTask;
import com.varun.swim.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.varun.swim.util.Constants.*;

@SuppressWarnings("InfiniteLoopStatement")
public class Server {

    private static final CustomLogger CUSTOM_LOGGER = new CustomLogger();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            CUSTOM_LOGGER.logInfo(String.format("Server started on port: %d", port));
            FileUtil.persistServerConfig(port);
            ServerSyncState serverSyncState = new ServerSyncState();

            // Background tasks
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);

            PingTask pingTask = new PingTask(serverSyncState, port);
            scheduledExecutorService.scheduleAtFixedRate(pingTask, PING_INTERVAL, PING_INTERVAL, TimeUnit.SECONDS);

            PingRequestTask pingRequestTask = new PingRequestTask(serverSyncState, port);
            scheduledExecutorService.scheduleAtFixedRate(
                    pingRequestTask, INDIRECT_PING_THRESHOLD, INDIRECT_PING_THRESHOLD, TimeUnit.SECONDS);

            SuspectDetectionTask suspectDetectionTask = new SuspectDetectionTask(serverSyncState, port);
            scheduledExecutorService.scheduleAtFixedRate(
                    suspectDetectionTask, SUSPECT_THRESHOLD, SUSPECT_THRESHOLD, TimeUnit.SECONDS);

            FailureDetectionTask failureDetectionTask = new FailureDetectionTask(serverSyncState, port);
            scheduledExecutorService.scheduleAtFixedRate(
                    failureDetectionTask, FAILURE_THRESHOLD, FAILURE_THRESHOLD, TimeUnit.SECONDS);

            // Handling incoming messages
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
            Objects.requireNonNull(message).interpret();
        }
        reader.close();
    }
}
