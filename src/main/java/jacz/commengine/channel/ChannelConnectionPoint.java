package jacz.commengine.channel;

import jacz.util.id.AlphaNumFactory;

import java.io.Serializable;

/**
 * This class offers simplified access to a ChannelModule. It is always associated to a ChannelModule, and offers
 * methods to write data, register FSMs and disconnect.
 */
public class ChannelConnectionPoint {

    private final ChannelModule channelModule;

    private final String id;

    public ChannelConnectionPoint(ChannelModule channelModule) {
        this.channelModule = channelModule;
        id = AlphaNumFactory.getStaticId();
    }

    public ChannelConnectionPoint(ChannelModule channelModule, String id) {
        this.channelModule = channelModule;
        this.id = id;
    }

    public ChannelModule getChannelModule() {
        return channelModule;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelConnectionPoint that = (ChannelConnectionPoint) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public long write(byte channel, Serializable message) {
        return write(channel, message, true);
    }

    public long write(byte channel, Serializable message, boolean flush) {
        return channelModule.write(channel, message, flush);
    }

    public long write(byte channel, byte[] data) {
        return write(channel, data, true);
    }

    public long write(byte channel, byte[] data, boolean flush) {
        return channelModule.write(channel, data, flush);
    }

    public long flush() {
        return channelModule.flush();
    }

    public void disconnect() {
        channelModule.disconnect();
    }

    /**
     * Checks if a given channel has an FSM registered with it
     *
     * @param channel the channel to check
     * @return true if the given channel is registered with an FSM, false otherwise
     */
    public boolean isChannelRegistered(byte channel) {
        return channelModule.isChannelRegistered(channel);
    }

    public String registerGenericFSM(ChannelFSMAction<?> channelFSMAction, byte channel) throws IllegalArgumentException {
        return registerGenericFSM(channelFSMAction, "unnamedGenericFSM", channel);
    }

    public String registerGenericFSM(ChannelFSMAction<?> channelFSMAction, String name, byte channel) throws IllegalArgumentException {
        return channelModule.registerNewFSM(channelFSMAction, name, channel);
    }

    public String registerTimedFSM(TimedChannelFSMAction<?> timedChannelFSMAction, long timeoutMillis, byte channel) throws IllegalArgumentException {
        return registerTimedFSM(timedChannelFSMAction, timeoutMillis, "unnamedTimedFSM", channel);
    }

    public String registerTimedFSM(TimedChannelFSMAction<?> timedChannelFSMAction, long timeoutMillis, String name, byte channel) throws IllegalArgumentException {
        return channelModule.registerNewFSM(timedChannelFSMAction, timeoutMillis, name, channel);
    }
}
