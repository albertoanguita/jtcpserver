package jacz.commengine.tcpconnection.server;

import jacz.util.queues.event_processing.MessageReader;
import jacz.util.queues.event_processing.StopReadingMessages;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Implementation of the message reader interface for listening for connections from clients
 */
final class ConnectionReader implements MessageReader {

    /**
     * TCP server to which this connection reader belongs to
     */
    private TCPServer tcpServer;

    private boolean socketClosed;

    /**
     * Server socket used to listen to incoming connections from clients
     */
    private ServerSocket serverSocket;

    /**
     * Creates a new connection reader for handling incoming connections and starts running it
     *
     * @param tcpServer TCP server to which this connection reader belongs to. Only needed to report stop in case of non-recoverable error
     * @param port      listening port for incoming connections
     * @throws java.io.IOException when there are problems opening the tcp server socket on the specified port
     */
    public ConnectionReader(TCPServer tcpServer, int port) throws IOException {
        this.tcpServer = tcpServer;
        socketClosed = false;
        serverSocket = new ServerSocket(port);
    }

    public void stopServerSocket() {
        synchronized (this) {
            try {
                socketClosed = true;
                serverSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public Object readMessage() {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            boolean mustIssueStopReadingMessages;
            synchronized (this) {
                mustIssueStopReadingMessages = socketClosed;
            }
            if (mustIssueStopReadingMessages) {
                // socket closed by user
                return new StopReadingMessages();
            } else {
                // error in the socket
                return e;
            }
        } catch (Exception e2) {
            return e2;
        }
    }

    public void stopped() {
        // connection reader has been stopped. Client has already been reported, in case it was an error -> do nothing else
        try {
            tcpServer.stopServer();
            serverSocket.close();
        } catch (IOException e) {
            // ignore
        }
    }
}
