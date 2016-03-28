package com.paritytrading.nassau.soupbintcp;

import static com.paritytrading.nassau.soupbintcp.DataTypes.*;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Common definitions.
 */
public class SoupBinTCP {

    public static final byte LOGIN_REJECT_CODE_NOT_AUTHORIZED        = 'A';
    public static final byte LOGIN_REJECT_CODE_SESSION_NOT_AVAILABLE = 'S';

    private SoupBinTCP() {
    }

    /**
     * Payload for the Login Accepted packet.
     */
    public static class LoginAccepted {
        public String session;
        public long   sequenceNumber;

        /**
         * Construct an instance.
         */
        public LoginAccepted() {
        }

        /**
         * Construct an instance.
         *
         * @param session the session
         * @param sequenceNumber the sequence number
         */
        public LoginAccepted(String session, long sequenceNumber) {
            this.session        = session;
            this.sequenceNumber = sequenceNumber;
        }

        void get(ByteBuffer buffer) throws IOException {
            session        = getAlphanumeric(buffer, 10);
            sequenceNumber = getNumeric(buffer, 20);
        }

        void put(ByteBuffer buffer) {
            putAlphanumericPadLeft(buffer, session, 10);
            putNumeric(buffer, sequenceNumber, 20);
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

        /**
         * Construct an instance.
         *
         * @param rejectReasonCode the reject reason code
         */
        public LoginRejected(byte rejectReasonCode) {
            this.rejectReasonCode = rejectReasonCode;
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
        public String username;
        public String password;
        public String requestedSession;
        public long   requestedSequenceNumber;

        /**
         * Construct an instance.
         */
        public LoginRequest() {
        }

        /**
         * Construct an instance.
         *
         * @param username the username
         * @param password the password
         * @param requestedSession the requested session
         * @param requestedSequenceNumber the requested sequence number
         */
        public LoginRequest(String username, String password, String requestedSession,
                long requestedSequenceNumber) {
            this.username                = username;
            this.password                = password;
            this.requestedSession        = requestedSession;
            this.requestedSequenceNumber = requestedSequenceNumber;
        }

        void get(ByteBuffer buffer) throws IOException {
            username                = getAlphanumeric(buffer,  6);
            password                = getAlphanumeric(buffer, 10);
            requestedSession        = getAlphanumeric(buffer, 10);
            requestedSequenceNumber = getNumeric(buffer, 20);
        }

        void put(ByteBuffer buffer) {
            putAlphanumericPadRight(buffer, username, 6);
            putAlphanumericPadRight(buffer, password, 10);
            putAlphanumericPadLeft(buffer, requestedSession, 10);
            putNumeric(buffer, requestedSequenceNumber, 20);
        }
    }

}
