package org.jvirtanen.nassau.soupbintcp;

import static org.jvirtanen.nassau.soupbintcp.Packets.*;
import static org.jvirtanen.nassau.soupbintcp.SoupBinTCP.*;
import static org.jvirtanen.nio.ByteBuffers.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class PacketParser {

    private LoginAccepted loginAccepted;
    private LoginRejected loginRejected;
    private LoginRequest  loginRequest;

    private SoupBinTCPSession session;

    PacketParser(SoupBinTCPSession session) {
        this.loginAccepted = new LoginAccepted();
        this.loginRejected = new LoginRejected();
        this.loginRequest  = new LoginRequest();

        this.session = session;
    }

    boolean parse(ByteBuffer buffer) throws IOException {
        if (buffer.remaining() < 2)
            return false;

        buffer.mark();

        buffer.order(ByteOrder.BIG_ENDIAN);

        int packetLength = getUnsignedShort(buffer);
        if (packetLength > buffer.capacity() - 2)
            throw new SoupBinTCPException("Packet length exceeds buffer capacity");

        if (buffer.remaining() < packetLength) {
            buffer.reset();
            return false;
        }

        byte packetType = buffer.get();

        int limit = buffer.limit();

        buffer.limit(buffer.position() + packetLength - 1);

        parse(packetType, buffer);

        buffer.position(buffer.limit());
        buffer.limit(limit);

        return true;
    }

    private void parse(byte packetType, ByteBuffer buffer) throws IOException {
        switch (packetType) {
        case PACKET_TYPE_DEBUG:
            session.debug(buffer);
            break;
        case PACKET_TYPE_LOGIN_ACCEPTED:
            loginAccepted(buffer);
            break;
        case PACKET_TYPE_LOGIN_REJECTED:
            loginRejected(buffer);
            break;
        case PACKET_TYPE_SEQUENCED_DATA:
            session.sequencedData(buffer);
            break;
        case PACKET_TYPE_SERVER_HEARTBEAT:
            session.serverHeartbeat();
            break;
        case PACKET_TYPE_END_OF_SESSION:
            session.endOfSession();
            break;
        case PACKET_TYPE_LOGIN_REQUEST:
            loginRequest(buffer);
            break;
        case PACKET_TYPE_UNSEQUENCED_DATA:
            session.unsequencedData(buffer);
            break;
        case PACKET_TYPE_CLIENT_HEARTBEAT:
            session.clientHeartbeat();
            break;
        case PACKET_TYPE_LOGOUT_REQUEST:
            session.logoutRequest();
            break;
        default:
            throw new SoupBinTCPException("Unknown packet type: " + (char)packetType);
        }
    }

    private void loginAccepted(ByteBuffer buffer) throws IOException {
        loginAccepted.get(buffer);
        session.loginAccepted(loginAccepted);
    }

    private void loginRejected(ByteBuffer buffer) throws IOException {
        loginRejected.get(buffer);
        session.loginRejected(loginRejected);
    }

    private void loginRequest(ByteBuffer buffer) throws IOException {
        loginRequest.get(buffer);
        session.loginRequest(loginRequest);
    }

}
