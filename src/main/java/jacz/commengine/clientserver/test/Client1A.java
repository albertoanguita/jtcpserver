package jacz.commengine.clientserver.test;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.clientserver.client.ClientModule;
import org.aanguita.jacuzzi.network.IP4Port;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 10-ene-2009<br>
 * Last Modified: 10-ene-2009
 */
class Client1A implements Runnable {

    private int port;

    Client1A(int port) {
        this.port = port;
    }

    public void run() {

        ClientModule clientModule;
        ChannelActionClient1 channelActionClient1 = new ChannelActionClient1("A");
        Set<Byte> allChannels = new HashSet<>();
        for (Byte channel = Byte.MIN_VALUE; channel < Byte.MAX_VALUE; channel++) {
            allChannels.add(channel);
        }
        allChannels.add(Byte.MAX_VALUE);
        Set<Set<Byte>> concurrentChannels = new HashSet<>();
        concurrentChannels.add(allChannels);
        clientModule = new ClientModule(new IP4Port("127.0.0.1", port), channelActionClient1, concurrentChannels);

        ChannelConnectionPoint cpp = null;
        try {
            cpp = clientModule.connect();
            clientModule.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        cpp.write((byte) 5, "hola servidor, soy A");
        //cpp.write((byte) 23, "hola de nuevo server, soy A");
    }
}
