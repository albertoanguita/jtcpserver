package jacz.commengine.clientserver.server;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.channel.ChannelModule;
import jacz.commengine.communication.CommError;
import jacz.commengine.tcpconnection.server.TCPServer;
import jacz.util.identifier.UniqueIdentifier;
import jacz.util.network.IP4Port;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements a server to which clients can connect from the internet. The server performs the specific
 * actions through a ServerAction interface, which specifies what to do with incoming connections or other events.
 * In addition, the ability to send messages to groups of clients is included.
 * <p/>
 * The listening port of the server is defined at construction time, and cannot be modified later.
 * <p/>
 * Each connected client is identified through an long. Each time a client connects, a method indicating the
 * integer assigned to the new client, together with its ChannelConnectionPoint will be invoked. The long id allows
 * to submit messages to the clients through the ServerModule, although that id can be ignored and solely use the
 * ChannelConnectionPoint.
 * <p/>
 * Communication with each client is handled though the channel module package. Thus, the ability to send messages
 * through 256 different channels is included.
 */
public class ServerModule {

    /**
     * Actions to be carried out by the ServerModule upon several events (new client connected, etc)
     */
    private final ServerAction serverAction;

    /**
     * List of connected clients, with detailed information of each one (id, ccp, ip & port)
     */
    private final ConnectedClients connectedClients;

    /**
     * The TCPServer object that listens to new connections for us
     */
    private final TCPServer tcpServer;

    /**
     * Sets of concurrent channels to be used with new client's connections
     */
    private final Set<Set<Byte>> concurrentChannels;

    /**
     * Class constructor
     *
     * @param port               port where the server will be listening to new connections
     * @param serverAction       actions to carry out by the ServerModule upon different events
     * @param concurrentChannels sets of concurrent channels to establish for new client connections (null to indicate
     *                           that all channels are used by a single thread
     */
    public ServerModule(int port, ServerAction serverAction, Set<Set<Byte>> concurrentChannels) {
        // the concurrent channels are copied, so the given parameter does not affect us in the future
        this.connectedClients = new ConnectedClients();
        this.serverAction = serverAction;
        if (concurrentChannels != null) {
            this.concurrentChannels = new HashSet<Set<Byte>>(concurrentChannels.size());
            for (Set<Byte> channelSet : concurrentChannels) {
                this.concurrentChannels.add(new HashSet<Byte>(channelSet));
            }
        } else {
            this.concurrentChannels = null;
        }
        tcpServer = new TCPServer(port, new TCPServerActionImpl(this));
    }

    /**
     * Starts the server. Clients can now connect to this server
     */
    public synchronized void startListeningConnections() {
        tcpServer.startServer();
    }

    /**
     * Stops the connection listening service (but connected clients remain as before). Is is guaranteed that after the invocation of this method,
     * no more clients will connect (even ongoing connections). The server can be started again
     */
    public synchronized void stopListeningConnections() {
        tcpServer.stopServer();
    }

    /**
     * Stops the connection listening service and disconnects all clients. Is is guaranteed that after the invocation of this method,
     * no more clients will connect (even ongoing connections). The server can be started again
     */
    public synchronized void stopAndDisconnect() {
        stopListeningConnections();
        disconnectAllClients();
    }

    /**
     * Retrieves the listening port of this ServerModule
     *
     * @return the listening port of this ServerModule
     */
    public int getListeningPort() {
        return tcpServer.getPort();
    }

    /**
     * Retrieves the number of connected clients
     *
     * @return the number of connected clients
     */
    public int getConnectedClientsCount() {
        return connectedClients.getConnectedClientsCount();
    }

    /**
     * Retrieves the channel connection point of a client
     *
     * @param clientID id of the client
     * @return the channel connection point object corresponding to the given client
     */
    public ChannelConnectionPoint getCCP(UniqueIdentifier clientID) {
        return connectedClients.getCCP(clientID);
    }

    /**
     * Retrieves the internet address information (ip and port) of a client
     *
     * @param clientID id of the client
     * @return the IP4Port object corresponding to the given client
     */
    public IP4Port getIP4Port(UniqueIdentifier clientID) {
        return connectedClients.getClientIP4Port(clientID);
    }

    /**
     * Disconnects a client from our server
     *
     * @param clientID the ID of the client to disconnect.
     */
    public void disconnectClient(UniqueIdentifier clientID) {
        ChannelConnectionPoint ccp = connectedClients.getCCP(clientID);
        if (ccp != null) {
            ccp.disconnect();
        }
    }

    /**
     * Disconnects all connected clients. This method can be invoked with the server running or stopped
     */
    public void disconnectAllClients() {
        Set<UniqueIdentifier> clientIDs;
        clientIDs = connectedClients.getClientIDs();
        for (UniqueIdentifier clientID : clientIDs) {
            disconnectClient(clientID);
        }
    }


    /**
     * Sends a message to a specific client
     *
     * @param clientID ID of the client to send the message
     * @param channel  channel for sending the message
     * @param message  the message to send
     * @throws java.io.IOException       exception raised when sending the message
     */
    public void write(UniqueIdentifier clientID, byte channel, Object message) throws IOException {
        ChannelConnectionPoint ccp = connectedClients.getCCP(clientID);
        if (ccp != null) {
            ccp.write(channel, message);
        }
    }

    /**
     * Sends an array of bytes to a specific client
     *
     * @param clientID ID of the client to send the message
     * @param channel  channel for sending the message
     * @param data     the data to send
     * @throws java.io.IOException       exception raised when sending the message
     */
    public void write(UniqueIdentifier clientID, byte channel, byte[] data) throws IOException {
        ChannelConnectionPoint ccp = connectedClients.getCCP(clientID);
        if (ccp != null) {
            ccp.write(channel, data);
        }
    }

