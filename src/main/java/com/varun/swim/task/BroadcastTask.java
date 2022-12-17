package com.varun.swim.task;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;
import com.varun.swim.util.FileUtil;

import java.io.IOException;
import java.util.Set;

public class BroadcastTask implements Runnable {

    private final ServerSyncState serverSyncState;
    private final int port;

    public BroadcastTask(ServerSyncState serverSyncState, int port) {
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
        String broadCastMessage = serverSyncState.toString();
        for (Integer peerPort : peerPorts) {
            if (serverSyncState.isNodeMarkedAsFailed(peerPort)) {
                continue;
            }
            CustomClient client;
            try {
                client = new CustomClient(peerPort);
                client.sendMessage(String.format("%s %d", broadCastMessage, port));
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
