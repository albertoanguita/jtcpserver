package jacz.commengine.communication;

import jacz.util.queues.event_processing.MessageReader;
import jacz.util.queues.event_processing.StopReadingMessages;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.WriteAbortedException;
import java.net.SocketException;

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
    private ObjectInputStream ois;

    /**
     * Class constructor
     *
     * @param communicationModule the CommunicationModule for this MessageReaderImpl
     * @param ois                 input stream for reading objects and byte arrays
     */
    MessageReaderImpl(CommunicationModule communicationModule, ObjectInputStream ois) {
        this.communicationModule = communicationModule;
        this.ois = ois;
    }

    /**
     * This method reads an object from the input stream
     *
     * @return the object read (ByteArrayWrapper if an array of bytes was read)
     */
    public Object readMessage() {
        try {
            byte[] oneLengthArray = new byte[1];
            try {
                CommunicationModule.readBytes(ois, oneLengthArray);
            } catch (IndexOutOfBoundsException e) {
                return new StopReadingMessages();
            }
            // an object is next
            if (oneLengthArray[0] == 0) {
                return ois.readObject();
            }
            // an array of bytes is next
            else {
                byte[] data = CommunicationModule.readByteArrayFromStreamAux(ois, oneLengthArray);
                return new ByteArrayWrapper(data);
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
            communicationModule.notifyError(new CommError(CommError.Type.UNKNOWN_CLASS_RECEIVED, e));
            return new StopReadingMessages();
        } catch (IOException e) {
            // some IOException when reading from the channel. This is notified with an error
            communicationModule.notifyError(new CommError(CommError.Type.IO_CHANNEL_FAILED_READING, e));
            return new StopReadingMessages();
        }
        // todo we should test other forms of loosing connection, like unplugging the cable, shutting down the router, etc
    }

    public void stopped() {
        // the reading process was stopped, inform the comm module
        communicationModule.notifyDisconnected();
    }
}
