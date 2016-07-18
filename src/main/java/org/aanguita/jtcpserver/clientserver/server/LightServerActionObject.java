package org.aanguita.jtcpserver.clientserver.server;

import java.io.Serializable;
import java.net.Socket;

/**
 * Light server interface, for implementing a connection-less server that answers generic client requests
 */
public interface LightServerActionObject {

    /**
     * A client makes a new request
     *
     * @param clientSocket the socket of the client
     * @param object       the generic object sent by the client
     * @return the response to the client request
     * @throws Exception there was some kind of error while processing the request.
     */
    public Serializable newClientRequest(Socket clientSocket, Object object) throws Exception;

    /**
     * Error in the connection server. The connection server had to be closed due to IO problems opening the tcp server and could not be restarted.
     * No new connections will be accepted, although connected clients will remain connected
     *
     * @param e exception that caused the error
     */
    public void TCPServerError(Exception e);
}
