package com.varun.swim.message;

import com.varun.swim.ServerSyncState;

import java.util.Arrays;
import java.util.List;

import static com.varun.swim.util.Constants.*;

public class ServerMessageFactory {

    private ServerMessageFactory() {
    }

    public static ServerMessage parseServerMessage(String input, int selfPort, ServerSyncState serverSyncState) {
        String[] splits = input.split("\\s+");
        String messageType = splits[0];
        switch (messageType) {
            case PING_MESSAGE -> {
                List<Integer> failedNodes = Arrays.stream(splits[2].split(":")[1].split(","))
                        .mapToInt(Integer::valueOf)
                        .boxed()
                        .toList();
                List<Integer> suspectedNodes = Arrays.stream(splits[3].split(":")[1].split(","))
                        .mapToInt(Integer::valueOf)
                        .boxed()
                        .toList();
                return new PingMessage(Integer.parseInt(splits[1]), selfPort, serverSyncState, failedNodes, suspectedNodes);
            }
            case PING_REQUEST_MESSAGE -> {
                return new PingRequestMessage(
                        Integer.parseInt(splits[1]), Integer.parseInt(splits[2]), selfPort, serverSyncState);
            }
            case ACK_MESSAGE -> {
                return new AckMessage(Integer.parseInt(splits[1]), serverSyncState);
            }
        }
        return null;
    }
}
