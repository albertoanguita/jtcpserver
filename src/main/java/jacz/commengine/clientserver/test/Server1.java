package jacz.commengine.clientserver.test;

import jacz.commengine.clientserver.server.ServerModule;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 10-ene-2009<br>
 * Last Modified: 10-ene-2009
 */
class Server1 implements Runnable {

    private ServerModule serverModule;

    private int port;

    Server1(int port) {
        this.port = port;
    }

    public void run() {


        ServerActionImpl1 serverActionImpl1 = new ServerActionImpl1();
        Set<Byte> allChannels = new HashSet<>();
        for (Byte channel = Byte.MIN_VALUE; channel < Byte.MAX_VALUE; channel++) {
            allChannels.add(channel);
        }
        allChannels.add(Byte.MAX_VALUE);
        Set<Set<Byte>> concurrentChannels = new HashSet<>();
        concurrentChannels.add(allChannels);

        serverModule = new ServerModule(port, serverActionImpl1, concurrentChannels);
        serverActionImpl1.setServerModule(serverModule);

        try {
            serverModule.startListeningConnections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        serverModule.stopAndDisconnect();
        System.out.println("Server stopped");
    }
}
