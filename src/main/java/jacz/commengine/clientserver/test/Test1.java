package jacz.commengine.clientserver.test;

import jacz.util.concurrency.task_executor.ThreadExecutor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Test class: 1 server 2 clients, basic communication
 */
public class Test1 {

    public static void main(String args[]) throws ExecutionException, InterruptedException {

        ThreadExecutor.registerClient(Test1.class.getName());
        final int port = 50000;

        Server1 s = new Server1(port);
        Future future1 = ThreadExecutor.submit(s);
        Future future2 = ThreadExecutor.submit(new Client1A(port));
        Future future3 = ThreadExecutor.submit(new Client1B(port));

        future1.get();
        future2.get();
        future3.get();



        //IOUtil.pauseEnter();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        s.stop();

        System.out.println("FINN");
        ThreadExecutor.shutdownClient(Test1.class.getName());
    }
}
