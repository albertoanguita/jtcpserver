package org.aanguita.jtcpserver.communication.test1;

import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Alberto on 16/10/2015.
 */
public class TestDisconnect {

    public static class ServerTask implements Runnable {

        @Override
        public void run() {
            try {
                System.out.println("SERVER: starting...");
                ServerSocket serverSocket = new ServerSocket(45000);
                Socket clientSocket = serverSocket.accept();
                System.out.println("SERVER: client connected");
                ThreadUtil.safeSleep(5000);
                System.out.println("SERVER: disconnecting client...");
                clientSocket.close();
                System.out.println("SERVER: end");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws Exception {

        ThreadExecutor.registerClient(TestDisconnect.class.getName());
        ThreadExecutor.submit(new ServerTask());


        ThreadUtil.safeSleep(1000);
        System.out.println("CLIENT: starting...");

        Socket socket = new Socket("127.0.0.1", 45000);
        System.out.println("CLIENT: connected");
        InputStream ois = socket.getInputStream();
        System.out.println("CLIENT: ois retrieved. Reading data...");
        try {
            byte[] data = new byte[2];
            int length = ois.read(data);
            if (length == -1) {
                System.out.println("dissssssssssssssssssssss");
            }
            System.out.println("CLIENT: read length " + length);
            System.out.println(Arrays.toString(data));
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("CLIENT: end");
        ThreadExecutor.shutdownClient(TestDisconnect.class.getName());
    }
}
