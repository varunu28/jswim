package com.varun.swim.util;

public class Constants {

    public static final String PING_MESSAGE = "ping_message";

    public static final String PING_REQUEST_MESSAGE = "ping_request_message";

    public static final String PING_CONFIRM = "ping_confirm";

    public static final String PING_SUSPECT = "ping_suspect";

    public static final String ACK_MESSAGE = "ack_message";

    public static final Long PING_INTERVAL = 5L;

    public static final Long INDIRECT_PING_THRESHOLD = 6L;

    public static final Long SUSPECT_THRESHOLD = 7L;

    public static final Long FAILURE_THRESHOLD = 8L;

    public static final Long PING_PEER_COUNT = 2L;

    private Constants() {
    }
}
