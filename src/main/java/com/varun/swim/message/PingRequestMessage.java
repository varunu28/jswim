package com.varun.swim.message;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;

import java.io.IOException;

import static com.varun.swim.util.Constants.PING_RESPONSE;

public record PingRequestMessage(int fromPort, int selfPort,
                                 ServerSyncState serverSyncState) implements ServerMessage {

    @Override
    public void interpret() throws IOException {
        CustomClient client = new CustomClient(this.fromPort);
        client.sendMessage(String.format("%s %d", PING_RESPONSE, selfPort));
        client.close();
        this.serverSyncState.markNodeAsAlive(this.fromPort);
    }
}
