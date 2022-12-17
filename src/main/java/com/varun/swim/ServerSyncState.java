package com.varun.swim;

import com.varun.swim.util.Constants;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ServerSyncState {

    private final Set<Integer> failedNodes;

    public ServerSyncState() {
        this.failedNodes = new HashSet<>();
    }

    public void markNodeAsFailed(int port) {
        this.failedNodes.add(port);
    }

    public boolean isNodeMarkedAsFailed(int node) {
        return this.failedNodes.contains(node);
    }

    public void markNodeAsAlive(int node) {
        this.failedNodes.remove(node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String allFailedNodes = failedNodes.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        sb.append(Constants.BROADCAST_REQUEST)
                .append(" ")
                .append(allFailedNodes.isEmpty() ? "," : allFailedNodes);
        return sb.toString();
    }
}
