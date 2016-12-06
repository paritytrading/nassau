package com.paritytrading.nassau.soupbintcp;

import org.jvirtanen.value.Value;

class SoupBinTCPSessionStatus {

    public static class HeartbeatTimeout extends Value
            implements SoupBinTCPClientStatus.Event, SoupBinTCPServerStatus.Event {
    }

}
