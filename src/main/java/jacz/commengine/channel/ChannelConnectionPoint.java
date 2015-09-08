package jacz.commengine.channel;

import jacz.util.identifier.UniqueIdentifier;
import jacz.util.identifier.UniqueIdentifierFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class offers simplified access to a ChannelModule. It is always associated to a ChannelModule, and offers
 * methods to write data, register FSMs and disconnect.
 */
public class ChannelConnectionPoint {

    private final ChannelModule channelModule;

    private final UniqueIdentifier id;

    public ChannelConnectionPoint(ChannelModule channelModule) {
        this.channelModule = channelModule;
        id = UniqueIdentifierFactory.getOneStaticIdentifier();
    }

    public ChannelConnectionPoint(ChannelModule channelModule, UniqueIdentifier id) {
        this.channelModule = channelModule;
        this.id = id;
    }

    public ChannelModule getChannelModule() {
        return channelModule;
    }

    public UniqueIdentifier getId() {
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

    public void write(byte channel, Object message) {
        write(channel, message, true);
    }

    public void write(byte channel, Object message, boolean flush) {
        channelModule.write(channel, message, flush);
    }

    public void write(byte channel, byte[] data) {
        write(channel, data, true);
    }

    public void write(byte channel, byte[] data, boolean flush) {
        channelModule.write(channel, data, flush);
    }

    public void flush() {
        channelModule.flush();
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

    public void registerGenericFSM(ChannelFSMAction<?> channelFSMAction, Byte... involvedChannels) throws IllegalArgumentException {
        registerGenericFSM(channelFSMAction, "unnamedGenericFSM", involvedChannels);
    }

    public void registerGenericFSM(ChannelFSMAction<?> channelFSMAction, String name, Byte... involvedChannels) throws IllegalArgumentException {
        // check involved channels are synchronized, and that non of the channels is already associated to another FSM
        Set<Byte> involvedChannelSet = new HashSet<Byte>(involvedChannels.length);
        involvedChannelSet.addAll(Arrays.asList(involvedChannels));
        channelModule.registerNewFSM(channelFSMAction, name, involvedChannelSet);
    }

    public void registerTimedFSM(TimedChannelFSMAction<?> timedChannelFSMAction, long timeoutMillis, Byte... involvedChannels) throws IllegalArgumentException {
        registerTimedFSM(timedChannelFSMAction, timeoutMillis, "unnamedTimedFSM", involvedChannels);
    }

    public void registerTimedFSM(TimedChannelFSMAction<?> timedChannelFSMAction, long timeoutMillis, String name, Byte... involvedChannels) throws IllegalArgumentException {
        // check involved channels are synchronized, and that non of the channels is already associated to another FSM
        Set<Byte> involvedChannelSet = new HashSet<Byte>(involvedChannels.length);
        involvedChannelSet.addAll(Arrays.asList(involvedChannels));
        channelModule.registerNewFSM(timedChannelFSMAction, timeoutMillis, name, involvedChannelSet);
    }
}
