package com.paritytrading.nassau.moldudp64;

import static com.paritytrading.nassau.moldudp64.MoldUDP64ClientState.*;
import static com.paritytrading.nassau.moldudp64.MoldUDP64ClientStatus.*;
import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;

import com.paritytrading.nassau.Messages;
import com.paritytrading.nassau.Strings;
import com.paritytrading.nassau.time.FixedClock;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

public class MoldUDP64SessionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Rule
    public Timeout timeout = new Timeout(1000);

    private FixedClock clock;

    private MoldUDP64DownstreamPacket packet;

    private Messages<String> clientMessages;

    private MoldUDP64ClientStatus clientStatus;

    private MoldUDP64Client client;

    private MoldUDP64Server server;

    private MoldUDP64RequestServer requestServer;

    private MoldUDP64DefaultMessageStore store;

    @Before
    public void setUp() throws Exception {
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
                requestAddress, clientMessages, clientStatus);

        server = new MoldUDP64Server(clock, serverChannel, "nassau");

        requestServer = new MoldUDP64RequestServer(serverRequestChannel);

        store = new MoldUDP64DefaultMessageStore();
    }

    @After
    public void tearDown() throws Exception {
        requestServer.close();

        server.close();
        client.close();
    }

    @Test
    public void packetWithSingleMessage() throws Exception {
        packet.clear();
        packet.put(wrap("foo"));

        server.send(packet);

        client.receive();

        assertEquals(asList("foo"), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void packetWithMultipleMessages() throws Exception {
        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));

        server.send(packet);

        client.receive();

        assertEquals(asList("foo", "bar"), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void heartbeat() throws Exception {
        server.sendHeartbeat();

        client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void endOfSession() throws Exception {
        server.sendEndOfSession();

        client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new EndOfSession(), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void keepAlive() throws Exception {
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
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new Downstream()), clientStatus.collect());
    }

    @Test
    public void maximumMessageLength() throws Exception {
        String message = repeat('X', 1398);

        packet.put(wrap(message));

        server.send(packet);

        client.receive();

        assertEquals(asList(message), clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void maximumMessageLengthExceeded() throws Exception {
        exception.expect(MoldUDP64Exception.class);

        packet.put(wrap(repeat('X', 1399)));
    }

    @Test
    public void backfill() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.nextSequenceNumber = 4;
        server.sendHeartbeat();

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(BACKFILL), new Request(1, 3),
                    new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void gapFill() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.nextSequenceNumber = 1;
        server.sendHeartbeat();

        client.receive();

        server.nextSequenceNumber = 4;
        server.sendHeartbeat();

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new State(GAP_FILL), new Request(1, 3),
                    new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void gapFillWithMultipleRequests() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.nextSequenceNumber = 1;
        server.sendHeartbeat();

        client.receive();

        server.nextSequenceNumber = 2;
        server.sendHeartbeat();

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));
        packet.put(wrap("baz"));

        server.nextSequenceNumber = 2;
        server.send(packet);

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new State(GAP_FILL), new Request(1, 1),
                    new Request(2, 2), new Downstream(),
                    new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void gapFillAfterEndOfSession() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        for (String message : messages)
            store.put(wrap(message));

        server.nextSequenceNumber = 1;
        server.sendHeartbeat();

        client.receive();

        server.nextSequenceNumber = 4;
        server.sendEndOfSession();

        client.receive();

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new State(GAP_FILL), new Request(1, 3),
                    new State(SYNCHRONIZED), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void gapFillTimeout() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        server.nextSequenceNumber = 1;
        server.sendHeartbeat();

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));

        server.nextSequenceNumber = 2;
        server.send(packet);

        client.receive();

        requestServer.serve(store);

        clock.setCurrentTimeMillis(1500);

        packet.clear();
        packet.put(wrap("baz"));

        server.nextSequenceNumber = 3;
        server.send(packet);

        client.receive();

        for (String message : messages)
            store.put(wrap(message));

        requestServer.serve(store);

        client.receiveResponse();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new State(GAP_FILL), new Request(1, 2),
                    new Request(1, 3), new State(SYNCHRONIZED),
                    new Downstream()), clientStatus.collect());
    }

    @Test
    public void partiallyObsoletePacket() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        packet.clear();
        packet.put(wrap("foo"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("baz"));

        server.nextSequenceNumber = 3;
        server.send(packet);

        client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new Downstream(), new Downstream()),
                clientStatus.collect());
    }

    @Test
    public void fullyObsoletePacket() throws Exception {
        List<String> messages = asList("foo", "bar", "baz");

        packet.clear();
        packet.put(wrap("foo"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("foo"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        client.receive();

        packet.clear();
        packet.put(wrap("bar"));
        packet.put(wrap("baz"));

        server.nextSequenceNumber = 2;
        server.send(packet);

        client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new State(SYNCHRONIZED), new Downstream(),
                    new Downstream(), new Downstream()),
                clientStatus.collect());
    }

}
