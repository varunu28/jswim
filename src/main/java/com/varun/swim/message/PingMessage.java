package com.varun.swim.message;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;

import java.io.IOException;
import java.util.List;

import static com.varun.swim.util.Constants.ACK_MESSAGE;
import static com.varun.swim.util.Constants.PING_MESSAGE;

public record PingMessage(int fromPort, int selfPort,
                          ServerSyncState serverSyncState,
                          List<Integer> failedNodes,
                          List<Integer> suspectedNodes) implements ServerMessage {

    @Override
    public void interpret() throws IOException {
        System.out.printf("Received %s from %d\n", PING_MESSAGE, this.fromPort);
        this.serverSyncState.markNodeAsAlive(this.fromPort);
        for (Integer failedNode : failedNodes) {
            serverSyncState.markNodeAsFailed(failedNode);
        }
        CustomClient client = new CustomClient(this.fromPort);
        client.sendMessage(String.format("%s %d", ACK_MESSAGE, selfPort));
        client.close();
    }
}
