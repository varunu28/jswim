package com.varun.swim.task;

import com.varun.swim.CustomClient;
import com.varun.swim.logging.CustomLogger;

import java.util.List;

public class Broadcaster {

    private static final CustomLogger customLogger = new CustomLogger();

    public static void broadcastToRandomPeers(List<Integer> peerNodes, String message, long broadcastCount) {
        int peerNodeIdx = 0;
        while (broadcastCount-- > 0 && peerNodeIdx < peerNodes.size()) {
            CustomClient client;
            int randomPeerNode = peerNodes.get(peerNodeIdx++);
            try {
                client = new CustomClient(randomPeerNode);
                client.sendMessage(message);
                client.close();
            } catch (Exception e) {
                customLogger.logError(String.format(
                        "Exception while communicating with node on port %d", randomPeerNode));
            }
        }
    }
}
