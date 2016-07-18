package org.aanguita.jtcpserver.tcpconnection.test;

import org.aanguita.jacuzzi.concurrency.task_executor.ThreadExecutor;
import org.aanguita.jacuzzi.io.IOUtil;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 *
 */
public class TestClient {


    public static void main(String args[]) {

        final int port = 50000;

        Client client = new Client("127.0.0.1", port);

        ThreadExecutor.registerClient(TestClient.class.getName());
        Future future = ThreadExecutor.submit(client);

        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        IOUtil.pauseEnter("press enter");
        try {
            client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("FIN");
        ThreadExecutor.shutdownClient(TestClient.class.getName());

    }
}
