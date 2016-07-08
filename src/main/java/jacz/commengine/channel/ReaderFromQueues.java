package jacz.commengine.channel;

import org.aanguita.jacuzzi.queues.event_processing.MessageReader;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * This MessageReader implementation is in charge of reading messages located at individual channel set queues.
 * That is, this will be only used when several threads are employed for handling the incoming messages. This reader
 * will read the messages for one of the synchronized channel sets.
 */
class ReaderFromQueues implements MessageReader {

    /**
     * Message queue from where this reader obtains messages
     */
    private ArrayBlockingQueue<Object> messageQueue;

    /**
     * Class constructor
     *
     * @param messageQueue message queue from where this reader obtains messages
     */
    public ReaderFromQueues(ArrayBlockingQueue<Object> messageQueue) {
        this.messageQueue = messageQueue;
    }

    public Object readMessage() {
        // messages are read from the associated message queue, and returned
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            // nobody can interrupt this thread -> cannot happen
        }
        // cannot reach this
        return null;
    }

    public void stopped() {
        // todo comm module will put a stopReadingMessages
        // nobody except the ChannelModule is going to put a StopReadingMessages message to our reader -> ignore
    }
}
