package jacz.commengine.channel;

import jacz.util.queues.event_processing.MessageHandler;

/**
 * Message handler for treating incoming channel messages
 */
class MessageHandlerImpl implements MessageHandler {

    /**
     * ChannelModule to which this object belongs
     */
    private ChannelModule channelModule;

    public MessageHandlerImpl(ChannelModule channelModule) {
        this.channelModule = channelModule;
    }

    public void handleMessage(Object o) {
        channelModule.handleIncomingMessage(o);
    }

    @Override
    public void finalizeHandler() {
        // no resources to close
    }
}
