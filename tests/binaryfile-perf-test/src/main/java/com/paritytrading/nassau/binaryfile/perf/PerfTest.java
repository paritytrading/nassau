/*
 * Copyright 2014 Nassau authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.paritytrading.nassau.binaryfile.perf;

import com.paritytrading.nassau.binaryfile.BinaryFILEReader;
import com.paritytrading.nassau.binaryfile.BinaryFILEStatusListener;
import com.paritytrading.nassau.binaryfile.BinaryFILEStatusParser;
import java.io.File;
import java.io.IOException;

class PerfTest {

    public static void main(String[] args) throws IOException {
        if (args.length != 1)
            usage();

        String inputFilename = args[0];

        MessageCounter counter = new MessageCounter();

        BinaryFILEStatusListener statusListener = new BinaryFILEStatusListener() {

            @Override
            public void endOfSession() {
            }

        };

        BinaryFILEStatusParser parser = new BinaryFILEStatusParser(counter, statusListener);

        try (BinaryFILEReader reader = BinaryFILEReader.open(new File(inputFilename), parser)) {
            long started = System.nanoTime();

            while (reader.read() >= 0);

            long finished = System.nanoTime();

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

    private static void usage() {
        System.err.println("Usage: nassau-binaryfile-perf-test <input-file>");
        System.exit(2);
    }

}
