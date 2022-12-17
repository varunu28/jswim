package com.varun.swim.message;

import com.varun.swim.ServerSyncState;

import java.util.Arrays;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.varun.swim.util.Constants.*;

public class ServerMessageFactory {

    private ServerMessageFactory() {
    }

    public static ServerMessage parseServerMessage(String input, int selfPort, ServerSyncState serverSyncState) {
        String[] splits = input.split("\\s+");
        String messageType = splits[0];
        return switch (messageType) {
            case PING_REQUEST -> new PingRequestMessage(Integer.parseInt(splits[1]), selfPort, serverSyncState);
            case PING_RESPONSE -> new PingResponseMessage(Integer.parseInt(splits[1]), serverSyncState);
            case BROADCAST_REQUEST -> new BroadcastMessage(Integer.parseInt(splits[2]), serverSyncState,
                    Arrays.stream(splits[1].split(","))
                            .mapToInt(Integer::valueOf)
                            .boxed()
                            .collect(toImmutableList()));
            default -> null;
        };
    }
}
