package jacz.commengine.tcpconnection.server;

import jacz.util.queues.event_processing.MessageProcessor;

import java.io.IOException;

/**
 * This class represents a TCP server for listening for new connections from clients. New connections are handled as indicated by the implementation
 * of the TCPServerAction interface provided to this class at construction time. This TCP server listens from connections at a fixed port (given
 * also at construction time) until it is told otherwise.
 * <p/>
 * Server can be started, stopped, and restarted (although port and TCPServerAction parameters are given at construction time and cannot be
 * modified later on)
 */
public final class TCPServer {

    /**
     * Port through which connections will be listened from. Given by the client (but not modifiable after construction)
     */
    private final int port;

    /**
     * Implementation of the TCPServerAction interface, containing methods to invoke upon new connections or errors (provided by the client
     * of the API)
     */
    private final TCPServerAction tcpServerAction;

    /**
     * Message processor in charge of accepting and handling incoming client connections
     */
    private MessageProcessor connectionProcessor;


    private ConnectionReader connectionReader;

    /**
     * This parameter indicates whether the TCPServer is running (accepting connections)
     */
    private boolean running;

    /**
     * Class constructor
     *
     * @param port            port for listening connections
     * @param tcpServerAction actions to invoke upon new connections or errors
     */
    public TCPServer(int port, TCPServerAction tcpServerAction) {
        this.port = port;
        this.tcpServerAction = tcpServerAction;
        running = false;
    }

    /**
     * Initializes the message processor for accepting connections (but does not start it)
     *
     * @param port            por for listening connections
     * @param tcpServerAction actions to invoke upon new connections or errors
     * @throws IOException there was an error initializing the socket for listening to connections. The message processor will remain
     *                     uninitialized
     */
    private synchronized void initializeConnectionProcessor(int port, TCPServerAction tcpServerAction) throws IOException {
        connectionReader = new ConnectionReader(this, port);
        connectionProcessor = new MessageProcessor("TCPServer", connectionReader, new ConnectionHandler(this, tcpServerAction), true);
    }

    /**
     * Says whether this TCPServer is currently running (accepting connections) or not
     *
     * @return true if this TCPServer is running, false otherwise
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * Retrieves the port assigned for listening incoming connections
     *
     * @return port for listening connections
     */
    public int getPort() {
        return port;
    }

    /**
     * Retrieves the actual port opened for listening incoming connections
     *
     * @return port for listening connections
     */
    public int getActualPort() {
        return isRunning() ? connectionReader.getPort() : -1;
    }

    /**
     * Starts the server. Connections are accepted from now on. If the server was already running, this method does nothing.
     * <p/>
     * This method might produce an error if the listening socket throws an IOException when opening up. In that case the error method of the
     * tcp server action is invoked.
     */
    public synchronized void startServer() throws IOException {
//        Exception exceptionDueToError = null;
//        synchronized (this) {
//            try {
        if (!isRunning()) {
            initializeConnectionProcessor(port, tcpServerAction);
            connectionProcessor.start();
            running = true;
        }
//            } catch (IOException e) {
//                exceptionDueToError = e;
//            }
//        }
//        if (exceptionDueToError != null) {
//            tcpServerAction.error(exceptionDueToError);
//        }
    }

    /**
     * Stops the server so no more connections are accepted. It is guaranteed that after the invocation of this method no new connections will
     * be created. The server can be started or restarted later
     */
    public void stopServer() {
        synchronized (this) {
//            if (connectionProcessor != null) {
//                connectionProcessor.stop();
//            }
            if (connectionReader != null) {
                connectionReader.stopServerSocket();
            }
            running = false;
        }
    }

    /**
     * Restarts the server. The server is stopped if it was running, and then it is started. This method might produce an error while starting
     * the server.
     */
    public void restartServer() throws IOException {
        stopServer();
        startServer();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopServer();
    }
}
