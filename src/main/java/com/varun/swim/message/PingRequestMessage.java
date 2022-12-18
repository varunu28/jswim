package com.varun.swim.message;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;

import java.io.IOException;

import static com.varun.swim.util.Constants.PING_MESSAGE;

public record PingRequestMessage(int fromPort, int forPort, int selfPort,
                                 ServerSyncState serverSyncState) implements ServerMessage {

    @Override
    public void interpret() throws IOException {
        CustomClient client = new CustomClient(this.forPort);
        client.sendMessage(String.format("%s %d %s", PING_MESSAGE, selfPort, serverSyncState.toString()));
        client.close();
        serverSyncState.recordIndirectPing(fromPort, forPort);
    }
}
