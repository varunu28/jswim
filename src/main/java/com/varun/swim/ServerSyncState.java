package com.varun.swim;

import com.varun.swim.logging.CustomLogger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.varun.swim.util.Constants.*;
import static java.util.stream.Collectors.toSet;

public class ServerSyncState {

    private final Set<Integer> failedNodes;
    private final Map<Integer, Long> nodeToPingTime;
    private final Map<Integer, List<Integer>> indirectPingMapping;
    private static final CustomLogger CUSTOM_LOGGER = new CustomLogger();
    private final Map<Integer, Long> suspectedNodeToPingTime;

    public ServerSyncState() {
        this.failedNodes = new HashSet<>();
        this.nodeToPingTime = new HashMap<>();
        this.indirectPingMapping = new HashMap<>();
        this.suspectedNodeToPingTime = new HashMap<>();
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
        updateFailedNodesList(currentTimeMillis);
        return Set.copyOf(failedNodes);
    }

    private void updateFailedNodesList(long currentTimeMillis) {
        Set<Integer> nodes = Set.copyOf(suspectedNodeToPingTime.keySet());
        for (Integer node : nodes) {
            long duration = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - nodeToPingTime.get(node));
            if (duration >= FAILURE_THRESHOLD) {
                markNodeAsFailed(node);
            }
        }
    }

    public Set<Integer> getNodesPassSuspectThreshold(long currentTimeMillis) {
        updateSuspectedNodesList(currentTimeMillis);
        return Set.copyOf(suspectedNodeToPingTime.keySet());
    }

    private void updateSuspectedNodesList(long currentTimeMillis) {
        Set<Integer> nodes = Set.copyOf(nodeToPingTime.keySet());
        for (Integer node : nodes) {
            long duration = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - nodeToPingTime.get(node));
            if (duration >= SUSPECT_THRESHOLD && !suspectedNodeToPingTime.containsKey(node)) {
                CUSTOM_LOGGER.logDebug(String.format("Marking node %d as SUSPECT", node));
                suspectedNodeToPingTime.put(node, nodeToPingTime.get(node));
                nodeToPingTime.remove(node);
            }
        }
    }

    public void recordPingRequestTime(int node) {
        this.nodeToPingTime.putIfAbsent(node, System.currentTimeMillis());
    }

    public void markNodeAsFailed(int node) {
        if (failedNodes.add(node)) {
            CUSTOM_LOGGER.logDebug(String.format("Marking node %d as FAILED", node));
            nodeToPingTime.remove(node);
            suspectedNodeToPingTime.remove(node);
        }
    }

    public boolean isNodeMarkedAsFailed(int node) {
        return this.failedNodes.contains(node);
    }

    public void markNodeAsAlive(int node) {
        if (suspectedNodeToPingTime.containsKey(node)) {
            suspectedNodeToPingTime.remove(node);
        } else {
            this.nodeToPingTime.remove(node);
        }
    }

    @Override
    public String toString() {
        String allFailedNodes = failedNodes.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        String allSuspectNodes = suspectedNodeToPingTime.keySet()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return PING_CONFIRM +
                ":" +
                (failedNodes.isEmpty() ? "," : allFailedNodes) +
                " " +
                PING_SUSPECT +
                ":" +
                (suspectedNodeToPingTime.isEmpty() ? "," : allSuspectNodes);
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
