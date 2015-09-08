package jacz.commengine.channel.tests.test2;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.channel.ChannelModule;
import jacz.commengine.channel.tests.test1.AccionCanal1;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 10-mar-2010
 * Time: 15:51:35
 * To change this template use File | Settings | File Templates.
 */
public class Test2 {


    private String name;

    private ChannelModule channelModule;

    private ChannelConnectionPoint channelConnectionPoint;

    public Test2(String name) {
        this.name = name;
    }

    public void openServer(int port) {
        // open a server and wait for a client to start
        try {
            ServerSocket serverSocket = new ServerSocket(port);
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
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

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

        channelModule = new ChannelModule(socket, new AccionCanal1(name), channels);
        channelConnectionPoint = channelModule.getChannelConnectionPoint();
        channelModule.start();
    }

    public void write(byte canal, String message) {
        System.out.println(name + " writes '" + message + "' through jacz.commengine.channel " + canal);
        channelConnectionPoint.write(canal, message);
    }

    public void write(byte canal, byte[] data) {
        //System.out.println(name + " writes '" + message + "' through jacz.commengine.channel " + canal);
        channelConnectionPoint.write(canal, data);
    }

    /*public void registerFSM(GenericFSM<?, Object> fsm, Set<Byte> involvedChannels, Object... receivedMessages) {
        try {
            channelConnectionPoint.registerGenericFSM(fsm, involvedChannels, receivedMessages);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }*/

    /*public void read() {
        String s = ((StringMessage) moduloComunicacion.read()).getS();
        System.out.println(name + " reads '" + s + "'");
    }*/


    public static void main(String args[]) {

        final int port = 50000;


        TaskFinalizationIndicator tfi1 = ParallelTaskExecutor.executeTask(new Server(port));
        TaskFinalizationIndicator tfi2 = ParallelTaskExecutor.executeTask(new Client(port));

        tfi1.waitForFinalization();
        tfi2.waitForFinalization();

        System.out.println("FIN");

    }


}
