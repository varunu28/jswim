package com.varun.swim.task;

import com.varun.swim.ServerSyncState;
import com.varun.swim.util.FileUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.varun.swim.util.Constants.PING_MESSAGE;
import static com.varun.swim.util.Constants.PING_PEER_COUNT;

public class PingTask implements Runnable {

    private final ServerSyncState serverSyncState;
    private final int port;

    public PingTask(ServerSyncState serverSyncState, int port) {
        this.serverSyncState = serverSyncState;
        this.port = port;
    }

    @Override
    public void run() {
        List<Integer> peerNodes;
        try {
            peerNodes = FileUtil.readPeerServerConfig(port)
                    .stream()
                    .filter(n -> !serverSyncState.isNodeMarkedAsFailed(n))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                        Collections.shuffle(collected);
                        return collected.stream();
                    }))
                    .limit(PING_PEER_COUNT)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Broadcaster.broadcastToRandomPeers(
                List.copyOf(peerNodes),
                String.format("%s %d %s", PING_MESSAGE, port, serverSyncState.toString()),
                PING_PEER_COUNT);
        peerNodes.forEach(this.serverSyncState::recordPingRequestTime);
    }
}
