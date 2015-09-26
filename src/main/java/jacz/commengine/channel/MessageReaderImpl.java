package jacz.commengine.channel;

import jacz.commengine.communication.ByteArrayWrapper;
import jacz.commengine.communication.CommunicationModule;
import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.io.object_serialization.Serializer;
import jacz.util.queues.event_processing.MessageReader;

/**
 * This class implements a MessageReader for reading the messages from the CommunicationModule
 */
class MessageReaderImpl implements MessageReader {

    /**
     * Associated ChannelModule
     */
    private ChannelModule channelModule;

    /**
     * CommunicationModule from which messages are read
     */
    private CommunicationModule commModule;

    public MessageReaderImpl(ChannelModule channelModule, CommunicationModule commModule) {
        this.channelModule = channelModule;
        this.commModule = commModule;
    }

    public Object readMessage() {
        // data arrays, given as ByteArrayWrapper objects, ar transformed into ByteArrayWrapperChannel objects at this
        // point. That is what upper modules expect. The other possibility is receiving a ChannelMessage object. In
        // this case it is left in that form.
        Object o = null;
        try {
            o = commModule.read();
            // the own commModule will return a StopReadingMessages object, if finished

            // transform it into a ByteArrayWrapperChannel object, which is what the rest of classes need
            if (o instanceof ByteArrayWrapper) {
                // byte[] received
                ByteArrayWrapper byteArrayWrapper = (ByteArrayWrapper) o;
                MutableOffset mutableOffset = new MutableOffset();
                byte channel = Serializer.deserializeByte(byteArrayWrapper.getData(), mutableOffset);
                return new ByteArrayWrapperChannel(channel, Serializer.deserializeRest(((ByteArrayWrapper) o).getData(), mutableOffset));
            } else {
                // if it is a ChannelMessage object, leave it that way
                return o;
            }
        } catch (InterruptedException e) {
            // ignore -> only the ChannelModule can invoke this one
        }
        return o;
    }

    public void stopped() {
        // a StopReadingMessages was obtained from the CommunicationModule -> notify the ChannelModule so he performs the necessary actions
        channelModule.messageReaderStopped();
    }
}
