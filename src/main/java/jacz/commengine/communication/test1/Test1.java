package jacz.commengine.communication.test1;

import jacz.commengine.communication.ByteArrayWrapper;
import jacz.commengine.communication.CommunicationModule;
import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;
import org.aanguita.jacuzzi.date_time.TimeElapsed;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 15-dic-2008<br>
 * Last Modified: 15-dic-2008
 */
public class Test1 {

    private String name;

    private CommunicationModule communicationModule;

    public Test1(String name) {
        this.name = name;
    }

    public void openServer(int port) {
        // open a server and wait for a client to connect
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            initModuloComunicacion("Server", clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    public void connect(int port) {
        // connect to a server
        try {
            Socket socket = new Socket("127.0.0.1", port);
            //Socket socket = new Socket("138.100.11.51", port);
            initModuloComunicacion("Client", socket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void disconnect() {
        communicationModule.disconnect();
    }

    private void initModuloComunicacion(String name, Socket socket) throws IOException {
        communicationModule = new CommunicationModule(socket);
    }

    public void write(Serializable message) {
        System.out.println(name + " writes '" + message + "'");
        TimeElapsed timeElapsed = new TimeElapsed();
        communicationModule.write(message);
        //communicationModule.write(new Integer(3));
        //byte[] data = new byte[1];
        //data[0] = 5;
        //communicationModule.write(data);
        System.out.println(timeElapsed.measureTime());
    }

    public void write(byte... data) {
        System.out.println(name + " writes a byte array");
        TimeElapsed timeElapsed = new TimeElapsed();
        communicationModule.write(data);
        System.out.println(timeElapsed.measureTime());
    }

    public void read() {
        //byte[] data = ((ByteArrayWrapper) communicationModule.read()).getData();
        //System.out.println(Arrays.toString(data));
        try {
            Object o = communicationModule.read();
            if (o instanceof ByteArrayWrapper) {
                ByteArrayWrapper byteArrayWrapper = (ByteArrayWrapper) o;
                System.out.println(name + " reads array: " + Arrays.toString(byteArrayWrapper.getData()));
                System.out.println(name + " array has size: " + byteArrayWrapper.getData().length);
            } else {
                System.out.println(name + " reads '" + o.toString() + "'");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) throws ExecutionException, InterruptedException {

        final int port = 50000;


        ThreadExecutor.registerClient(Test1.class.getName());
        Future future1 = ThreadExecutor.submit(new PT1(port));
        Future future2 = ThreadExecutor.submit(new PT2(port));

        future1.get();
        future2.get();

        System.out.println("FIN main");
        ThreadExecutor.shutdownClient(Test1.class.getName());

//        System.exit(0);
    }


}
