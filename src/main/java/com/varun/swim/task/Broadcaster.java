package com.varun.swim.task;

import com.varun.swim.CustomClient;

import java.util.List;

public class Broadcaster {

    public static void broadcastToRandomPeers(List<Integer> peerNodes, String message, long broadcastCount) {
        int peerNodeIdx = 0;
        while (broadcastCount-- > 0 && peerNodeIdx < peerNodes.size()) {
            CustomClient client;
            int randomPeerNode = peerNodes.get(peerNodeIdx++);
            System.out.printf("Broadcasting to %d\n", randomPeerNode);
            try {
                client = new CustomClient(randomPeerNode);
                client.sendMessage(message);
                client.close();
            } catch (Exception e) {
                System.out.printf("Exception while communicating with node on port %d \n", randomPeerNode);
            }
        }
    }
}
