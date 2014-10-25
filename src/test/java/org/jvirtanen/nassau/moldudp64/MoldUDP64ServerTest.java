package org.jvirtanen.nassau.moldudp64;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64ClientStatus.*;
import static org.jvirtanen.nassau.util.Strings.*;

import java.nio.channels.DatagramChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.jvirtanen.nassau.Messages;
import org.jvirtanen.nassau.util.FixedClock;
import org.jvirtanen.nassau.util.Strings;

public class MoldUDP64ServerTest {

    @Rule
    public Timeout timeout = new Timeout(1000);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private FixedClock clock;

    private DatagramChannel requestChannel;

    private MoldUDP64DownstreamPacket packet;

    private Messages<String> clientMessages;

    private MoldUDP64ClientStatus clientStatus;

    private MoldUDP64Client client;
    private MoldUDP64Server server;

    @Before
    public void setUp() throws Exception {
        DatagramChannel clientChannel = DatagramChannels.openClientChannel();
        DatagramChannel serverChannel = DatagramChannels.openServerChannel(clientChannel);

        clock = new FixedClock();

        requestChannel = DatagramChannels.openRequestChannel();

        packet = new MoldUDP64DownstreamPacket();

        clientMessages = new Messages<>(Strings.MESSAGE_PARSER);

        clientStatus = new MoldUDP64ClientStatus();

        client = new MoldUDP64Client(clientChannel, requestChannel.getLocalAddress(), clientMessages, clientStatus);
        server = new MoldUDP64Server(clock, serverChannel, "nassau");
    }

    @After
    public void tearDown() throws Exception {
        server.close();
        client.close();

        requestChannel.close();
    }

    @Test
    public void packetWithSingleMessage() throws Exception {
        packet.clear();
        packet.put(wrap("foo"));

        server.send(packet);

        packet.clear();
        packet.put(wrap("bar"));

        server.send(packet);

        while (clientMessages.collect().size() != 2)
            client.receive();

        assertEquals(asList("foo", "bar"), clientMessages.collect());
        assertEquals(asList(new Downstream(), new Downstream()), clientStatus.collect());
    }

    @Test
    public void packetWithMultipleMessages() throws Exception {
        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));

        server.send(packet);

        packet.clear();
        packet.put(wrap("baz"));
        packet.put(wrap("quux"));

        server.send(packet);

        while (clientMessages.collect().size() != 4)
            client.receive();

        assertEquals(asList("foo", "bar", "baz", "quux"), clientMessages.collect());
        assertEquals(asList(new Downstream(), new Downstream()), clientStatus.collect());
    }

    @Test
    public void heartbeat() throws Exception {
        server.sendHeartbeat();

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new Downstream()), clientStatus.collect());
    }

    @Test
    public void endOfSession() throws Exception {
        server.sendEndOfSession();

        while (clientStatus.collect().size() != 2)
            client.receive();

        assertEquals(emptyList(), clientMessages.collect());
        assertEquals(asList(new EndOfSession(), new Downstream()), clientStatus.collect());
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

        while (clientStatus.collect().size() != 2)
            client.receive();

        assertEquals(asList(new Downstream(), new Downstream()), clientStatus.collect());

    }

    @Test
    public void maximumMessageLength() throws Exception {
        String message = repeat('X', 1398);

        packet.put(wrap(message));

        server.send(packet);

        while (clientMessages.collect().size() != 1)
            client.receive();

        assertEquals(asList(message), clientMessages.collect());
        assertEquals(asList(new Downstream()), clientStatus.collect());
    }

    @Test
    public void maximumMessageLengthExceeded() throws Exception {
        exception.expect(MoldUDP64Exception.class);

        packet.put(wrap(repeat('X', 1399)));
    }

}
