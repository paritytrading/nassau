/**
 * An implementation of the NASDAQ SoupBinTCP 3.00 protocol.
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
 * <p>Data reception can run on one thread and data transmission and session
 * keep-alive on another without locking. Data transmission and session
 * keep-alive can run on different threads but require locking.</p>
 *
 * <p>The underlying socket channels can be either blocking or non-blocking.
 * In both cases, data transmission always blocks.</p>
 */
package org.jvirtanen.nassau.soupbintcp;
