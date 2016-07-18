package org.aanguita.jtcpserver.channel.tests.test1;

import org.aanguita.jtcpserver.channel.ChannelAction;
import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.communication.CommError;

import java.util.Arrays;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 23-dic-2008<br>
 * Last Modified: 23-dic-2008
 */
public class AccionCanal1 implements ChannelAction {

    private String name;

    public AccionCanal1(String name) {
        this.name = name;
    }

    public void newMessage(ChannelConnectionPoint ccp, byte canal, Object mensaje) {
        String text = (String) mensaje;
        System.out.println(name + " read a message through channel " + canal + ": " + text);
        ccp.write(canal, new NotSerClass());
    }

    public void newMessage(ChannelConnectionPoint ccp, byte canal, byte[] data) {
        System.out.println(name + " reads a byte[] through channel " + canal + ": " + Arrays.toString(data));
    }

    @Override
    public void channelFreed(ChannelConnectionPoint ccp, byte channel) {
        System.out.println("Channel freed: " + channel);
    }

    public void disconnected(ChannelConnectionPoint ccp, boolean expected) {
        System.out.println("comm disconnected");
    }

    public void error(ChannelConnectionPoint ccp, CommError e) {
        System.out.println(e);
    }
}
