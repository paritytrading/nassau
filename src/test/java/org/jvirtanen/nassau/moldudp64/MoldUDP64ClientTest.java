package org.jvirtanen.nassau.moldudp64;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64ClientState.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64ClientStatus.*;
import static org.jvirtanen.nassau.util.Strings.*;

import java.nio.channels.DatagramChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.jvirtanen.nassau.Messages;
import org.jvirtanen.nassau.util.Strings;

public class MoldUDP64ClientTest {

    @Rule
    public Timeout timeout = new Timeout(1000);

    private MoldUDP64DownstreamPacket packet;

    private Messages<String> clientMessages;

    private MoldUDP64ClientStatus clientStatus;

    private MoldUDP64Client client;
    private MoldUDP64Server server;

    private MoldUDP64RequestServer requestServer;

    @Before
    public void setUp() throws Exception {
        DatagramChannel clientChannel  = DatagramChannels.openClientChannel();
        DatagramChannel serverChannel  = DatagramChannels.openServerChannel(clientChannel);
        DatagramChannel requestChannel = DatagramChannels.openRequestChannel();

        packet = new MoldUDP64DownstreamPacket();

        clientMessages = new Messages<>(Strings.MESSAGE_PARSER);

        clientStatus = new MoldUDP64ClientStatus();

        client = new MoldUDP64Client(clientChannel, requestChannel.getLocalAddress(), clientMessages, clientStatus);
        server = new MoldUDP64Server(serverChannel, "nassau");

        requestServer = new MoldUDP64RequestServer(requestChannel);
    }

    @After
    public void tearDown() throws Exception {
        requestServer.close();

        server.close();
        client.close();
    }

    @Test
    public void concurrentGapFill() throws Exception {
        packet.clear();
        packet.put(wrap("bar"));

        server.nextSequenceNumber = 2;
        server.send(packet);

        packet.clear();
        packet.put(wrap("baz"));

        server.nextSequenceNumber = 3;
        server.send(packet);

        while (clientStatus.collect().size() != 3)
            client.receive();

        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        packet.clear();
        packet.put(wrap("foo"));
        packet.put(wrap("bar"));
        packet.put(wrap("baz"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        packet.clear();
        packet.put(wrap("quux"));

        server.nextSequenceNumber = 4;
        server.send(packet);

        while (clientMessages.collect().size() != 4)
            client.receive();

        assertEquals(asList("foo", "bar", "baz", "quux"), clientMessages.collect());
        assertEquals(asList(new Transition(BACKFILL), new Request(1, 2), new Request(1, 3),
                    new Transition(SYNCHRONIZED), new Downstream(), new Downstream(),
                    new Downstream()), clientStatus.collect());
    }

}
