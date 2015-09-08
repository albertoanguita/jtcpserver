package jacz.commengine.communication;

import jacz.util.io.object_serialization.MutableOffset;
import jacz.util.io.object_serialization.Serializer;
import jacz.util.queues.event_processing.MessageReader;
import jacz.util.queues.event_processing.StopReadingMessages;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.WriteAbortedException;
import java.net.SocketException;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 12/08/14
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
public class MessageReaderImplOld implements MessageReader {

    /**
     * CommunicationModule associated to this MessageReaderImpl
     */
    private CommModuleOld communicationModule;

    /**
     * Input stream for reading messages from the other point
     */
    private ObjectInputStream ois;

    /**
     * Arrays used in the un-serialization process. They are defined here to save time (so we don't have to create
     * arrays each time a message arrives)
     */
    private byte[] oneLengthArray;
    private byte[] twoLengthArray;
    private byte[] fourLengthArray;


    /**
     * Class constructor
     *
     * @param communicationModule the CommunicationModule for this MessageReaderImpl
     * @param ois                 input stream for reading objects and byte arrays
     */
    MessageReaderImplOld(CommModuleOld communicationModule, ObjectInputStream ois) {
        this.communicationModule = communicationModule;
        this.ois = ois;
        oneLengthArray = new byte[1];
        twoLengthArray = new byte[2];
        fourLengthArray = new byte[4];

    }

    /**
     * This method reads an object from the input stream
     *
     * @return the object read (ByteArrayWrapper if an array of bytes was read)
     */
    public Object readMessage() {
        try {
            try {
                readBytes(oneLengthArray);
            } catch (IndexOutOfBoundsException e) {
                return new StopReadingMessages();
            }

            // an object is next
            if (oneLengthArray[0] == 0) {
                return ois.readObject();
            }
            // an array of bytes is next
            else {
                int oneArrayValue = oneLengthArray[0];
                if (oneArrayValue < 0) {
                    oneArrayValue += 256;
                }
                if (oneArrayValue > 0 && oneArrayValue <= 254) {
                    byte[] data = new byte[oneArrayValue];
                    readBytes(data);
                    return new ByteArrayWrapper(data);
                } else if (oneArrayValue == 255) {
                    readBytes(twoLengthArray);
                    int length = Serializer.deserializeShort(twoLengthArray, new MutableOffset());
                    if (length < 0) {
                        length += 65536;
                    }
                    if (length > 0) {
                        byte[] data = new byte[length];
                        readBytes(data);
                        return new ByteArrayWrapper(data);
                    } else {
                        readBytes(fourLengthArray);
                        length = Serializer.deserializeInt(fourLengthArray, new MutableOffset());
                        //int length = 16777216 * twoLengthArray[0] + 65536 * twoLengthArray[1] + 256 * twoLengthArray[2] + twoLengthArray[3];
                        byte[] data = new byte[length];
                        readBytes(data);
                        return new ByteArrayWrapper(data);
                    }
                } else {
                    // cannot happen
                    return new StopReadingMessages();
                }
            }
        } catch (SocketException e) {
            // connection closed at this communication end -> stop reading messages
            // (client will be informed)
            return new StopReadingMessages();
        } catch (EOFException e) {
            // connection closed at the other communication end -> stop reading messages (client will be informed)
            return new StopReadingMessages();
        } catch (WriteAbortedException e) {
            // connection closed at the other communication end due to problems sending a not serializable object ->
            // stop reading messages (client will be informed)
            return new StopReadingMessages();
        } catch (ClassNotFoundException e) {
            // the class for an received object was not found. This is notified with an error and a stop
            if (communicationModule.requestStopOrErrorFlag()) {
                communicationModule.error(new CommError(CommError.Type.UNKNOWN_CLASS_RECEIVED, e));
            }
            return new StopReadingMessages();
        } catch (IOException e) {
            // some IOException when reading from the channel. This is notified with an error
            if (communicationModule.requestStopOrErrorFlag()) {
                communicationModule.error(new CommError(CommError.Type.IO_CHANNEL_FAILED_READING, e));
            }
            return new StopReadingMessages();
        }
        // todo we should test other forms of loosing connection, like unplugging the cable, shutting down the router, etc
    }

    /**
     * Reads a series of bytes from the input stream and stores them in a byte array. The number of bytes read
     * corresponds to the length of the passed array
     *
     * @param array where the bytes will be stored
     * @throws IOException problems reading from the stream
     */
    private void readBytes(byte[] array) throws IOException {
        int bytesRead = 0;
        while (bytesRead < array.length) {
            bytesRead += ois.read(array, bytesRead, array.length - bytesRead);
        }
    }

    public void stopped() {
        // the reading process was stopped, inform the comm module
        if (communicationModule.requestStopOrErrorFlag()) {
            communicationModule.stopped();
        }
    }
}
