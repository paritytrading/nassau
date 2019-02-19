package com.paritytrading.nassau.util;

import static com.paritytrading.nassau.soupbintcp.SoupBinTCP.*;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPClient;
import com.paritytrading.nassau.soupbintcp.SoupBinTCPClientStatusListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Utility methods for working with the NASDAQ SoupBinTCP 3.00 protocol.
 */
public class SoupBinTCP {

    private static final int TIMEOUT_MILLIS = 1000;

    private SoupBinTCP() {
    }

    /**
     * Receive messages. Invoke the message listener on each message. Continue
     * until an End of Session packet is received or the end-of-stream is
     * reached.
     *
     * @param address the address
     * @param username the username
     * @param password the password
     * @param listener a message listener
     * @throws IOException if an I/O error occurs
     */
    public static void receive(InetSocketAddress address, String username,
            String password, MessageListener listener) throws IOException {
        SocketChannel channel = SocketChannel.open();

        channel.connect(address);
        channel.configureBlocking(false);

        StatusListener statusListener = new StatusListener();

        try (Selector selector = Selector.open();
                SoupBinTCPClient client = new SoupBinTCPClient(channel, listener, statusListener)) {
            channel.register(selector, SelectionKey.OP_READ);

            LoginRequest message = new LoginRequest();

            message.setUsername(username);
            message.setPassword(password);
            message.setRequestedSession("");
            message.setRequestedSequenceNumber(1);

            client.login(message);

            while (statusListener.receive) {
                int numKeys = selector.select(TIMEOUT_MILLIS);

                if (numKeys > 0) {
                    if (client.receive() < 0)
                        break;

                    selector.selectedKeys().clear();
                }

                client.keepAlive();
            }
        }
    }

    private static class StatusListener implements SoupBinTCPClientStatusListener {

        boolean receive = true;

        @Override
        public void heartbeatTimeout(SoupBinTCPClient session) throws IOException {
            throw new IOException("Heartbeat timeout");
        }

        @Override
        public void loginAccepted(SoupBinTCPClient session, LoginAccepted payload) {
        }

        @Override
        public void loginRejected(SoupBinTCPClient session, LoginRejected payload) throws IOException {
            throw new IOException("Login rejected");
        }

        @Override
        public void endOfSession(SoupBinTCPClient session) {
            receive = false;
        }

    }

}
