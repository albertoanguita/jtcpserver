package jacz.commengine.clientserver.test;

import jacz.commengine.clientserver.server.LightServer;
import jacz.commengine.clientserver.server.LightServerActionObject;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;

/**
 * Opens a light server that prints the client address info (public and private) for 10 seconds
 *
 * http://en.wikipedia.org/wiki/Ephemeral_port
 */
public class BasicServer implements LightServerActionObject {


    public static void main(String[] args) throws IOException {
        int port = 64905;
        long seconds = 30;
        LightServer lightServer = new LightServer(port, new BasicServer(), true);
        System.out.println("Starting server at " + port);
        lightServer.start();

        ThreadUtil.safeSleep(seconds * 1000);
        lightServer.stop();

        System.out.println("Stopping server");
    }

    @Override
    public Serializable newClientRequest(Socket clientSocket, Object object) throws Exception {
        int clientPort = clientSocket.getPort();
        String clientIP = clientSocket.getInetAddress().getHostAddress();
        System.out.println("Public ip: " + clientIP + ":" + clientPort);
        String localIP = (String) object;
        System.out.println("Private ip: " + localIP);
        System.out.println("---");
        return true;
    }

    @Override
    public void TCPServerError(Exception e) {
        e.printStackTrace();
    }
}
