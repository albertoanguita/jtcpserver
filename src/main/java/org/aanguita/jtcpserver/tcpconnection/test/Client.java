package org.aanguita.jtcpserver.tcpconnection.test;

import org.aanguita.jtcpserver.tcpconnection.client.TCPClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 02-mar-2010
 * Time: 17:46:06
 * To change this template use File | Settings | File Templates.
 */
public class Client implements Runnable {

    private String ip;

    private int port;

    Socket socket;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        try {
            socket = TCPClient.connect(ip, port);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("Client connected");
            try {
                StrMsg strMsg = (StrMsg) ois.readObject();
                System.out.println("leido: " + strMsg.str);
                byte[] data = new byte[1];
                ois.read(data, 0, 1);
                System.out.println("leido: " + Arrays.toString(data));
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
