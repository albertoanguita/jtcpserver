package jacz.commengine.communication;

import jacz.util.date_time.TimeElapsed;
import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.io.object_serialization.Serializer;
import jacz.util.queues.event_processing.MessageProcessor;

import java.io.*;
import java.net.Socket;

/**
 * This class offers a module for point to point TCP based communications. Both objects and arrays of bytes can be sent
 * to other remote points.
 * <p/>
 * The incoming data must be read by the client using this module, through the method read(). This method always
 * returns an object. In case an array of bytes was received, the object will belong to the class ByteArrayWrapper,
 * containing the expected data.
 * <p/>
 * Regarding efficiency of transmission, the sent messages will always carry some overhead. In case of objects, the
 * mentioned overhead will be of 1 byte (usually the serialization of an integer occupies around 60 bytes). In the
 * case of arrays of bytes, it will depend on the size of the array. If the length of the array does not surpass 254,
 * then the overhead will be 1 byte again (this means that sending arrays on length little more than 1 wastes a lot
 * of bandwidth). If the length of the array is between 255 and 65535, then the overhead is 3 bytes. Finally, for any
 * greater lengths, the overhead will be 7 bytes.
 * <p/>
 * A disconnected module cannot be again reconnected. A new module should be created to do this.
 * <p/>
 * All public methods in this class are thread-safe
 */
public class CommunicationModule {

    /**
     * Message processor for reading messages from the input stream and storing them in a queue
     */
    private MessageProcessor messageProcessor;

    /**
     * Whether this CommunicationModule is currently connected or not. Initially, it is true (connection is established
     * at construction time)
     */
    private boolean connected;

    /**
     * Whether this comm module was manually disconnected or not
     */
    private boolean manuallyDisconnected;

    /**
     * The socket of this communication
     */
    private Socket socket;

    /**
     * Output stream for sending messages to the other point
     */
    private ObjectOutputStream oos;

    /**
     * Fixed size array for sending the one-byte overheads in the messages (this attribute is here for performance
     * reasons)
     */
    private byte[] oneLengthArray;

    /**
     * If an error has happened, this variable stores the issued error
     */
    private CommError error;

    /**
     * Class constructor
     *
     * @param socket socket for communicating with the other point. Must be correctly initialized
     * @throws IOException if the input and output channels cannot be correctly initialized
     */
    public CommunicationModule(Socket socket) throws IOException {
        this("", socket);
    }

    /**
     * Class constructor
     *
     * @param name   name of this communication module
     * @param socket socket for communicating with the other point. Must be correctly initialized
     * @throws IOException if the input and output channels cannot be correctly initialized
     */
    public CommunicationModule(String name, Socket socket) throws IOException {
        this.socket = socket;
        // order of these two gets cannot be modified, or it will not work
        oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        messageProcessor = new MessageProcessor(name + "/commMod", new MessageReaderImpl(this, ois));
        this.oos.flush();
        manuallyDisconnected = false;
        connected = true;

        oneLengthArray = new byte[1];
        error = null;

        messageProcessor.start();
    }

    /**
     * Disconnects this CommunicationModule. No communications can be performed from this moment (although already
     * stored messages can still be read)
     */
    public synchronized void disconnect() {
        if (connected) {
            manuallyDisconnected = true;
            connected = false;
            try {
                socket.close();
            } catch (IOException e) {
                // ignore, assume that the read operation finished ok
            }
        }
    }

    synchronized void notifyDisconnected() {
        connected = false;
    }

    public synchronized boolean isManuallyDisconnected() {
        return manuallyDisconnected;
    }

