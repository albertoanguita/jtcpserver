package jacz.commengine.tcpconnection.test;

import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;
import jacz.util.io.IOUtil;

/**
 * test
 */
public class TestServer {


    public static void main(String args[]) {

        final int port = 50000;

        Server server = new Server(port);
        TaskFinalizationIndicator tfi1 = ParallelTaskExecutor.executeTask(server);

        tfi1.waitForFinalization();
        IOUtil.pauseEnter("press enter");
        server.stop();

        System.out.println("FIN");
    }
}
