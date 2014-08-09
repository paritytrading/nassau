/**
 * An implementation of the NASDAQ SoupBinTCP 3.0 protocol.
 *
 * <p>The implementation is based on the Java NIO API and consists of three
 * primary functions:</p>
 * <ul>
 * <li>data reception
 *   ({@link org.jvirtanen.nassau.soupbintcp.SoupBinTCPSession#receive})</li>
 * <li>data transmission</li>
 * <li>session keep-alive
 *   ({@link org.jvirtanen.nassau.soupbintcp.SoupBinTCPSession#keepAlive})</li>
 * </ul>
 *
 * <p>All three primary functions can run on different threads. Alternately,
 * two or all three of them can run on the same thread.</p>
 *
 * <p>The underlying socket channels can be either blocking or non-blocking.
 * In both cases, data transmission always blocks.</p>
 */
package org.jvirtanen.nassau.soupbintcp;
