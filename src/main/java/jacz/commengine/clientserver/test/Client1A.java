package jacz.commengine.clientserver.test;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.clientserver.client.ClientModule;
import jacz.util.concurrency.task_executor.ParallelTask;
import jacz.util.network.IP4Port;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 10-ene-2009<br>
 * Last Modified: 10-ene-2009
 */
class Client1A implements ParallelTask {

    private int port;

    Client1A(int port) {
        this.port = port;
    }

    public void performTask() {

        ClientModule clientModule;
        ChannelActionClient1 channelActionClient1 = new ChannelActionClient1("A");
        clientModule = new ClientModule(new IP4Port("127.0.0.1", port), channelActionClient1, null);

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
