package org.aanguita.jtcpserver.channel.tests.test2;

import org.aanguita.jtcpserver.channel.ChannelAction;
import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.communication.CommError;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 10-mar-2010
 * Time: 16:36:54
 * To change this template use File | Settings | File Templates.
 */
public class AccionCanal2_Client implements ChannelAction {


    public void newMessage(ChannelConnectionPoint ccp, byte canal, Object mensaje) {
        System.out.println("Server. Mensaje recibido por canal " + canal + ": " + mensaje.toString());
    }

    public void newMessage(ChannelConnectionPoint ccp, byte canal, byte[] data) {
    }

    @Override
    public void channelFreed(ChannelConnectionPoint ccp, byte channel) {
        System.out.println("Channels freed: " + channel);
    }

    public void disconnected(ChannelConnectionPoint ccp, boolean expected) {
        System.out.println("comm disconnected");
    }

    public void error(ChannelConnectionPoint ccp, CommError e) {
        System.out.println(e);
    }

}
