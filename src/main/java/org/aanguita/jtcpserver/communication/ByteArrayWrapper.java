package org.aanguita.jtcpserver.communication;

/**
 * This class offers an object mode for storing an array of bytes. When the CommunicationModule reads an array of
 * bytes, they are stored as a ByteArrayWrapper so they can be obtained as an object.
 *
 * This object can only be created by classes of this package, it is not intended to be used outside of this package. However, it can be returned
 * as a result of a read operation on the CommunicationModule
 */
public class ByteArrayWrapper {

    /**
     * The stored bytes
     */
    private byte[] data;

    /**
     * Class constructor
     *
     * @param data the bytes to store
     */
    ByteArrayWrapper(byte[] data) {
        this.data = data;
    }

    /**
     * Retrieves the bytes stored in this ByteArrayWrapper
     *
     * @return the bytes stored in this ByteArrayWrapper 
     */
    public byte[] getData() {
        return data;
    }
}
