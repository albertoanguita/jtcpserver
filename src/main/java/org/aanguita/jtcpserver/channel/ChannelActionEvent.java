package org.aanguita.jtcpserver.channel;

import org.aanguita.jtcpserver.communication.CommError;

import java.util.Set;

/**
 * Events associated to the channel action
 */
class ChannelActionEvent {

    static enum EventType {
        NEW_OBJECT_MESSAGE,
        NEW_DATA_MESSAGE,
        CHANNELS_FREED,
        DISCONNECTED,
        ERROR
    }

    final EventType eventType;

    final ChannelConnectionPoint ccp;

    final byte channel;

    final Object message;

    final byte[] data;

    final Set<Byte> channels;

    final boolean expected;

    final CommError e;

    private ChannelActionEvent(EventType eventType, ChannelConnectionPoint ccp, byte channel, Object message, byte[] data, Set<Byte> channels, boolean expected, CommError e) {
        this.eventType = eventType;
        this.ccp = ccp;
        this.channel = channel;
        this.message = message;
        this.data = data;
        this.channels = channels;
        this.expected = expected;
        this.e = e;
    }

    static ChannelActionEvent buildNewObjectMessage(ChannelConnectionPoint ccp, byte channel, Object message) {
        return new ChannelActionEvent(EventType.NEW_OBJECT_MESSAGE, ccp, channel, message, null, null, false, null);
    }

    static ChannelActionEvent buildNewDataMessage(ChannelConnectionPoint ccp, byte channel, byte[] data) {
        return new ChannelActionEvent(EventType.NEW_DATA_MESSAGE, ccp, channel, null, data, null, false, null);
    }

    static ChannelActionEvent buildChannelsFreed(ChannelConnectionPoint ccp, Set<Byte> channels) {
        return new ChannelActionEvent(EventType.CHANNELS_FREED, ccp, (byte) 0, null, null, channels, false, null);
    }

    static ChannelActionEvent buildDisconnected(ChannelConnectionPoint ccp, boolean expected) {
        return new ChannelActionEvent(EventType.DISCONNECTED, ccp, (byte) 0, null, null, null, expected, null);
    }

    static ChannelActionEvent buildError(ChannelConnectionPoint ccp, CommError e) {
        return new ChannelActionEvent(EventType.ERROR, ccp, (byte) 0, null, null, null, false, e);
    }
}
