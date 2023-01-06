package com.varun.swim;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.varun.swim.util.Constants.*;
import static java.util.stream.Collectors.toSet;

public class ServerSyncState {

    private final Set<Integer> failedNodes;
    private final Map<Integer, Long> nodeToPingTime;

    private final Map<Integer, List<Integer>> indirectPingMapping;

    public ServerSyncState() {
        this.failedNodes = new HashSet<>();
        this.nodeToPingTime = new HashMap<>();
        this.indirectPingMapping = new HashMap<>();
    }

    public Set<Integer> getNodesForIndirectPing(long currentTimeMillis) {
        return this.nodeToPingTime.entrySet()
                .stream()
                .filter(e -> {
                    long timeInMillis = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - e.getValue());
                    return timeInMillis >= INDIRECT_PING_THRESHOLD && timeInMillis < SUSPECT_THRESHOLD;
                })
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    public Set<Integer> getNodesPassFailedThreshold(long currentTimeMillis) {
        return this.nodeToPingTime.entrySet()
                .stream()
                .filter(e -> TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - e.getValue()) >= FAILURE_THRESHOLD)
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    public Set<Integer> getNodesPassSuspectThreshold(long currentTimeMillis) {
        return this.nodeToPingTime.entrySet()
                .stream()
                .filter(e -> {
                    long timeInMillis = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - e.getValue());
                    return timeInMillis < FAILURE_THRESHOLD && timeInMillis >= SUSPECT_THRESHOLD;
                })
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    public void recordPingRequestTime(int node) {
        this.nodeToPingTime.putIfAbsent(node, System.currentTimeMillis());
    }

    public void markNodeAsFailed(int node) {
        if (this.failedNodes.add(node)) {
            System.out.printf("Marking node %d as FAILED\n", node);
            this.nodeToPingTime.remove(node);
        }
    }

    public boolean isNodeMarkedAsFailed(int node) {
        return this.failedNodes.contains(node);
    }

    public void markNodeAsAlive(int node) {
        this.nodeToPingTime.remove(node);
    }

    @Override
    public String toString() {
        String allFailedNodes = failedNodes.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        Set<Integer> suspectedNodes = getNodesPassSuspectThreshold(System.currentTimeMillis());
        String allSuspectNodes = suspectedNodes
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return PING_CONFIRM +
                ":" +
                (failedNodes.isEmpty() ? "," : allFailedNodes) +
                " " +
                PING_SUSPECT +
                ":" +
                (suspectedNodes.isEmpty() ? "," : allSuspectNodes);
    }

    public void recordIndirectPing(int fromPort, int forPort) {
        this.indirectPingMapping.computeIfAbsent(forPort, k -> new ArrayList<>()).add(fromPort);
    }

    public List<Integer> getRecordForIndirectPing(int fromPort) {
        return List.copyOf(this.indirectPingMapping.getOrDefault(fromPort, new ArrayList<>()));
    }

    public void updateIndirectPingRecord(int fromPort, List<Integer> indirectPingRequestsForNode) {
        this.indirectPingMapping.getOrDefault(fromPort, new ArrayList<>())
                .removeAll(indirectPingRequestsForNode);
    }
}
