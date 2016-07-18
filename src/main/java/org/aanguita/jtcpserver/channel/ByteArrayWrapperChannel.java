package org.aanguita.jtcpserver.channel;

/**
 * This class implements an object for storing both a channel and an array of bytes. It is the equivalent to the
 * ByteArrayWrapper, but for the channel level.
 */
public class ByteArrayWrapperChannel {

    /**
     * Channel of communication
     */
    private Byte channel;

    /**
     * Array of bytes composing the data of this message
     */
    private byte[] data;

    /**
     * Class constructor
     *
     * @param channel communication channel
     * @param data    array of bytes composing the data
     */
    ByteArrayWrapperChannel(byte channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }

    /**
     * Retrieves the communication channel
     *
     * @return the communication channel
     */
    public Byte getChannel() {
        return channel;
    }

    /**
     * Retrieves the stored byte array
     *
     * @return the byte array
     */
    public byte[] getData() {
        return data;
    }
}
