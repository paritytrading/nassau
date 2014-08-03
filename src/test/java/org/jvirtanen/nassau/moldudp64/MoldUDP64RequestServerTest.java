package org.jvirtanen.nassau.moldudp64;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;
import static org.jvirtanen.nassau.moldudp64.MoldUDP64ClientStatus.*;
import static org.jvirtanen.nassau.util.Strings.*;

import java.nio.channels.DatagramChannel;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.jvirtanen.nassau.Messages;
import org.jvirtanen.nassau.util.Strings;

public class MoldUDP64RequestServerTest {

    @Rule
    public Timeout timeout = new Timeout(1000);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MoldUDP64DownstreamPacket packet;

    private Messages<String> clientMessages;

    private MoldUDP64ClientStatus clientStatus;

    private MoldUDP64Client client;
    private MoldUDP64Server server;

    private MoldUDP64RequestServer requestServer;

    private MoldUDP64DefaultMessageStore store;

    private BackgroundRequestServer backgroundRequestServer;

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

        store = new MoldUDP64DefaultMessageStore();

        backgroundRequestServer = new BackgroundRequestServer(requestServer, store);

        backgroundRequestServer.start();
    }

    @After
    public void tearDown() throws Exception {
        backgroundRequestServer.stop();

        requestServer.close();

        client.close();
        server.close();
    }

    @Test
    public void backfill() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        for (String message : messages)
            store.put(wrap(message));

        packet.put(wrap("quux"));

        server.nextSequenceNumber = 4;
        server.send(packet);

        while (clientMessages.collect().size() != 4)
            client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new Request(1, 4), new Downstream()), clientStatus.collect());
    }

    @Test
    public void gapFill() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        for (String message : messages)
            store.put(wrap(message));

        packet.clear();
        packet.put(wrap("foo"));

        server.nextSequenceNumber = 1;
        server.send(packet);

        packet.clear();
        packet.put(wrap("quux"));

        server.nextSequenceNumber = 4;
        server.send(packet);

        while (clientMessages.collect().size() != 4)
            client.receive();

        assertEquals(messages, clientMessages.collect());
        assertEquals(asList(new Downstream(), new Request(2, 3), new Downstream()),
                clientStatus.collect());
    }

}
