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
package com.paritytrading.nassau.moldudp64;

/**
 * The state of a MoldUDP64 client in relation to the MoldUDP64 server.
 */
public enum MoldUDP64ClientState {

    /**
     * The state of the client in relation to the server is unknown.
     */
    UNKNOWN,

    /**
     * The client is yet to achieve synchronization with the server.
     */
    BACKFILL,

    /**
     * The client is in synchronization with the server.
     */
    SYNCHRONIZED,

    /**
     * The client has fallen out of synchronization with the server.
     */
    GAP_FILL,

}
