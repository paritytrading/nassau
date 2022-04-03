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

import static com.paritytrading.nassau.Strings.*;
import static com.paritytrading.nassau.soupbintcp.SoupBinTCPClientStatus.*;
import static com.paritytrading.nassau.soupbintcp.SoupBinTCPServerStatus.*;
import static com.paritytrading.nassau.soupbintcp.SoupBinTCPSessionStatus.*;
import static java.util.Arrays.*;
import static org.junit.jupiter.api.Assertions.*;

import com.paritytrading.nassau.FixedClock;
import com.paritytrading.nassau.Messages;
import com.paritytrading.nassau.Strings;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(value=1, unit=TimeUnit.SECONDS)
class SoupBinTCPSessionTest {

    private static final int MAX_RX_PAYLOAD_LENGTH = 4000;
    private static final int MAX_TX_PAYLOAD_LENGTH = 65534;

    private SoupBinTCP.LoginAccepted loginAccepted;
    private SoupBinTCP.LoginRejected loginRejected;
    private SoupBinTCP.LoginRequest  loginRequest;

    private FixedClock clock;

    private Messages<String> clientMessages;
    private Messages<String> serverMessages;

    private SoupBinTCPClientStatus clientStatus;
    private SoupBinTCPServerStatus serverStatus;

    private SoupBinTCPClient client;
    private SoupBinTCPServer server;

    @BeforeEach
    void setUp() throws Exception {
        loginAccepted = new SoupBinTCP.LoginAccepted();
        loginRejected = new SoupBinTCP.LoginRejected();
        loginRequest  = new SoupBinTCP.LoginRequest();

        clock = new FixedClock();

        ServerSocketChannel acceptor = ServerSocketChannel.open();
        acceptor.bind(null);

        SocketChannel clientChannel = SocketChannel.open(acceptor.getLocalAddress());

        SocketChannel serverChannel = acceptor.accept();
        acceptor.close();

        clientMessages = new Messages<>(Strings.MESSAGE_PARSER);
        serverMessages = new Messages<>(Strings.MESSAGE_PARSER);

        clientStatus = new SoupBinTCPClientStatus();
        serverStatus = new SoupBinTCPServerStatus();

        client = new SoupBinTCPClient(clock, clientChannel, MAX_RX_PAYLOAD_LENGTH, clientMessages, clientStatus);
        server = new SoupBinTCPServer(clock, serverChannel, MAX_RX_PAYLOAD_LENGTH, serverMessages, serverStatus);
    }

    @AfterEach
    void tearDown() throws Exception {
        client.close();
        server.close();
    }

    @Test
    void loginAccepted() throws Exception {
        loginAccepted.setSession("foo");
        loginAccepted.setSequenceNumber(123);
        server.accept(loginAccepted);

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(asList(new LoginAccepted("       foo", 123)),
                clientStatus.collect());
    }

    @Test
    void loginRejected() throws Exception {
        loginRejected.setRejectReasonCode(SoupBinTCP.LOGIN_REJECT_CODE_NOT_AUTHORIZED);

        server.reject(loginRejected);

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(asList(new LoginRejected(SoupBinTCP.LOGIN_REJECT_CODE_NOT_AUTHORIZED)),
                clientStatus.collect());
    }

    @Test
    void endOfSession() throws Exception {
        server.endSession();

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(asList(new EndOfSession()), clientStatus.collect());
    }

    @Test
    void loginRequest() throws Exception {
        loginRequest.setUsername("foo");
        loginRequest.setPassword("bar");
        loginRequest.setRequestedSession("baz");
        loginRequest.setRequestedSequenceNumber(123);

        client.login(loginRequest);

        while (serverStatus.collect().size() != 1)
            server.receive();

        assertEquals(asList(new LoginRequest("foo   ", "bar       ",
                        "       baz", 123)), serverStatus.collect());
    }

    @Test
    void logoutRequest() throws Exception {
        client.logout();

        while (serverStatus.collect().size() != 1)
            server.receive();

        assertEquals(asList(new LogoutRequest()), serverStatus.collect());
    }

    @Test
    void unsequencedData() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        for (String message : messages)
            client.send(wrap(message));

        while (serverMessages.collect().size() != messages.size())
            server.receive();

        assertEquals(messages, serverMessages.collect());
    }

    @Test
    void maximumPacketLengthForInboundUnsequencedData() throws Exception {
        String message = repeat('X', MAX_RX_PAYLOAD_LENGTH);

        client.send(wrap(message));

        while (serverMessages.collect().size() != 1)
            server.receive();

        assertEquals(asList(message), serverMessages.collect());
    }

    @Test
    void maximumPacketLengthExceededForInboundUnsequencedData() throws Exception {
        String message = repeat('X', MAX_RX_PAYLOAD_LENGTH + 1);

        client.send(wrap(message));

        assertThrows(SoupBinTCPException.class, () -> {
            while (serverMessages.collect().size() != 1)
                server.receive();
        });
    }

    @Test
    void maximumPacketLengthForOutboundUnsequencedData() throws Exception {
        client.send(wrap(repeat('X', MAX_TX_PAYLOAD_LENGTH)));
    }

    @Test
    void maximumPacketLengthExceededForOutboundUnsequencedData() throws Exception {
        assertThrows(SoupBinTCPException.class, () -> {
            client.send(wrap(repeat('X', MAX_TX_PAYLOAD_LENGTH + 1)));
        });
    }

    @Test
    void sequencedData() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        for (String message : messages)
            server.send(wrap(message));

        while (clientMessages.collect().size() != messages.size())
            client.receive();

        assertEquals(messages, clientMessages.collect());
    }

    @Test
    void maximumPacketLengthForInboundSequencedData() throws Exception {
        String message = repeat('X', MAX_RX_PAYLOAD_LENGTH);

        server.send(wrap(message));

        while (clientMessages.collect().size() != 1)
            client.receive();

        assertEquals(asList(message), clientMessages.collect());
    }

    @Test
    void maximumPacketLengthExceededForInboundSequencedData() throws Exception {
        String message = repeat('X', MAX_RX_PAYLOAD_LENGTH + 1);

        server.send(wrap(message));

        assertThrows(SoupBinTCPException.class, () -> {
            while (clientMessages.collect().size() != 1)
                client.receive();
        });
    }

    @Test
    void maximumPacketLengthForOutboundSequencedData() throws Exception {
        server.send(wrap(repeat('X', MAX_TX_PAYLOAD_LENGTH)));
    }

    @Test
    void maximumPacketLengthExceededForOutboundSequencedData() throws Exception {
        assertThrows(SoupBinTCPException.class, () -> {
            server.send(wrap(repeat('X', MAX_TX_PAYLOAD_LENGTH + 1)));
        });
    }

    @Test
    void serverKeepAlive() throws Exception {
        clock.setCurrentTimeMillis(1500);

        client.keepAlive();
        server.keepAlive();

        server.receive();

        clock.setCurrentTimeMillis(15500);

        server.keepAlive();

        clock.setCurrentTimeMillis(16750);

        server.keepAlive();

        assertEquals(asList(new HeartbeatTimeout()), serverStatus.collect());
    }

    @Test
    void clientKeepAlive() throws Exception {
        clock.setCurrentTimeMillis(1500);

        client.keepAlive();
        server.keepAlive();

        client.receive();

        clock.setCurrentTimeMillis(15500);

        client.keepAlive();

        clock.setCurrentTimeMillis(16750);

        client.keepAlive();

        assertEquals(asList(new HeartbeatTimeout()), clientStatus.collect());
    }

}
