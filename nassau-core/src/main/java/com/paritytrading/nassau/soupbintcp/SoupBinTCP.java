package com.paritytrading.nassau.soupbintcp;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Common definitions.
 */
public class SoupBinTCP {

    /*
     * These messages can be sent by both the client and server.
     */
    static final byte PACKET_TYPE_DEBUG = '+';

    /*
     * These messages are sent by the server to the client.
     */
    static final byte PACKET_TYPE_LOGIN_ACCEPTED   = 'A';
    static final byte PACKET_TYPE_LOGIN_REJECTED   = 'J';
    static final byte PACKET_TYPE_SEQUENCED_DATA   = 'S';
    static final byte PACKET_TYPE_SERVER_HEARTBEAT = 'H';
    static final byte PACKET_TYPE_END_OF_SESSION   = 'Z';

    /*
     * These messages are sent by the client to the server.
     */
    static final byte PACKET_TYPE_LOGIN_REQUEST    = 'L';
    static final byte PACKET_TYPE_UNSEQUENCED_DATA = 'U';
    static final byte PACKET_TYPE_CLIENT_HEARTBEAT = 'R';
    static final byte PACKET_TYPE_LOGOUT_REQUEST   = 'O';

    static final int MAX_PACKET_LENGTH = 65535;

    public static final byte LOGIN_REJECT_CODE_NOT_AUTHORIZED        = 'A';
    public static final byte LOGIN_REJECT_CODE_SESSION_NOT_AVAILABLE = 'S';

    private SoupBinTCP() {
    }

    /**
     * Payload for the Login Accepted packet.
     */
    public static class LoginAccepted {
        public byte[] session;
        public byte[] sequenceNumber;

        /**
         * Construct an instance.
         */
        public LoginAccepted() {
            session        = new byte[10];
            sequenceNumber = new byte[20];
        }

        void get(ByteBuffer buffer) throws IOException {
            buffer.get(session);
            buffer.get(sequenceNumber);
        }

        void put(ByteBuffer buffer) {
            buffer.put(session);
            buffer.put(sequenceNumber);
        }
    }

    /**
     * Payload for the Login Rejected packet.
     */
    public static class LoginRejected {
        public byte rejectReasonCode;

        /**
         * Construct an instance.
         */
        public LoginRejected() {
        }

        void get(ByteBuffer buffer) {
            rejectReasonCode = buffer.get();
        }

        void put(ByteBuffer buffer) {
            buffer.put(rejectReasonCode);
        }
    }

    /**
     * Payload for the Login Request packet.
     */
    public static class LoginRequest {
        public byte[] username;
        public byte[] password;
        public byte[] requestedSession;
        public byte[] requestedSequenceNumber;

        /**
         * Construct an instance.
         */
        public LoginRequest() {
            username                = new byte[6];
            password                = new byte[10];
            requestedSession        = new byte[10];
            requestedSequenceNumber = new byte[20];
        }

        void get(ByteBuffer buffer) throws IOException {
            buffer.get(username);
            buffer.get(password);
            buffer.get(requestedSession);
            buffer.get(requestedSequenceNumber);
        }

        void put(ByteBuffer buffer) {
            buffer.put(username);
            buffer.put(password);
            buffer.put(requestedSession);
            buffer.put(requestedSequenceNumber);
        }
    }

}
