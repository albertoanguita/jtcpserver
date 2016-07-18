package org.aanguita.jtcpserver.channel.tests.test1;

import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.channel.ChannelModule;
import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 23-dic-2008<br>
 * Last Modified: 23-dic-2008
 */
public class Test1 {

    ServerSocket serverSocket;

    private String name;

    private ChannelModule channelModule;

    private ChannelConnectionPoint channelConnectionPoint;

    public Test1(String name) {
        this.name = name;
    }

    public void openServer(int port) {
        // open a server and wait for a client to start
        try {
            serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            initModuloCanal(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void connect(int port) {
        // start to a server
        try {
            Socket socket = new Socket("127.0.0.1", port);
            initModuloCanal(socket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initModuloCanal(Socket socket) throws IOException {

        //Set<Set<Byte>> channels = null;
        Set<Set<Byte>> channels = new HashSet<Set<Byte>>();
        Set<Byte> set1 = new HashSet<Byte>();
        set1.add((byte) 1);
        set1.add((byte) 2);
        set1.add((byte) 3);
        set1.add((byte) 4);
        Set<Byte> set2 = new HashSet<Byte>();
        set2.add((byte) 5);
        set2.add((byte) 120);
        channels.add(set1);
        channels.add(set2);

        channelModule = new ChannelModule(socket, new AccionCanal1(name), null);
        channelConnectionPoint = channelModule.getChannelConnectionPoint();
        channelModule.start();
    }

    public void write(byte canal, Serializable message) {
        System.out.println(name + " writes '" + message + "' through channel " + canal);
        channelConnectionPoint.write(canal, message);
    }

    public void write(byte canal, byte[] data) {
        //System.out.println(name + " writes '" + message + "' through channel " + canal);
        channelConnectionPoint.write(canal, data);
    }

    public void disconnect() {
        channelConnectionPoint.disconnect();
    }

    public void disconnectServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void read() {
        String s = ((StringMessage) moduloComunicacion.read()).getS();
        System.out.println(name + " reads '" + s + "'");
    }*/


    public static void main(String args[]) throws ExecutionException, InterruptedException {

        ThreadExecutor.registerClient(Test1.class.getName());
        final int port = 50000;


        Future future1 = ThreadExecutor.submit(new PT1(port));
        Future future2 = ThreadExecutor.submit(new PT2(port));

        future1.get();
        future2.get();

        System.out.println("FIN");
        ThreadExecutor.shutdownClient(Test1.class.getName());
    }


}
