package jacz.commengine.communication.test1;

import jacz.util.concurrency.task_executor.ParallelTask;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-dic-2008<br>
 * Last Modified: 16-dic-2008
 */
public class PT1 implements ParallelTask {

    private int port;

    public PT1(int port) {
        this.port = port;
    }

    public void performTask() {
        Test1 t1 = new Test1("t1");

        t1.openServer(port);
        //t1.write("hola t2");
        //t1.disconnect();
        //t1.read();
        //t1.read();
        //t1.read();
        t1.read();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("FIN");
        //t1.read();
        t1.disconnect();

        //ioUtil.pauseEnter();


    }
}
