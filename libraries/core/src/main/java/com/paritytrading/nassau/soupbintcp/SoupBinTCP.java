/*
 * Copyright 2014 Nassau authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        private byte[] session;
        private byte[] sequenceNumber;

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

        /**
         * Get the session.
         *
         * @return the session
         */
        public String getSession() {
            return ASCII.get(session);
        }

        /**
         * Set the session.
         *
         * @param session the session
         */
        public void setSession(String session) {
            ASCII.putRight(this.session, session);
        }

        /**
         * Get the sequence number.
         *
         * @return the sequence number
         */
        public long getSequenceNumber() {
            return ASCII.getLong(sequenceNumber);
        }

        /**
         * Set the sequence number.
         *
         * @param sequenceNumber the sequence number
         */
        public void setSequenceNumber(long sequenceNumber) {
            ASCII.putLongRight(this.sequenceNumber, sequenceNumber);
        }
    }

    /**
     * Payload for the Login Rejected packet.
     */
    public static class LoginRejected {
        private byte rejectReasonCode;

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

        /**
         * Get the reject reason code.
         *
         * @return the reject reason code
         */
        public byte getRejectReasonCode() {
            return rejectReasonCode;
        }

        /**
         * Set the reject reason code.
         *
         * @param rejectReasonCode the reject reason code
         */
        public void setRejectReasonCode(byte rejectReasonCode) {
            this.rejectReasonCode = rejectReasonCode;
        }
    }

    /**
     * Payload for the Login Request packet.
     */
    public static class LoginRequest {
        private byte[] username;
        private byte[] password;
        private byte[] requestedSession;
        private byte[] requestedSequenceNumber;

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

        /**
         * Get the user name.
         *
         * @return the user name
         */
        public String getUsername() {
            return ASCII.get(username);
        }

        /**
         * Set the user name.
         *
         * @param username the user name
         */
        public void setUsername(String username) {
            ASCII.putLeft(this.username, username);
        }

        /**
         * Get the password.
         *
         * @return the password
         */
        public String getPassword() {
            return ASCII.get(password);
        }

        /**
         * Set the password.
         *
         * @param password the password
         */
        public void setPassword(String password) {
            ASCII.putLeft(this.password, password);
        }

        /**
         * Get the requested session.
         *
         * @return the requested session
         */
        public String getRequestedSession() {
            return ASCII.get(requestedSession);
        }

        /**
         * Set the requested session.
         *
         * @param requestedSession the requested session
         */
        public void setRequestedSession(String requestedSession) {
            ASCII.putRight(this.requestedSession, requestedSession);
        }

        /**
         * Get the requested sequence number.
         *
         * @return the requested sequence number
         */
        public long getRequestedSequenceNumber() {
            return ASCII.getLong(requestedSequenceNumber);
        }

        /**
         * Set the requested sequence number.
         *
         * @param requestedSequenceNumber the requested sequence number
         */
        public void setRequestedSequenceNumber(long requestedSequenceNumber) {
            ASCII.putLongRight(this.requestedSequenceNumber, requestedSequenceNumber);
        }
    }

}
