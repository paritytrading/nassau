package com.paritytrading.nassau.util;

import com.paritytrading.nassau.MessageListener;
import com.paritytrading.nassau.binaryfile.BinaryFILEReader;
import com.paritytrading.nassau.binaryfile.BinaryFILEStatusListener;
import com.paritytrading.nassau.binaryfile.BinaryFILEStatusParser;
import java.io.File;
import java.io.IOException;

/**
 * Utility methods for working with the NASDAQ BinaryFILE 1.00 file format.
 */
public class BinaryFILE {

    private BinaryFILE() {
    }

    /**
     * Read messages. Invoke the message listener on each message. Continue
     * until either a payload with length of zero indicating the End of Session
     * is encountered or the end-of-file is reached.
     *
     * @param file a file
     * @param listener a message listener
     * @throws IOException if an I/O error occurs
     */
    public static void read(File file, MessageListener listener) throws IOException {
        StatusListener statusListener = new StatusListener();

        BinaryFILEStatusParser statusParser = new BinaryFILEStatusParser(listener, statusListener);

        try (BinaryFILEReader reader = BinaryFILEReader.open(file, statusParser)) {
            while (statusListener.receive && reader.read() >= 0);
        }
    }

    private static class StatusListener implements BinaryFILEStatusListener {

        boolean receive = true;

        @Override
        public void endOfSession() {
            receive = false;
        }

    }

}
