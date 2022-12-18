package com.varun.swim.task;

import com.varun.swim.ServerSyncState;
import com.varun.swim.util.FileUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.varun.swim.util.Constants.*;

public class FailureDetectionTask implements Runnable {

    private final ServerSyncState serverSyncState;
    private final int port;

    public FailureDetectionTask(ServerSyncState serverSyncState, int port) {
        this.serverSyncState = serverSyncState;
        this.port = port;
    }

    @Override
    public void run() {
        Set<Integer> failedNodes = serverSyncState.getNodesPassFailedThreshold(System.currentTimeMillis());
        List<Integer> peerNodes;
        try {
            peerNodes = FileUtil.readPeerServerConfig(port)
                    .stream()
                    .filter(n -> !serverSyncState.isNodeMarkedAsFailed(n))
                    .filter(n -> !failedNodes.contains(n))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                        Collections.shuffle(collected);
                        return collected.stream();
                    }))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Integer node : failedNodes) {
            Broadcaster.broadcastToRandomPeers(
                    List.copyOf(peerNodes),
                    String.format("%s %d %s:%d %s:,", PING_MESSAGE, port, PING_CONFIRM, node, PING_SUSPECT),
                    PING_PEER_COUNT);
            this.serverSyncState.markNodeAsFailed(node);
        }
    }
}
