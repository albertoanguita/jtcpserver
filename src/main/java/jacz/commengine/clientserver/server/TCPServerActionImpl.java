package jacz.commengine.clientserver.server;

import jacz.commengine.tcpconnection.server.TCPServerAction;

import java.net.Socket;

/**
 * This class implements the TCPServerAction interface for handling new connections from clients
 */
class TCPServerActionImpl implements TCPServerAction {

    /**
     * Server module to which the TCPServerActionImpl belongs to
     */
    private ServerModule serverModule;

    /**
     * Class constructor
     *
     * @param serverModule server module to which the TCPServerActionImpl belongs to
     */
    public TCPServerActionImpl(ServerModule serverModule) {
        this.serverModule = serverModule;
    }

    @Override
    public void processNewConnection(Socket socket) {
        serverModule.reportNewConnection(socket);
    }

    @Override
    public void error(Exception e) {
        serverModule.reportTCPServerError(e);
    }
}
