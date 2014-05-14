package org.jvirtanen.nassau;

import java.io.IOException;

public class ProtocolException extends IOException {

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable reason) {
        super(message, reason);
    }

    public ProtocolException(Throwable reason) {
        super(reason);
    }

}
