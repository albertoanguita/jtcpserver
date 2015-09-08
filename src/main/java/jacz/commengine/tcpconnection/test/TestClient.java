package jacz.commengine.tcpconnection.test;

import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;
import jacz.util.io.IOUtil;

import java.io.IOException;

/**
 *
 */
public class TestClient {


    public static void main(String args[]) {

        final int port = 50000;

        Client client = new Client("127.0.0.1", port);

        TaskFinalizationIndicator tfi2 = ParallelTaskExecutor.executeTask(client);

        tfi2.waitForFinalization();
        IOUtil.pauseEnter("press enter");
        try {
            client.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("FIN");

    }
}