    synchronized void notifyError(CommError commError) {
        if (!isError()) {
            // only the first error is registered
            this.error = commError;
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public synchronized boolean isError() {
        return error != null;
    }

    public synchronized CommError getError() {
        return error;
    }

    /**
     * Reads a received message, blocking if there are no messages (until one is received)
     *
     * @return the oldest received message. If an array of bytes was received, then a ByteArrayWrapper object
     * containing it is returned. When the communication socket is closed (either due to an error or any of the users
     * closing the communications) a StopReadingMessages object will be inserted in the queue
     * @throws InterruptedException the thread waiting for a message is interrupted
     */
    public Object read() throws InterruptedException {
        return messageProcessor.takeMessage();
    }

    public static byte[] readByteArrayFromStream(ObjectInputStream ois) throws IOException {
        byte[] oneLengthArray = new byte[1];
        readBytes(ois, oneLengthArray);
        return readByteArrayFromStreamAux(ois, oneLengthArray);
    }

    public static byte[] readByteArrayFromStreamAux(ObjectInputStream ois, byte[] oneLengthArray) throws IOException {
        int length = Serializer.deserializeByteValue(oneLengthArray, new MutableOffset()) & 0xFF;
        if (length == 255) {
            byte[] twoLengthArray = new byte[2];
            readBytes(ois, twoLengthArray);
            length = Serializer.deserializeShortValue(twoLengthArray, new MutableOffset()) & 0xFFFF;
            if (length == 0) {
                byte[] fourLengthArray = new byte[4];
                readBytes(ois, fourLengthArray);
                length = Serializer.deserializeIntValue(fourLengthArray, new MutableOffset());
            }
        }
        byte[] data = new byte[length];
        readBytes(ois, data);
        return data;
    }

    /**
     * Reads a series of bytes from the input stream and stores them in a byte array. The number of bytes read
     * corresponds to the length of the passed array
     *
     * @param array where the bytes will be stored
     * @throws IOException problems reading from the stream
     */
    static void readBytes(ObjectInputStream ois, byte[] array) throws IOException {
        int bytesRead = 0;
        while (bytesRead < array.length) {
            bytesRead += ois.read(array, bytesRead, array.length - bytesRead);
        }
    }


    /**
     * Writes an object message to the other point. An overhead of 1 bytes is added to the message size.
     * <p/>
     * If this CommunicationModule is disconnected, the message is ignored
     *
     * @param message the object to send
     */
    public synchronized long write(Object message) {
        return write(message, true);
    }

    public synchronized long write(Object message, boolean flush) {
        CommError commError = null;
        long time = 0L;
        if (connected) {
            try {
                // one byte containing a zero is sent before the object, to tell the other point that he must read an object
                commError = writeOneLengthArray((byte) 0);
                if (commError == null) {
                    TimeElapsed timeElapsed = new TimeElapsed();
                    oos.writeObject(message);
                    if (flush) {
                        oos.flush();
                    }
                    time = timeElapsed.measureTime();
                }
            } catch (InvalidClassException e) {
                commError = new CommError(CommError.Type.CLASS_CANNOT_BE_SERIALIZED, e);
            } catch (NotSerializableException e) {
                commError = new CommError(CommError.Type.WRITE_NON_SERIALIZABLE_OBJECT, e);
            } catch (IOException e) {
                commError = new CommError(CommError.Type.IO_CHANNEL_FAILED_WRITING, e);
            }
        }
        if (commError != null) {
            notifyError(commError);
        }
        return time;
    }

    /**
     * Writes an array of bytes to the other end. An overhead is added to the message size, depending on the array
     * length.
     * if 0 < length < 255:      overhead of 1 byte
     * if 255 <= length < 65536: overhead of 3 bytes
     * if length >= 65536:       overhead of 7 bytes
     * <p/>
     * If this CommunicationModule is disconnected, the message is ignored
     *
     * @param data the array of bytes to send.
     */
    public synchronized long write(byte[] data) {
        return write(data, true);
    }

    public synchronized long write(byte[] data, boolean flush) {
        CommError commError = null;
        TimeElapsed timeElapsed = new TimeElapsed();
        // the first byte sent indicates that an array of bytes is going to be sent.
        // If the value sent is between 1 and 254, then an array of that size is sent.
        // If the value is 255, then the next two bytes indicate the size of the array (btw 255 and 2^16 - 1)
        // if those two bytes are zero, then the next four bytes indicate the size of the array
        // (greater than 2^16 - 1)
        if (connected) {
            try {
                writeByteArrayToStream(oos, data);
                if (flush) {
                    oos.flush();
                }
            } catch (IOException e) {
                commError = new CommError(CommError.Type.IO_CHANNEL_FAILED_WRITING, e);
            }
        }
        if (commError != null) {
            notifyError(commError);
        }
        return timeElapsed.measureTime();
    }

    public synchronized long flush() {
        CommError commError = null;
        long time = 0L;
        if (connected) {
            try {
                TimeElapsed timeElapsed = new TimeElapsed();
                oos.flush();
                time = timeElapsed.measureTime();
            } catch (IOException e) {
                commError = new CommError(CommError.Type.IO_CHANNEL_FAILED_WRITING, e);
            }
        }
        if (commError != null) {
            notifyError(commError);
        }
        return time;
    }

    public static void writeByteArrayToStream(ObjectOutputStream oos, byte[] data) throws IOException {
        if (data.length > 0) {
            if (data.length < 255) {
                oos.write(Serializer.serialize((byte) data.length));
            } else {
                oos.write(Serializer.serialize((byte) 255));
                if (data.length < 65536) {
                    oos.write(Serializer.serialize((short) data.length));
                } else {
                    oos.write(Serializer.serialize((short) 0));
                    oos.write(Serializer.serialize(data.length));
                }
            }
            oos.write(data);
        }
    }

    /**
     * Sends the oneLengthArray to the other point, with the passed value in its only position
     *
     * @param value the value to put in the only position of the array before sending it
     * @return a CommError object if there was an error writing the byte, or null if everything went fine
     */
    private CommError writeOneLengthArray(byte value) {
        oneLengthArray[0] = value;
        try {
            oos.write(oneLengthArray);
            return null;
        } catch (IOException e) {
            return new CommError(CommError.Type.IO_CHANNEL_FAILED_WRITING, e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        socket.close();
    }
}
