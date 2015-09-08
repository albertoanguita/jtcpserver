package jacz.commengine.channel;

import jacz.commengine.communication.CommError;
import jacz.commengine.communication.CommunicationAction;

/**
 * This class implements the CommunicationAction interface required by the CommunicationModule. The actions of stop
 * and error are handled here
 */
class CommunicationActionImpl implements CommunicationAction {

    private ChannelModule channelModule;

    public CommunicationActionImpl(ChannelModule channelModule) {
        this.channelModule = channelModule;
    }

    public void stopped(boolean expected) {
//        channelModule.disconnected(expected);
    }

    public void error(CommError e) {
//        channelModule.error(e);
    }
}
