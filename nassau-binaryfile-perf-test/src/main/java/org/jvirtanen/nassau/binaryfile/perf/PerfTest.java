package org.jvirtanen.nassau.binaryfile.perf;

import static org.jvirtanen.util.Applications.*;

import java.io.File;
import java.io.IOException;
import org.jvirtanen.nassau.binaryfile.BinaryFILEReader;
import org.jvirtanen.nassau.binaryfile.BinaryFILEStatusListener;
import org.jvirtanen.nassau.binaryfile.BinaryFILEStatusParser;

class PerfTest {

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            usage("nassau-binaryfile-perf-test <input-file>");

        String inputFilename = args[0];

        MessageCounter counter = new MessageCounter();

        BinaryFILEStatusListener statusListener = new BinaryFILEStatusListener() {

            @Override
            public void endOfSession() {
            }

        };

        BinaryFILEStatusParser parser = new BinaryFILEStatusParser(counter, statusListener);

        BinaryFILEReader reader = BinaryFILEReader.open(new File(inputFilename), parser);

        long started = System.nanoTime();

        while (reader.read());

        long finished = System.nanoTime();

        reader.close();

        double seconds  = (finished - started) / (1000.0 * 1000 * 1000);
        long   messages = counter.getMessageCount();

        System.out.printf("Results:\n");
        System.out.printf("\n");
        System.out.printf("    Messages: %10d\n", messages);
        System.out.printf("        Time: %13.2f s\n", seconds);
        System.out.printf("  Throughput: %13.2f messages/s\n", messages / seconds);
        System.out.printf("\n");
    }

}