    /**
     * Sends a message to all connected clients
     *
     * @param channel channel for sending the message
     * @param message the message to send
     * @throws java.io.IOException exception raised when sending the message
     */
    public void writeAll(byte channel, Object message) throws IOException {
        Set<UniqueIdentifier> clientIDs;
        clientIDs = connectedClients.getClientIDs();
        writeAllIn(clientIDs, channel, message);
    }

    /**
     * Sends a message to all connected clients
     *
     * @param channel channel for sending the message
     * @param data    the data to send
     * @throws java.io.IOException exception raised when sending the message
     */
    public void writeAll(byte channel, byte[] data) throws IOException {
        Set<UniqueIdentifier> clientIDs;
        clientIDs = connectedClients.getClientIDs();
        writeAllIn(clientIDs, channel, data);
    }

    /**
     * Sends a message to a list of clients
     *
     * @param clientIDs IDs of the clients to send the message
     * @param channel   channel for sending the message
     * @param message   message to send
     * @throws java.io.IOException       exception raised when sending the message
     */
    public void writeAllIn(Set<UniqueIdentifier> clientIDs, byte channel, Object message) throws IOException {
        for (UniqueIdentifier clientID : clientIDs) {
            write(clientID, channel, message);
        }
    }

    /**
     * Sends an array of bytes to a list of clients
     *
     * @param clientIDs IDs of the clients to send the message
     * @param channel   channel for sending the message
     * @param data      the data to send
     * @throws java.io.IOException       exception raised when sending the message
     */
    public void writeAllIn(Set<UniqueIdentifier> clientIDs, byte channel, byte[] data) throws IOException {
        for (UniqueIdentifier clientID : clientIDs) {
            write(clientID, channel, data);
        }
    }

    /**
     * Sends a message to all clients excepts a list of given ids
     *
     * @param channel      channel for sending the message
     * @param message      message to send
     * @param clientIDsOut the ids of the clients to exclude
     * @throws java.io.IOException       exception raised when sending the message to some client
     */
    public void writeAllBut(byte channel, Object message, UniqueIdentifier... clientIDsOut) throws IOException {
        Set<UniqueIdentifier> clientIDs;
        clientIDs = connectedClients.getClientIDsExcept(clientIDsOut);
        writeAllIn(clientIDs, channel, message);
    }

    /**
     * Sends a message to all clients excepts a list of given ids
     *
     * @param channel      channel for sending the message
     * @param data         the data to send
     * @param clientIDsOut the ids of the clients to exclude
     * @throws java.io.IOException       exception raised when sending the message to some client
     */
    public void writeAllBut(byte channel, byte[] data, UniqueIdentifier... clientIDsOut) throws IOException {
        Set<UniqueIdentifier> clientIDs;
        clientIDs = connectedClients.getClientIDsExcept(clientIDsOut);
        writeAllIn(clientIDs, channel, data);
    }


    ////////////////////////////////////////////////////////
    //     EVENTS FROM MODULE CHANNELS AND TCP SERVER     //
    ////////////////////////////////////////////////////////

    void reportNewMessage(ChannelConnectionPoint ccp, byte channel, Object message) {
        serverAction.newMessage(ccp.getId(), ccp, channel, message);
    }

    void reportNewMessage(ChannelConnectionPoint ccp, byte channel, byte[] data) {
        serverAction.newMessage(ccp.getId(), ccp, channel, data);
    }

    void reportChannelsFreed(ChannelConnectionPoint ccp, byte channel) {
        serverAction.channelFreed(ccp.getId(), ccp, channel);
    }

    void reportClientDisconnected(ChannelConnectionPoint ccp, boolean expected) {
        removeClient(ccp.getId());
        serverAction.clientDisconnected(ccp.getId(), ccp, expected);
    }

    void reportClientError(ChannelConnectionPoint ccp, CommError e) {
        removeClient(ccp.getId());
        serverAction.clientError(ccp.getId(), ccp, e);
    }

    /**
     * Allows the TCPServer to notify a new client connection. The clients socket is provided
     *
     * @param socket new client's socket
     */
    synchronized void reportNewConnection(Socket socket) {
        ChannelModule channelModule = null;
        String clientIP = null;
        int clientPort = 0;
        Exception exception = null;
        // first check that the tcp server is running, otherwise no connections are accepted
        if (tcpServer.isRunning()) {
            // the ChannelModule for this new client is created and initialized using the information from the socket
            clientPort = socket.getPort();
            clientIP = socket.getInetAddress().getHostAddress();
            ChannelActionImpl channelActionImpl = new ChannelActionImpl(this);

            try {
                channelModule = new ChannelModule(socket, channelActionImpl, concurrentChannels);
                connectedClients.addClient(channelModule, clientIP, clientPort);
            } catch (IOException e) {
                exception = e;
            }
        }
        if (channelModule != null) {
            // the new client method is invoked before incoming messages are processed, so FSMs can be established first
            serverAction.newClientConnection(channelModule.getChannelConnectionPoint().getId(), channelModule.getChannelConnectionPoint(), new IP4Port(clientIP, clientPort));
            // now we can startListeningConnections processing client's incoming messages
            channelModule.start();
        } else if (exception != null) {
            serverAction.newConnectionError(exception, new IP4Port(clientIP, clientPort));
        }
    }

    /**
     * This method allows the TCPServer indicate that an error has been produced
     *
     * @param e exception that caused the error
     */
    void reportTCPServerError(Exception e) {
        serverAction.TCPServerError(e);
    }

    /**
     * Removes the stored information of a client (but does not disconnect it)
     *
     * @param clientID the id of the client to remove
     */
    private void removeClient(UniqueIdentifier clientID) {
        connectedClients.removeClient(clientID);
    }
}
