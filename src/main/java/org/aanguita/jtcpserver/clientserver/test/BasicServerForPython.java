package org.aanguita.jtcpserver.clientserver.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Alberto on 16/10/2015.
 */
public class BasicServerForPython {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket;
        serverSocket = new ServerSocket(45000);

        Socket socket = serverSocket.accept();

        OutputStream oos = socket.getOutputStream();
        InputStream ois = socket.getInputStream();

        byte[] data = new byte[1];
        ois.read(data);
        System.out.println("Data received: " + Arrays.toString(data));

        byte[] response = new byte[1];
        response[0] = (byte) (data[0] + (byte) 1);
        oos.write(response);
    }
}
