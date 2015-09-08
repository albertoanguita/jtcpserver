package jacz.commengine.tcpconnection.server;

import jacz.util.queues.event_processing.MessageHandler;

import java.io.IOException;
import java.net.Socket;

/**
 * This class implements a message handler for handling incoming client connections
 */
final class ConnectionHandler implements MessageHandler {

    /**
     * TCP server to which this connection handler belongs to
     */
    private TCPServer tcpServer;

    /**
     * Actions to invoke for each accepted connection
     */
    private TCPServerAction tcpServerAction;

    /**
     * Constructor of class
     *
     * @param tcpServer       TCP server to which this connection handler belongs to
     * @param tcpServerAction Actions to invoke for each accepted connection
     */
    public ConnectionHandler(TCPServer tcpServer, TCPServerAction tcpServerAction) {
        this.tcpServer = tcpServer;
        this.tcpServerAction = tcpServerAction;
    }

    @Override
    public void handleMessage(Object o) {
        if (o instanceof Socket) {
            Socket clientSocket = (Socket) o;
            if (tcpServer.isRunning()) {
                tcpServerAction.processNewConnection(clientSocket);
            } else {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        } else {
            tcpServerAction.error((Exception) o);
        }
    }

    @Override
    public void finalizeHandler() {
        // todo
    }
}
