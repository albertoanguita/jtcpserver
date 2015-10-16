package jacz.commengine.clientserver.test;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.clientserver.server.ServerAction;
import jacz.commengine.communication.CommError;
import jacz.util.identifier.UniqueIdentifier;
import jacz.util.network.IP4Port;

import java.util.Arrays;

/**
 * Created by Alberto on 16/10/2015.
 */
public class ServerForPythonActionImpl implements ServerAction {

    @Override
    public void newClientConnection(UniqueIdentifier clientID, ChannelConnectionPoint ccp, IP4Port ip4Port) {
        System.out.println("New client connected: " + clientID);
    }

    @Override
    public void newMessage(UniqueIdentifier clientID, ChannelConnectionPoint ccp, byte channel, Object message) {
        System.out.println("New object message!!!: " + message);
    }

    @Override
    public void newMessage(UniqueIdentifier clientID, ChannelConnectionPoint ccp, byte channel, byte[] data) {
        System.out.println("New byte message: " + Arrays.toString(data));
        byte[] response = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            response[i] = b;
        }
        ccp.write((byte) 0, response, true);
    }

    @Override
    public void channelFreed(UniqueIdentifier clientID, ChannelConnectionPoint ccp, byte channel) {
        System.out.println("Channel freed");
    }

    @Override
    public void clientDisconnected(UniqueIdentifier clientID, ChannelConnectionPoint ccp, boolean expected) {
        System.out.println("Client disconnected: " + clientID);
    }

    @Override
    public void clientError(UniqueIdentifier clientID, ChannelConnectionPoint ccp, CommError e) {
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
