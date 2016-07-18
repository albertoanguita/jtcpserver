package org.aanguita.jtcpserver.clientserver.server;

import org.aanguita.jtcpserver.channel.ChannelAction;
import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.communication.CommError;

/**
 * This class implements the ChannelAction interface so the incoming messages from clients and some additional
 * events are handled appropriately. The actions are delegated to a ServerAction object given by the client of the
 * server module.
 * <p/>
 * There is one object of this class for each connected client. The client ID is stored here.
 */
class ChannelActionImpl implements ChannelAction {

    /**
     * Server module to which the ChannelActionImpl belongs to
     */
    private ServerModule serverModule;

    /**
     * Class constructor. The client ID is not given here because it is never known at construction time. It is known
     * after the corresponding ChannelModule has been created
     *
     * @param serverModule server module to which the ChannelActionImpl belongs to
     */
    public ChannelActionImpl(ServerModule serverModule) {
        this.serverModule = serverModule;
    }

    public void newMessage(ChannelConnectionPoint ccp, byte channel, Object message) {
        serverModule.reportNewMessage(ccp, channel, message);
    }

    @Override
    public void newMessage(ChannelConnectionPoint ccp, byte channel, byte[] data) {
        serverModule.reportNewMessage(ccp, channel, data);
    }

    @Override
    public void channelFreed(ChannelConnectionPoint ccp, byte channel) {
        serverModule.reportChannelsFreed(ccp, channel);
    }

    @Override
    public void disconnected(ChannelConnectionPoint ccp, boolean expected) {
        serverModule.reportClientDisconnected(ccp, expected);
    }

    @Override
    public void error(ChannelConnectionPoint ccp, CommError e) {
        serverModule.reportClientError(ccp, e);
    }
}
