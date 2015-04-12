package org.jvirtanen.nassau.moldudp64;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import org.jvirtanen.nassau.MessageListener;

abstract class MoldUDP64TestClientFactory {

    public abstract MoldUDP64TestClient create(DatagramChannel clientChannel,
            DatagramChannel serverRequestChannel, MessageListener listener,
            MoldUDP64ClientStatusListener statusListener) throws IOException;

    public static class SingleChannel extends MoldUDP64TestClientFactory {

        @Override
        public MoldUDP64TestClient create(DatagramChannel clientChannel,
                DatagramChannel serverRequestChannel, MessageListener listener,
                MoldUDP64ClientStatusListener statusListener) throws IOException {
            final SingleChannelMoldUDP64Client client = new SingleChannelMoldUDP64Client(
                    clientChannel, serverRequestChannel.getLocalAddress(), listener,
                    statusListener);

            return new MoldUDP64TestClient() {

                @Override
                public void receive() throws IOException {
                    client.receive();
                }

                @Override
                public void receiveResponse() throws IOException {
                    client.receive();
                }

                @Override
                public void close() throws IOException {
                    client.close();
                }

            };
        }

    }

}
