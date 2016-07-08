package jacz.commengine.communication;

import org.aanguita.jacuzzi.io.serialization.MutableOffset;
import org.aanguita.jacuzzi.io.serialization.Serializer;
import org.aanguita.jacuzzi.queues.event_processing.MessageReader;
import org.aanguita.jacuzzi.queues.event_processing.StopReadingMessages;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class implements the reading of messages from an ObjectInputStream. It is sent to a MessageProcessor, who
 * executes it.
 */
class MessageReaderImpl implements MessageReader {

    /**
     * CommunicationModule associated to this MessageReaderImpl
     */
    private CommunicationModule communicationModule;

    /**
     * Input stream for reading messages from the other point
     */
    private InputStream ois;

    /**
     * Class constructor
     *
     * @param communicationModule the CommunicationModule for this MessageReaderImpl
     * @param ois                 input stream for reading objects and byte arrays
     */
    MessageReaderImpl(CommunicationModule communicationModule, InputStream ois) {
        this.communicationModule = communicationModule;
        this.ois = ois;
    }

    public Object readMessage() {
        try {
            byte[] oneLengthArray = new byte[1];
            try {
                CommunicationModule.readBytes(ois, oneLengthArray);
            } catch (IOException e) {
                // socket closed
                return new StopReadingMessages();
            }
            // an object is next
            if (oneLengthArray[0] == 0) {
                // first, we receive a byte array encoding an int value, with the length of the object
                byte[] encodedInt = new byte[4];
                CommunicationModule.readBytes(ois, encodedInt);
                int objectLength = Serializer.deserializeIntValue(encodedInt, new MutableOffset());
                // read those bytes, and decode the object
                byte[] encodedObject = new byte[objectLength];
                CommunicationModule.readBytes(ois, encodedObject);
                return Serializer.deserializeObjectWithoutLengthHeader(encodedObject);
            }
            // an array of bytes is next
            else {
                byte[] data = CommunicationModule.readByteArrayFromStreamAux(ois, oneLengthArray);
                return new ByteArrayWrapper(data);
            }
        } catch (ClassNotFoundException e) {
            // the class for an received object was not found. This is notified with an error and a stop
            communicationModule.notifyError(new CommError(CommError.Type.UNKNOWN_CLASS_RECEIVED, e));
            return new StopReadingMessages();
        } catch (IOException e) {
            // some IOException when reading from the channel. This is notified with an error
            communicationModule.notifyError(new CommError(CommError.Type.IO_CHANNEL_FAILED_READING, e));
            return new StopReadingMessages();
        }
    }

    public void stopped() {
        // the reading process was stopped, inform the comm module
        communicationModule.notifyDisconnected();
    }
}
