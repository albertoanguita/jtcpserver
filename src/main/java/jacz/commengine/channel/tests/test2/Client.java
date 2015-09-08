package jacz.commengine.channel.tests.test2;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.channel.ChannelModule;
import jacz.util.concurrency.task_executor.ParallelTask;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 11-mar-2010
 * Time: 12:57:57
 * To change this template use File | Settings | File Templates.
 */
public class Client implements ParallelTask {

    private int port;

    private final String version = "version 1";

    private final String name = "miNombre";

    private final Integer bwth = 3;

    public Client(int port) {
        this.port = port;
    }

    public void performTask() {
        // open a server and wait for a client to start
        try {
            Socket socket = new Socket("127.0.0.1", port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            Set<Set<Byte>> channels = null;
            /*Set<Set<Byte>> channels = new HashSet<Set<Byte>>();
            Set<Byte> set1 = new HashSet<Byte>();
            set1.add((byte) 0);
            set1.add((byte) 2);
            set1.add((byte) 3);
            set1.add((byte) 4);
            Set<Byte> set2 = new HashSet<Byte>();
            set2.add((byte) 5);
            set2.add((byte) 120);
            channels.add(set1);
            channels.add(set2);*/

            ChannelModule channelModule = new ChannelModule(socket, new AccionCanal2_Client(), channels);
            ChannelConnectionPoint channelConnectionPoint = channelModule.getChannelConnectionPoint();
            channelModule.start();

            channelConnectionPoint.registerGenericFSM(new FSMClient(version, name, bwth), (byte) 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
