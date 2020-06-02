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
package com.paritytrading.nassau.binaryfile;

import static com.paritytrading.nassau.Strings.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;

public class BinaryFILEWriterTest {

    @Test
    public void write() throws Exception {
        File file = File.createTempFile("binaryfile", ".dat");

        try (BinaryFILEWriter writer = BinaryFILEWriter.open(file)) {
            List<String> messages = asList("foo", "bar", "baz", "quux", "");

            for (String message : messages)
                writer.write(wrap(message));
        }

        byte[] writtenBytes  = Files.readAllBytes(file.toPath());
        byte[] expectedBytes = Files.readAllBytes(Paths.get(getClass().getResource("/binaryfile.dat").toURI()));

        assertArrayEquals(expectedBytes, writtenBytes);
    }

}
