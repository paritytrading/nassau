package org.jvirtanen.nassau.soupbintcp;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.jvirtanen.nassau.soupbintcp.SoupBinTCPClientStatus.*;
import static org.jvirtanen.nassau.soupbintcp.SoupBinTCPServerStatus.*;
import static org.jvirtanen.nassau.util.Strings.*;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.jvirtanen.nassau.Messages;
import org.jvirtanen.nassau.util.FixedClock;
import org.jvirtanen.nassau.util.Strings;

public class SoupBinTCPSessionTest {

    @Rule
    public Timeout timeout = new Timeout(1000);

    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    @Before
    public void setUp() throws Exception {
        loginAccepted = new SoupBinTCP.LoginAccepted();
        loginRejected = new SoupBinTCP.LoginRejected();
        loginRequest  = new SoupBinTCP.LoginRequest();

        clock = new FixedClock();

        ServerSocketChannel acceptor = ServerSocketChannel.open();
        acceptor.bind(null);

        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.connect(acceptor.getLocalAddress());

        SocketChannel serverChannel = acceptor.accept();
        acceptor.close();

        clientMessages = new Messages<>(Strings.MESSAGE_PARSER);
        serverMessages = new Messages<>(Strings.MESSAGE_PARSER);

        clientStatus = new SoupBinTCPClientStatus();
        serverStatus = new SoupBinTCPServerStatus();

        client = new SoupBinTCPClient(clock, clientChannel, clientMessages, clientStatus);
        server = new SoupBinTCPServer(clock, serverChannel, serverMessages, serverStatus);
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
    }

    @Test
    public void loginAccepted() throws Exception {
        loginAccepted.session        = "foo";
        loginAccepted.sequenceNumber = 123;

        server.accept(loginAccepted);

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(asList(new LoginAccepted("       foo", 123)),
                clientStatus.collect());
    }

    @Test
    public void loginRejected() throws Exception {
        loginRejected.rejectReasonCode = SoupBinTCP.LOGIN_REJECT_CODE_NOT_AUTHORIZED;

        server.reject(loginRejected);

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(asList(new LoginRejected(SoupBinTCP.LOGIN_REJECT_CODE_NOT_AUTHORIZED)),
                clientStatus.collect());
    }

    @Test
    public void endOfSession() throws Exception {
        server.endSession();

        while (clientStatus.collect().size() != 1)
            client.receive();

        assertEquals(asList(new EndOfSession()), clientStatus.collect());
    }

    @Test
    public void loginRequest() throws Exception {
        loginRequest.username                = "foo";
        loginRequest.password                = "bar";
        loginRequest.requestedSession        = "baz";
        loginRequest.requestedSequenceNumber = 123;

        client.login(loginRequest);

        while (serverStatus.collect().size() != 1)
            server.receive();

        assertEquals(asList(new LoginRequest("foo   ", "bar       ",
                        "       baz", 123)), serverStatus.collect());
    }

    @Test
    public void logoutRequest() throws Exception {
        client.logout();

        while (serverStatus.collect().size() != 1)
            server.receive();

        assertEquals(asList(new LogoutRequest()), serverStatus.collect());
    }

    @Test
    public void unsequencedData() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        for (String message : messages)
            client.send(wrap(message));

        while (serverMessages.collect().size() != messages.size())
            server.receive();

        assertEquals(messages, serverMessages.collect());
    }

    @Test
    public void maximumPacketLengthForUnsequencedData() throws Exception {
        String message = message(65534);

        client.send(wrap(message));

        while (serverMessages.collect().size() != 1)
            server.receive();

        assertEquals(asList(message), serverMessages.collect());
    }

    @Test
    public void maximumPacketLengthExceededForUnsequencedData() throws Exception {
        exception.expect(SoupBinTCPException.class);

        client.send(wrap(message(65535)));
    }

    @Test
    public void sequencedData() throws Exception {
        List<String> messages = asList("foo", "bar", "baz", "quux");

        for (String message : messages)
            server.send(wrap(message));

        while (clientMessages.collect().size() != messages.size())
            client.receive();

        assertEquals(messages, clientMessages.collect());
    }

    @Test
    public void maximumPacketLengthForSequencedData() throws Exception {
        String message = message(65534);

        server.send(wrap(message));

        while (clientMessages.collect().size() != 1)
            client.receive();

        assertEquals(asList(message), clientMessages.collect());
    }

    @Test
    public void maximumPacketLengthExceededForSequencedData() throws Exception {
        exception.expect(SoupBinTCPException.class);

        server.send(wrap(message(65535)));
    }

    @Test
    public void serverKeepAlive() throws Exception {
        clock.setCurrentTimeMillis(1500);

        client.keepAlive();
        server.keepAlive();

        server.receive();

        clock.setCurrentTimeMillis(15500);

        server.keepAlive();

        clock.setCurrentTimeMillis(16750);

        exception.expect(SoupBinTCPException.class);

        server.keepAlive();
    }

    @Test
    public void clientKeepAlive() throws Exception {
        clock.setCurrentTimeMillis(1500);

        client.keepAlive();
        server.keepAlive();

        client.receive();

        clock.setCurrentTimeMillis(15500);

        client.keepAlive();

        clock.setCurrentTimeMillis(16750);

        exception.expect(SoupBinTCPException.class);

        client.keepAlive();
    }

    private String message(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++)
            builder.append('A');

        return builder.toString();
    }

}
