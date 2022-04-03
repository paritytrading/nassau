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

import static com.paritytrading.nassau.Strings.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64ClientState.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64ClientStatus.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;

import com.paritytrading.nassau.FixedClock;
import com.paritytrading.nassau.Messages;
import com.paritytrading.nassau.Strings;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value=1, unit=TimeUnit.SECONDS)
class MoldUDP64SessionTest {

    private FixedClock clock;

    private MoldUDP64DownstreamPacket packet;

    private Messages<String> clientMessages;

    private MoldUDP64ClientStatus clientStatus;

    private MoldUDP64Client client;

    private MoldUDP64Server server;

    private MoldUDP64RequestServer requestServer;

    private MoldUDP64DefaultMessageStore store;

    @BeforeEach
    void setUp() throws Exception {
        DatagramChannel clientChannel = DatagramChannels.openClientChannel();
        DatagramChannel clientRequestChannel = DatagramChannels.openClientRequestChannel();

        DatagramChannel serverChannel = DatagramChannels.openServerChannel(clientChannel);
        DatagramChannel serverRequestChannel = DatagramChannels.openServerRequestChannel();

        SocketAddress requestAddress = serverRequestChannel.getLocalAddress();

        clock = new FixedClock();

        packet = new MoldUDP64DownstreamPacket();

        clientMessages = new Messages<>(Strings.MESSAGE_PARSER);

        clientStatus = new MoldUDP64ClientStatus();

        client = new MoldUDP64Client(clock, clientChannel, clientRequestChannel,
                requestAddress, clientMessages, clientStatus, 1);

        server = new MoldUDP64Server(clock, serverChannel, "nassau");

        requestServer = new MoldUDP64RequestServer(serverRequestChannel);

        store = new MoldUDP64DefaultMessageStore();
    }

    @AfterEach
    void tearDown() throws Exception {
        requestServer.close();

        server.close();
        client.close();
    }

    @Test
    void packetWithSingleMessage() throws Exception {
        packet.clear();
        packet.put(wrap("foo"));

        server.send(packet);

        client.receive();

        assertEquals(asList("foo"), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 1)),
                clientStatus.collect());
    }

    @Test
    void packetWithMultipleMessages() throws Exception {
        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));

        server.send(packet);

        client.receive();

        assertEquals(asList("foo", "bar"), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 2)),
                clientStatus.collect());
    }

    @Test
    void heartbeat() throws Exception {
        server.sendHeartbeat();

        client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 0)),
                clientStatus.collect());
    }

    @Test
    void endOfSession() throws Exception {
        server.sendEndOfSession();

        client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new EndOfSession(), new Downstream(1, 0)),
                clientStatus.collect());
    }

    @Test
    void keepAlive() throws Exception {
        clock.setCurrentTimeMillis(500);

        server.keepAlive();

        clock.setCurrentTimeMillis(1000);

        server.keepAlive();

        clock.setCurrentTimeMillis(1500);

        server.keepAlive();

        clock.setCurrentTimeMillis(2000);

        server.keepAlive();

        while (clientStatus.collect().size() != 3)
            client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 0),
                    new Downstream(1, 0)), clientStatus.collect());
    }

    @Test
    void maximumMessageLength() throws Exception {
        String message = repeat('X', 1398);

        packet.put(wrap(message));

        server.send(packet);

        client.receive();

        assertEquals(asList(message), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 1)),
                clientStatus.collect());
    }

    @Test
    void maximumMessageLengthExceeded() throws Exception {
        assertThrows(MoldUDP64Exception.class, () -> {
            packet.put(wrap(repeat('X', 1399)));
        });
    }

    @Test
    void backfill() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.setNextSequenceNumber(4);
        server.sendHeartbeat();

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(BACKFILL), new Request(1, 3),
                    new State(SYNCHRONIZED), new Downstream(1, 3)),
                clientStatus.collect());
    }

    @Test
    void gapFill() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.setNextSequenceNumber(1);
        server.sendHeartbeat();

        client.receive();

        server.setNextSequenceNumber(4);
        server.sendHeartbeat();

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 0),
                    new State(GAP_FILL), new Request(1, 3),
                    new State(SYNCHRONIZED), new Downstream(1, 3)),
                clientStatus.collect());
    }

    @Test
    void gapFillWithMultipleRequests() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.setNextSequenceNumber(1);
        server.sendHeartbeat();

        client.receive();

        server.setNextSequenceNumber(2);
        server.sendHeartbeat();

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));
        packet.put(wrap("baz"));

        server.setNextSequenceNumber(2);
        server.send(packet);

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 0),
                    new State(GAP_FILL), new Request(1, 1),
                    new Request(2, 2), new Downstream(1, 1),
                    new State(SYNCHRONIZED), new Downstream(2, 2)),
                clientStatus.collect());
    }

    @Test
    void gapFillAfterEndOfSession() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.setNextSequenceNumber(1);
        server.sendHeartbeat();

        client.receive();

        server.setNextSequenceNumber(4);
        server.sendEndOfSession();

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 0),
                    new State(GAP_FILL), new Request(1, 3),
                    new State(SYNCHRONIZED), new Downstream(1, 3)),
                clientStatus.collect());
    }

    @Test
    void gapFillTimeout() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        server.setNextSequenceNumber(1);
        server.sendHeartbeat();

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));

        server.setNextSequenceNumber(2);
        server.send(packet);

        client.receive();

        requestServer.serve(store);

        clock.setCurrentTimeMillis(1500);

        packet.clear();
        packet.put(wrap("baz"));

        server.setNextSequenceNumber(3);
        server.send(packet);

        client.receive();

        for (String message : messages)
            store.put(wrap(message));

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 0),
                    new State(GAP_FILL), new Request(1, 2),
                    new Request(1, 3), new State(SYNCHRONIZED),
                    new Downstream(1, 3)), clientStatus.collect());
    }

    @Test
    void partiallyObsoletePacket() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        packet.clear();
        packet.put(wrap("foo"));

        server.setNextSequenceNumber(1);
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));

        server.setNextSequenceNumber(1);
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("baz"));

        server.setNextSequenceNumber(3);
        server.send(packet);

        client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 1),
                    new Downstream(1, 2), new Downstream(3, 1)),
                clientStatus.collect());
    }

    @Test
    void fullyObsoletePacket() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        packet.clear();
        packet.put(wrap("foo"));

        server.setNextSequenceNumber(1);
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("foo"));

        server.setNextSequenceNumber(1);
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));
        packet.put(wrap("baz"));

        server.setNextSequenceNumber(2);
        server.send(packet);

        client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(1, 1),
                    new Downstream(1, 1), new Downstream(2, 2)),
                clientStatus.collect());
    }

    @Test
    void requestedSequenceNumber() throws Exception {
        List<String> messages = asList("bar");

        client.setNextExpectedSequenceNumber(4);

        packet.clear();
        packet.put(wrap("foo"));

        server.setNextSequenceNumber(3);
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));

        server.setNextSequenceNumber(4);
        server.send(packet);

        client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(3, 1),
                    new Downstream(4, 1)), clientStatus.collect());
    }

    @Test
    void firstReceivedMessage() throws Exception {
        List<String> messages = asList("foo", "bar");

        client.setNextExpectedSequenceNumber(0);

        packet.clear();
        packet.put(wrap("foo"));

        server.setNextSequenceNumber(3);
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));

        server.setNextSequenceNumber(4);
        server.send(packet);

        client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(3, 1),
                    new Downstream(4, 1)), clientStatus.collect());
    }

}
