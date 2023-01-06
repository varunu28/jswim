package com.varun.swim.message;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;

import static com.varun.swim.util.Constants.PING_MESSAGE;
import static com.varun.swim.util.Constants.PING_REQUEST_MESSAGE;

public record PingRequestMessage(int fromPort, int forPort, int selfPort,
                                 ServerSyncState serverSyncState) implements ServerMessage {

    @Override
    public void interpret() {
        try {
            System.out.printf("Received %s from %d for %d\n", PING_REQUEST_MESSAGE, this.fromPort, this.forPort);
            CustomClient client = new CustomClient(this.forPort);
            client.sendMessage(String.format("%s %d %s", PING_MESSAGE, selfPort, serverSyncState.toString()));
            client.close();
            serverSyncState.recordIndirectPing(fromPort, forPort);
        } catch (Exception e) {
            System.out.printf("Exception while indirectly pinging %d on behalf of %d\n", this.forPort, this.fromPort);
        }
    }
}
