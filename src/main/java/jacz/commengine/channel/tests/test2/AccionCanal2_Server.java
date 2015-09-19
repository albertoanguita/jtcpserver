package jacz.commengine.channel.tests.test2;

import jacz.commengine.channel.ChannelAction;
import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.communication.CommError;

import java.util.Set;

/**
 */
public class AccionCanal2_Server implements ChannelAction {

    private String version;

    public AccionCanal2_Server(String version) {
        this.version = version;
    }

    public void newMessage(ChannelConnectionPoint ccp, byte canal, Object mensaje) {
        // initiate FSM
        if (canal == 0) {
            ccp.registerGenericFSM(new FSMServer(version), (byte) 0);
        } else {
            System.out.println("Server. Mensaje recibido por canal " + canal + ": " + mensaje.toString());
        }
    }

    @Override
    public void channelFreed(ChannelConnectionPoint ccp, byte channel) {
        System.out.println("Channels freed: " + channel);
    }

    public void newMessage(ChannelConnectionPoint ccp, byte canal, byte[] data) {
        // ignore
    }

    public void disconnected(ChannelConnectionPoint ccp, boolean expected) {
        System.out.println("comm disconnected");
    }

    public void error(ChannelConnectionPoint ccp, CommError e) {
        System.out.println(e);
    }

}
