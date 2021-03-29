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

import com.paritytrading.nassau.Value;
import java.util.ArrayList;
import java.util.List;

class BinaryFILEStatus implements BinaryFILEStatusListener {

    private List<Event> events;

    public BinaryFILEStatus() {
        this.events = new ArrayList<>();
    }

    public List<Event> collect() {
        return events;
    }

    @Override
    public void endOfSession() {
        events.add(new EndOfSession());
    }

    public interface Event {
    }

    public static class EndOfSession extends Value implements Event {
    }

}
