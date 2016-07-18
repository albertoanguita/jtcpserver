package org.aanguita.jtcpserver.channel.tests.test1;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-dic-2008<br>
 * Last Modified: 16-dic-2008
 */
public class PT1 implements Runnable {

    private int port;

    public PT1(int port) {
        this.port = port;
    }

    public void run() {
        Test1 t1 = new Test1("t1");

        t1.openServer(port);
        /*t1.write((byte)2, "hola t2");
        t1.write((byte)3, "hola t2");
        t1.write((byte)5, "hola t2");
        t1.write((byte)15, "hola t2");
        byte[] data = new byte[2];
        data[0] = 5;
        data[1] = 121;
        t1.write((byte)120, data);
        t1.write((byte)2, new NotSerClass());*/
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        t1.disconnect();
        t1.disconnectServer();
    }
}
