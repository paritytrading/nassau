package org.jvirtanen.nassau.moldudp64;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

class BackgroundRequestServer {

    private static final int SLEEP_MILLIS = 50;

    private MoldUDP64RequestServer server;

    private MoldUDP64MessageStore store;

    private volatile boolean stopped;

    private CountDownLatch stop;

    BackgroundRequestServer(MoldUDP64RequestServer server,
            MoldUDP64MessageStore store) {
        this.server = server;
        this.store  = store;

        this.stopped = false;
        this.stop    = new CountDownLatch(1);
    }

    void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopped) {
                    try {
                        server.serve(store);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        Thread.sleep(SLEEP_MILLIS);
                    } catch (InterruptedException e) {
                    }
                }

                stop.countDown();
            }

        }).start();
    }

    void stop() {
        stopped = true;

        try {
            stop.await();
        } catch (InterruptedException e) {
        }
    }

}
