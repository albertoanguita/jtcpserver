package jacz.commengine.tcpconnection.test;

import jacz.commengine.tcpconnection.server.TCPServer;
import jacz.util.concurrency.task_executor.ParallelTask;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 02-mar-2010
 * Time: 17:46:01
 * To change this template use File | Settings | File Templates.
 */
public class Server implements ParallelTask {

    private int port;

    TCPServer tcpServer;

    public Server(int port) {
        this.port = port;
    }

    public void performTask() {
        //TCPServer tcpServer = new TCPServer(port, new ServerAction());
        tcpServer = new TCPServer(port, new ServerAction());
        tcpServer.startServer();
        System.out.println("Server started");
    }

    public void stop() {
        tcpServer.stopServer();
    }
}
