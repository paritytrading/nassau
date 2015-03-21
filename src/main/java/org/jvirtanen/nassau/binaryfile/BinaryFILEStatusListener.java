package org.jvirtanen.nassau.binaryfile;

import java.io.IOException;

/**
 * The interface for status events.
 */
public interface BinaryFILEStatusListener {

    /**
     * Read a payload with length of zero indicating the End of Session.
     *
     * @throws IOException if an I/O error occurs
     */
    void endOfSession() throws IOException;

}
