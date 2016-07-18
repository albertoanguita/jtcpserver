package org.aanguita.jtcpserver.channel.tests.test2;

import org.aanguita.jtcpserver.channel.ChannelModule;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 11-mar-2010
 * Time: 12:57:51
 * To change this template use File | Settings | File Templates.
 */
public class Server implements Runnable {

    private int port;

    private final String version = "version 1";

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        // open a server and wait for a client to start
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

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

            ChannelModule channelModule = new ChannelModule(clientSocket, new AccionCanal2_Server(version), channels);
            //ChannelConnectionPoint channelConnectionPoint = channelModule.getChannelConnectionPoint();
            channelModule.start();

            /*Set<Byte> invChannels = new HashSet<Byte>();
            invChannels.add((byte) 0);
            channelConnectionPoint.registerGenericFSM(new FSMServer(version, name, bwth), invChannels);*/


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
