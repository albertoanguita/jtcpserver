package jacz.commengine.tcpconnection.server;

import java.net.Socket;

/**
 * This interface contain methods that are invoked during the listening connection process. They allow clients of this API to specify to actions
 * to be carried out whenever new connections arrive or when an error arises
 * <p/>
 * Concurrency-related considerations:
 * - The processNewConnection method is executed by an independent thread
 * - The error method is also executed by the same thread, so it cannot overlap with the processNewConnection call
 * - Errors produced on engine start are issued by the same thread that invoked the start method.
 * - None of these calls will ever hold the TCPServer synchronized
 */
public interface TCPServerAction {

    /**
     * A connection has been achieved with a new client
     *
     * @param clientSocket the socket object to communicate with the client from now on
     */
    public void processNewConnection(Socket clientSocket);

    /**
     * The server stopped due to an I/O error and could not be restarted again. No further connections will be accepted.
     * <p/>
     * This error can be produced when starting the socket for listening connections or while handling a new connection
     *
     * @param e the exception that caused this error
     */
    public void error(Exception e);
}
