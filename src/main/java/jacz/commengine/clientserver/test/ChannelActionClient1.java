package jacz.commengine.clientserver.test;

import jacz.commengine.channel.ChannelAction;
import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.communication.CommError;

import java.util.Set;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 10-ene-2009<br>
 * Last Modified: 10-ene-2009
 */
class ChannelActionClient1 implements ChannelAction {

    private String name;

    public ChannelActionClient1(String name) {
        this.name = name;
    }

    public void newMessage(ChannelConnectionPoint ccp, byte b, Object o) {
        System.out.println("Cliente" + name + " - mensaje recibido por canal " + b + ": " + o);
    }

    @Override
    public void newMessage(ChannelConnectionPoint ccp, byte b, byte[] bytes) {
        System.out.println("Cliente" + name + " - mensaje de byte[] recibido por canal " + b);
    }

    @Override
    public void channelFreed(ChannelConnectionPoint channelConnectionPoint, byte channel) {
        System.out.println("Channels freed " + channel);
    }

    public void disconnected(ChannelConnectionPoint channelConnectionPoint, boolean expected) {
        System.out.println("client stopped");
    }

    public void error(ChannelConnectionPoint channelConnectionPoint, CommError e) {
        System.out.println(e);
    }
}
