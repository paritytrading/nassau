package com.paritytrading.nassau;

import java.nio.ByteBuffer;

public interface MessageParser<M> {

    M parse(ByteBuffer buffer);

}
