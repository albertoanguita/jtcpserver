package org.aanguita.jtcpserver.clientserver.client;

import org.aanguita.jtcpserver.channel.ChannelAction;
import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.channel.ChannelModule;
import org.aanguita.jtcpserver.tcpconnection.client.TCPClient;
import org.aanguita.jacuzzi.network.IP4Port;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

/**
 * This class implements a client module able to connect to a server. Only the connection process is handled here.
 * With the connection, a ChannelConnectionPoint object is given, allowing communication with the server. However,
 * a ChannelAction object must be given at construction time to indicate how to treat incoming messages.
 * <p/>
 * The server details are given at construction time and cannot be modified later, so in case we want to connect to
 * a different server, we will have to create a new object.
 * <p/>
 * In order to disconnect from the server we must use the returned ChannelConnectionPoint object, as this class has no
 * method for disconnecting
 */
public class ClientModule {

    /**
     * IP and port of the server to which we will connect
     */
    private final IP4Port serverIp4Port;

    /**
     * ChannelAction object to use for the treatment of incoming messages from the server, once we are connected
     */
    private final ChannelAction channelAction;

    /**
     * Sets of channels handled by the same thread --> order of messages to two channels in the same set will be
     * maintained, and will be sequentially processed
     * <p/>
     * In addition, channels not present in any set are considered not-used, and any incoming message to any not-used
     * channel will be discarded
     * <p/>
     * A null value for this attribute indicates that all channels are used, by a unique thread.
     */
    private final Set<Set<Byte>> concurrentChannels;

    /**
     * ChannelModule employed for communicating with the server
     */
    private ChannelModule channelModule;

    /**
     * Class constructor
     *
     * @param serverIp4Port      IP and port of the server to which we will connect
     * @param channelAction      actions associated to this client for incoming messages from the server
     * @param concurrentChannels Sets of channels handled by the same thread. A null value for this attribute indicates that all channels are used,
     *                           by a unique thread.
     */
    public ClientModule(IP4Port serverIp4Port,
                        ChannelAction channelAction,
                        Set<Set<Byte>> concurrentChannels) {
        this.serverIp4Port = serverIp4Port;
        this.channelAction = channelAction;
        this.concurrentChannels = concurrentChannels;
        channelModule = null;
    }

    /**
     * Establishes connection to the server
     *
     * @return the ChannelConnectionPoint object that can be used to write messages to the server
     * @throws java.io.IOException raised when there were problems connecting the server (for example the server is
     *                             not available
     */
    public synchronized ChannelConnectionPoint connect() throws IOException {
        // we connect to the server and create the ModuloCanal for communication. From it we obtain the
        // ChannelConnectionPoint to be returned. The ModuloCanal is not started here, must be started separately
        Socket socket = TCPClient.connect(serverIp4Port.getIp(), serverIp4Port.getPort());
        channelModule = new ChannelModule(socket, channelAction, concurrentChannels);
        return channelModule.getChannelConnectionPoint();
    }

    /**
     * Establishes connection to the server, with a specific timeout
     *
     * @return the ChannelConnectionPoint object that can be used to write messages to the server
     * @throws java.io.IOException raised when there were problems connecting the server (for example the server is
     *                             not available
     */
    public synchronized ChannelConnectionPoint connect(int timeout) throws IOException {
        // we connect to the server and create the ModuloCanal for communication. From it we obtain the
        // ChannelConnectionPoint to be returned. The ModuloCanal is not started here, must be started separately
        Socket socket = TCPClient.connect(serverIp4Port.getIp(), serverIp4Port.getPort(), timeout);
        channelModule = new ChannelModule(socket, channelAction, concurrentChannels);
        return channelModule.getChannelConnectionPoint();
    }

    /**
     * Makes the ChannelModule created during the connection process begin processing incoming messages from the server
     */
    public synchronized void start() {
        if (channelModule != null) {
            channelModule.start();
        }
    }
}
