package jacz.commengine.clientserver.test;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.clientserver.client.ClientModule;
import jacz.util.concurrency.task_executor.ParallelTask;
import jacz.util.network.IP4Port;

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
class Client1B implements ParallelTask {

    private int port;

    Client1B(int port) {
        this.port = port;
    }

    public void performTask() {

        ClientModule clientModule;
        ChannelActionClient1 channelActionClient1 = new ChannelActionClient1("B");
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

        cpp.write((byte) 80, "hola servidor, soy B");
        cpp.write((byte) 90, "hola de nuevo servidor, soy B");
    }
}
