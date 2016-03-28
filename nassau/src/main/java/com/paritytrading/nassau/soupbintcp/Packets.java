package com.paritytrading.nassau.soupbintcp;

abstract class Packets {

    static final byte PACKET_TYPE_DEBUG            = '+';
    static final byte PACKET_TYPE_LOGIN_ACCEPTED   = 'A';
    static final byte PACKET_TYPE_LOGIN_REJECTED   = 'J';
    static final byte PACKET_TYPE_SEQUENCED_DATA   = 'S';
    static final byte PACKET_TYPE_SERVER_HEARTBEAT = 'H';
    static final byte PACKET_TYPE_END_OF_SESSION   = 'Z';

    static final byte PACKET_TYPE_LOGIN_REQUEST    = 'L';
    static final byte PACKET_TYPE_UNSEQUENCED_DATA = 'U';
    static final byte PACKET_TYPE_CLIENT_HEARTBEAT = 'R';
    static final byte PACKET_TYPE_LOGOUT_REQUEST   = 'O';

    static final int MAX_PACKET_LENGTH = 65535;

    static void unexpectedPacketType(byte packetType) throws SoupBinTCPException {
        throw new SoupBinTCPException("Unexpected packet type: " + (char)packetType);
    }

}
