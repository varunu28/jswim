package com.varun.swim.message;

import com.google.common.collect.ImmutableList;
import com.varun.swim.ServerSyncState;

import java.io.IOException;

public record BroadcastMessage(int fromPort, ServerSyncState serverSyncState,
                               ImmutableList<Integer> broadcastedFailedNodes) implements ServerMessage {

    @Override
    public void interpret() throws IOException {
        System.out.printf("Received broadcast message from port: %d \n", fromPort);
        for (Integer node : broadcastedFailedNodes) {
            this.serverSyncState.markNodeAsFailed(node);
        }
    }
}
