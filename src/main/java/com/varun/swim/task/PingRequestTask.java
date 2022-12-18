package com.varun.swim.task;

import com.varun.swim.ServerSyncState;
import com.varun.swim.util.FileUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.varun.swim.util.Constants.PING_PEER_COUNT;
import static com.varun.swim.util.Constants.PING_REQUEST_MESSAGE;
import static java.util.stream.Collectors.toList;

public class PingRequestTask implements Runnable {

    private final ServerSyncState serverSyncState;
    private final int port;

    public PingRequestTask(ServerSyncState serverSyncState, int port) {
        this.serverSyncState = serverSyncState;
        this.port = port;
    }

    @Override
    public void run() {
        Set<Integer> indirectPingNodes = serverSyncState.getNodesForIndirectPing(System.currentTimeMillis());
        List<Integer> peerNodes;
        try {
            peerNodes = FileUtil.readPeerServerConfig(port)
                    .stream()
                    .filter(serverSyncState::isNodeMarkedAsFailed)
                    .collect(Collectors.collectingAndThen(toList(), collected -> {
                        Collections.shuffle(collected);
                        return collected.stream();
                    }))
                    .collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Integer node : indirectPingNodes) {
            Broadcaster.broadcastToRandomPeers(
                    peerNodes,
                    String.format("%s %d %d", PING_REQUEST_MESSAGE, port, node),
                    PING_PEER_COUNT);
        }
    }
}
