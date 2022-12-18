package com.varun.swim.message;

import com.varun.swim.CustomClient;
import com.varun.swim.ServerSyncState;

import java.io.IOException;
import java.util.List;

import static com.varun.swim.util.Constants.ACK_MESSAGE;

public record AckMessage(int fromPort, ServerSyncState serverSyncState) implements ServerMessage {

    @Override
    public void interpret() throws IOException {
        System.out.printf("Received %s from %d\n", ACK_MESSAGE, this.fromPort);
        this.serverSyncState.markNodeAsAlive(this.fromPort);
        List<Integer> indirectPingRequestsForNode = serverSyncState.getRecordForIndirectPing(fromPort);
        for (Integer node : indirectPingRequestsForNode) {
            CustomClient client = new CustomClient(node);
            client.sendMessage(String.format("%s %d", ACK_MESSAGE, fromPort));
            client.close();
        }
        serverSyncState.updateIndirectPingRecord(fromPort, indirectPingRequestsForNode);
    }
}
