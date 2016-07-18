package org.aanguita.jtcpserver.clientserver.server;

import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.communication.CommError;
import org.aanguita.jacuzzi.network.IP4Port;

/**
 * This interface contains the methods required for using a ServerModule. These handle the arrival of messages from
 * clients, the connection and disconnection from clients, etc.
 * <p/>
 * Each client is identified by a UniqueIdentifier object which is notified at the moment the client connects to
 * the server. The connection method also provides the ChannelConnectionPoint for directly communicating with the
 * client, in case we prefer to bypass the ServerModule API. The client ID given always equals the ID contained in the
 * ChannelConnectionPoint object.
 * <p/>
 * Concurrency-related considerations:
 * - The newMessage methods are called by independent threads. The concurrency of these threads depends on how the concurrentChannels were set up.
 * - The channelsFreed is also called by an independent thread. Its invocation however will never overlap with any call to newMessage,
 * but will be called by the same thread that made the last call to the corresponding FSM (the one being freed)
 * - clientDisconnected and clientError methods are called by an independent thread. Only one of them will ever be invoked, and only once. Its invocation
 * will never overlap any other invocation in this interface (including FSM methods), and no other invocations related to client connections
 * will follow them
 * - The newClientConnection, newConnectionError and TCPServerError methods are executed by an independent thread, so none of these three can overlap
 * - TCPServerErrors produced on server start are issued by the same thread that invoked the start method.
 * - None of these calls will ever hold the ServerModule synchronized
 */
public interface ServerAction {

    /**
     * Method invoked when a new client connects to the ServerModule. This method is guaranteed to be invoked before the channel module for this
     * client is started, so FSMs can be attached first
     *
     * @param clientID identifier assigned to this new client. This id is used to refer to this client in future actions in the server module
     *                 (write, disconnect, etc)
     * @param ccp      cpp of this client (we can use it to bypass the server module for communicating with the client)
     * @param ip4Port  connection details of the new client (ip and port)
     */
    void newClientConnection(String clientID, ChannelConnectionPoint ccp, IP4Port ip4Port);

    /**
     * Method invoked when a new object message arrives from a connected client
     *
     * @param clientID id of the client that sends the message
     * @param ccp      the ChannelConnectionPoint object associated to the client
     * @param channel  channel through which the message arrives
     * @param message  the object message
     */
    void newMessage(String clientID, ChannelConnectionPoint ccp, byte channel, Object message);

    /**
     * Method invoked when a new byte array message arrives from a connected client
     *
     * @param clientID id of the client that sends the message
     * @param ccp      the ChannelConnectionPoint object associated to the client
     * @param channel  channel through which the message arrives
     * @param data     the byte array message
     */
    void newMessage(String clientID, ChannelConnectionPoint ccp, byte channel, byte[] data);

    /**
     * Method invoked when a set of concurrent channels if freed (no FSMs monitor then any more). During this method
     * invocation, no other threads will be able to register new FSMs
     *
     * @param clientID the ID of the client whose channels are freed
     * @param ccp      ChannelConnectionPoint object which frees the channels
     * @param channel  channel freed
     */
    void channelFreed(String clientID, ChannelConnectionPoint ccp, byte channel);

    /**
     * Method invoked when a client disconnects from the server
     *
     * @param clientID ID of the clientDisconnected client
     * @param ccp      ChannelConnectionPoint object for the disconnected client
     * @param expected true if the disconnection is provoked by an action initiated by our client, false otherwise
     */
    void clientDisconnected(String clientID, ChannelConnectionPoint ccp, boolean expected);

    /**
     * An unexpected exception was raised when reading objects from the socket of a client. This client was disconnected
     *
     * @param clientID id of the client whose connection raised an error
     * @param ccp      ChannelConnectionPoint object which raised the error
     * @param e        raised exception
     */
    void clientError(String clientID, ChannelConnectionPoint ccp, CommError e);

    /**
     * Error listening to a new connection. This new connection was ignored, server keeps running. It is basically
     * useful for logging, as the service will keep running normally
     *
     * @param e       exception raised
     * @param ip4Port IP address and port of the client whose connection failed
     */
    void newConnectionError(Exception e, IP4Port ip4Port);

    /**
     * Error in the connection server. The connection server had to be closed due to IO problems opening the tcp server and could not be restarted.
     * No new connections will be accepted, although connected clients will remain connected
     *
     * @param e exception that caused the error
     */
    void TCPServerError(Exception e);
}
