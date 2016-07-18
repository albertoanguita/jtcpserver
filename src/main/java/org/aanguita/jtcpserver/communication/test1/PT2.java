package org.aanguita.jtcpserver.communication.test1;


/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 16-dic-2008<br>
 * Last Modified: 16-dic-2008
 */
public class PT2 implements Runnable {

    private int port;

    public PT2(int port) {
        this.port = port;
    }

    public void run() {
        Test1 t2 = new Test1("t2");

        t2.connect(port);

//        t2.read();
        t2.write("hola t1");
//        t2.write((byte) 1, (byte) 2, (byte) 3);
//        byte[] ba = new byte[25];
//        for (int i = 0; i < ba.length; i++) {
//            ba[i] = 5;
//        }
//        t2.write(ba);
////        t2.write(new NotSerClass());
//        byte[] ba2 = new byte[700000];
//        for (int i = 0; i < ba2.length; i++) {
//            ba2[i] = 1;
//        }
//        t2.write(ba2);
//        t2.write(79000);
//        t2.write(new HashMap<String, String>());
//        t2.write("hierhigoegrzsodirjgodijgdipojxhgdoihj");
//        t2.write((byte) 13);
        //t2.disconnect();

        /*try {
            //String str = FileReaderWriter.readTextFile(".\\trunk\\src\\nivel_comunicacion\\examples\\test1\\largetext.txt");
            //t2.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //t2.disconnect();

    }
}
