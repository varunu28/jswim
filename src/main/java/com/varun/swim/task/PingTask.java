package com.varun.swim.task;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;
import com.varun.swim.util.FileUtil;

import java.io.IOException;
import java.util.Set;

import static com.varun.swim.util.Constants.PING_REQUEST;

public class PingTask implements Runnable {

    private final ServerSyncState serverSyncState;
    private final int port;

    public PingTask(ServerSyncState serverSyncState, int port) {
        this.serverSyncState = serverSyncState;
        this.port = port;
    }

    @Override
    public void run() {
        Set<Integer> peerPorts;
        try {
            peerPorts = FileUtil.readPeerServerConfig(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Integer peerPort : peerPorts) {
            if (serverSyncState.isNodeMarkedAsFailed(peerPort)) {
                continue;
            }
            CustomClient client;
            try {
                client = new CustomClient(peerPort);
                client.sendMessage(String.format("%s %d", PING_REQUEST, port));
                client.close();
            } catch (IOException e) {
                if (e.getMessage().contains("Connection refused")) {
                    System.out.printf("Marking node on port: %d as failed\n", peerPort);
                    this.serverSyncState.markNodeAsFailed(peerPort);
                    continue;
                }
                throw new RuntimeException(e);
            }
        }
    }
}
