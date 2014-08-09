package org.jvirtanen.nassau;

import java.nio.ByteBuffer;

public interface MessageParser<M> {

    M parse(ByteBuffer buffer);

}
