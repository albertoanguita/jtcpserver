package jacz.commengine.channel;

import java.io.Serializable;

/**
 * This class implements an object message that can be sent through a ChannelModule. It includes the channel of transmission and the object sent
 */
class ChannelMessage implements Serializable {

    /**
     * Transmission channel (256 possible values)
     */
    byte canal;

    /**
     * The message itself
     */
    Serializable message;

    ChannelMessage(byte canal, Serializable message) {
        this.canal = canal;
        this.message = message;
    }
}
