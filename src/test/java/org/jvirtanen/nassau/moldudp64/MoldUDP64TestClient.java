package org.jvirtanen.nassau.moldudp64;

import java.io.Closeable;
import java.io.IOException;

interface MoldUDP64TestClient extends Closeable {

    void receive() throws IOException;

    void receiveResponse() throws IOException;

}
