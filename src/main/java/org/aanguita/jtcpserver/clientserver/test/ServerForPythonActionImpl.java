package org.aanguita.jtcpserver.clientserver.test;

import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.clientserver.server.ServerAction;
import org.aanguita.jtcpserver.communication.CommError;
import org.aanguita.jacuzzi.network.IP4Port;

import java.util.Arrays;

/**
 * Created by Alberto on 16/10/2015.
 */
public class ServerForPythonActionImpl implements ServerAction {

    @Override
    public void newClientConnection(String clientID, ChannelConnectionPoint ccp, IP4Port ip4Port) {
        System.out.println("New client connected: " + clientID);
    }

    @Override
    public void newMessage(String clientID, ChannelConnectionPoint ccp, byte channel, Object message) {
        System.out.println("New object message!!!: " + message);
    }

    @Override
    public void newMessage(String clientID, ChannelConnectionPoint ccp, byte channel, byte[] data) {
        System.out.println("New byte message: " + Arrays.toString(data));
        byte[] response = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            response[i] = b;
        }
        ccp.write((byte) 0, response, true);
    }

    @Override
    public void channelFreed(String clientID, ChannelConnectionPoint ccp, byte channel) {
        System.out.println("Channel freed");
    }

    @Override
    public void clientDisconnected(String clientID, ChannelConnectionPoint ccp, boolean expected) {
        System.out.println("Client disconnected: " + clientID);
    }

    @Override
    public void clientError(String clientID, ChannelConnectionPoint ccp, CommError e) {
        System.out.println("Client error: " + clientID);
    }

    @Override
    public void newConnectionError(Exception e, IP4Port ip4Port) {
        System.out.println("New connection error");
    }

    @Override
    public void TCPServerError(Exception e) {
        System.out.println("TCP server error");
    }
}
