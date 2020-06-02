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

/**
 * An implementation of the NASDAQ MoldUDP64 1.00 protocol.
 *
 * <p>The implementation is based on the Java NIO API.</p>
 *
 * <p>The underlying socket channels can be either blocking or non-blocking.
 * In both cases, data transmission always blocks.</p>
 */
package com.paritytrading.nassau.moldudp64;
