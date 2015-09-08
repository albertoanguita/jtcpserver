package jacz.commengine.tcpconnection.test;

import jacz.commengine.tcpconnection.server.TCPServerAction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 
 */
public class ServerAction implements TCPServerAction {

    public void processNewConnection(Socket clientSocket) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.writeObject(new StrMsg("joder"));
            byte[] data = new byte[1];
            data[0] = 5;
            //data[1] = 6;
            oos.write(data);
            oos.flush();
            System.out.println("5 escrito");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void error(Exception e) {
        System.out.println("Error en el tcp server");
        e.printStackTrace();
    }
}
