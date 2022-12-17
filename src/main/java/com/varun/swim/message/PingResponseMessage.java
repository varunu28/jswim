package com.varun.swim.message;

import com.varun.swim.ServerSyncState;

import static com.varun.swim.util.Constants.PING_RESPONSE;

public record PingResponseMessage(int fromPort, ServerSyncState serverSyncState) implements ServerMessage {

    @Override
    public void interpret() {
        System.out.printf("Received %s from %d\n", PING_RESPONSE, this.fromPort);
        this.serverSyncState.markNodeAsAlive(this.fromPort);
    }
}
