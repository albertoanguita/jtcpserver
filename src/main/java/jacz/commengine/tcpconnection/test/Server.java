package jacz.commengine.tcpconnection.test;

import jacz.commengine.tcpconnection.server.TCPServer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 02-mar-2010
 * Time: 17:46:01
 * To change this template use File | Settings | File Templates.
 */
public class Server implements Runnable {

    private int port;

    TCPServer tcpServer;

    public Server(int port) {
        this.port = port;
    }

    public void run() {
        //TCPServer tcpServer = new TCPServer(port, new ServerAction());
        tcpServer = new TCPServer(port, new ServerAction());
        try {
            tcpServer.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server started");
    }

    public void stop() {
        tcpServer.stopServer();
    }
}
