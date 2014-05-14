package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.DataTypes.*;

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

        public void get(ByteBuffer buffer) {
            session        = getAlphanumeric(buffer, 10);
            sequenceNumber = getNumeric(buffer, 20);
        }

        public void put(ByteBuffer buffer) {
            putAlphanumericPadLeft(buffer, session, 10);
            putNumeric(buffer, sequenceNumber, 20);
        }
    }

    /**
     * Payload for the Login Rejected packet.
     */
    public static class LoginRejected {
        public byte rejectReasonCode;

        public void get(ByteBuffer buffer) {
            rejectReasonCode = buffer.get();
        }

        public void put(ByteBuffer buffer) {
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

        public void get(ByteBuffer buffer) {
            username                = getAlphanumeric(buffer,  6);
            password                = getAlphanumeric(buffer, 10);
            requestedSession        = getAlphanumeric(buffer, 10);
            requestedSequenceNumber = getNumeric(buffer, 20);
        }

        public void put(ByteBuffer buffer) {
            putAlphanumericPadRight(buffer, username, 6);
            putAlphanumericPadRight(buffer, password, 10);
            putAlphanumericPadLeft(buffer, requestedSession, 10);
            putNumeric(buffer, requestedSequenceNumber, 20);
        }
    }

}
