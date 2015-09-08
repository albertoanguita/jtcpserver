package jacz.commengine.channel;

import jacz.util.queues.event_processing.MessageHandler;

/**
 * This MessageHandler implementation is in charge of sending incoming messages from the CommunicationModule to
 * individual channel set queues.
 * That is, this will be only used when several threads are employed for handling the incoming messages. This handler
 * will feed the queue of the synchronized channel sets with messages from the CommunicationModule.
 */
class SenderToQueues implements MessageHandler {

    /**
     * The associated ChannelModule (which contains the queues that this handler will feed
     */
    private ChannelModule channelModule;

    /**
     * Class constructor
     *
     * @param channelModule the associated ChannelModule
     */
    public SenderToQueues(ChannelModule channelModule) {
        this.channelModule = channelModule;
    }

    public void handleMessage(Object o) {
        // a message from the CommunicationModule is handled. This message can be an array of bytes (wrapper by a
        // ByteArrayWrapperChannel) or a ChannelMessage object. Implementation of this method differs for each of the
        // cases because the channel is obtained differently in each of them (although in both cases the received
        // object is fed to the corresponding queue the way it comes)
        if (o instanceof ByteArrayWrapperChannel) {
            // byte[] received
            ByteArrayWrapperChannel byteArrayWrapperCanal = (ByteArrayWrapperChannel) o;
            channelModule.addMessageToChannelQueue(byteArrayWrapperCanal.getChannel(), byteArrayWrapperCanal);
        } else {
            ChannelMessage channelMessage = (ChannelMessage) o;
            channelModule.addMessageToChannelQueue(channelMessage.canal, channelMessage);
        }
    }

    @Override
    public void finalizeHandler() {
        // no resources to close
    }
}
