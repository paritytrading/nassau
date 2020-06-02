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

import static com.paritytrading.nassau.soupbintcp.SoupBinTCP.*;

import java.io.IOException;

/**
 * The interface for inbound status events on the server side.
 */
public interface SoupBinTCPServerStatusListener {

    /**
     * Receive an indication of a heartbeat timeout.
     *
     * @param session the session
     * @throws IOException if an I/O error occurs
     */
    void heartbeatTimeout(SoupBinTCPServer session) throws IOException;

    /**
     * Receive a Login Request packet.
     *
     * @param session the session
     * @param payload the packet payload
     * @throws IOException if an I/O error occurs
     */
    void loginRequest(SoupBinTCPServer session, LoginRequest payload) throws IOException;

    /**
     * Receive a Logout Request packet.
     *
     * @param session the session
     * @throws IOException if an I/O error occurs
     */
    void logoutRequest(SoupBinTCPServer session) throws IOException;

}
