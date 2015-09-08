package jacz.commengine.clientserver.test;

import jacz.util.concurrency.task_executor.ParallelTaskExecutor;
import jacz.util.concurrency.task_executor.TaskFinalizationIndicator;


/**
 * Test class: 1 server 2 clients, basic communication
 */
public class Test1 {

    public static void main(String args[]) {

        final int port = 50000;

        Server1 s = new Server1(port);
        TaskFinalizationIndicator tfi1 = ParallelTaskExecutor.executeTask(s);
        TaskFinalizationIndicator tfi2 = ParallelTaskExecutor.executeTask(new Client1A(port));
        TaskFinalizationIndicator tfi3 = ParallelTaskExecutor.executeTask(new Client1B(port));

        tfi1.waitForFinalization();
        tfi2.waitForFinalization();
        tfi3.waitForFinalization();



        //IOUtil.pauseEnter();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        s.stop();

        System.out.println("FINN");
    }
}
