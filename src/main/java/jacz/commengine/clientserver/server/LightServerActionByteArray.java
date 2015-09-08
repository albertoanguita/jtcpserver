package jacz.commengine.clientserver.server;

import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 12/02/14
 * Time: 22:25
 * To change this template use File | Settings | File Templates.
 */
public interface LightServerActionByteArray {

    public byte[] newClientConnection(Socket clientSocket, byte[] data) throws Exception;

    /**
     * Error in the connection server. The connection server had to be closed due to IO problems opening the tcp server and could not be restarted.
     * No new connections will be accepted, although connected clients will remain connected
     *
     * @param e exception that caused the error
     */
    public void TCPServerError(Exception e);

}
