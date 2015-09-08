package jacz.commengine.channel;

import jacz.commengine.communication.CommError;

import java.util.Set;

/**
 * This interface contains the methods that act as link between the channel module and the client using the channel module. When the channel module
 * receives messages from the other end, it invokes the methods contained here.
 * <p/>
 * The user must provide an implementation of these methods so incoming messages are treated properly.
 * <p/>
 * It is possible for the user to register FSMs in some channels. If a message arrives through a channel with a registered FSM, the message will
 * be redirected to it, avoiding this code. Messages will only arrive here when there are no FSMs registered in the message channel.
 * <p/>
 * Concurrency-related considerations:
 * - The newMessage methods are called by independent threads. The concurrency of these threads depends on how the channel module was set up.
 * - The channelsFreed is also called by an independent thread. Its invocation however will never overlap with any call to newMessage,
 * but will be called by the same thread that made the last call to the corresponding FSM (the one being freed)
 * - disconnected and error methods are called by an independent thread. Only one of them will ever be invoked, and only once. Its invocation
 * will never overlap any other invocation in this interface (including FSM methods), and no other invocations of any kind will follow them
 * - None of these invocations will ever hold the channel module class synchronized
 */
public interface ChannelAction {

    /**
     * This method is invoked when a new object message arrives to the channel module
     *
     * @param ccp     ChannelConnectionPoint associated to this ChannelModule for communicating with the other end
     * @param channel channel through which the message arrived
     * @param message object message that arrived at the ChannelModule
     */
    public void newMessage(ChannelConnectionPoint ccp, byte channel, Object message);

    /**
     * This method is invoked when a new byte array message arrives to the channel module
     *
     * @param ccp     ChannelConnectionPoint associated to this ChannelModule
     * @param channel channel through which the message arrived
     * @param data    array of bytes received by the ChannelModule
     */
    public void newMessage(ChannelConnectionPoint ccp, byte channel, byte[] data);

    /**
     * Method invoked when an FSM concludes execution and frees some channels. At this invocation it is sure that the
     * given channels are free to use again
     * <p/>
     * During the execution of this method, no other thread can access synchronized code of the cpp (for example,
     * to register new FSMs), it is synchronized and occupied by the thread executing this method
     *
     * @param ccp      ChannelConnectionPoint to which the freed channels are associated to
     * @param channels list of channels freed
     */
    public void channelsFreed(ChannelConnectionPoint ccp, Set<Byte> channels);

    /**
     * The Channel module has been disconnected due to this end or the other communication end closing the connection
     *
     * @param ccp      ChannelConnectionPoint to which the disconnected session is associated to
     * @param expected true if the disconnection is provoked by an action initiated by our client, false otherwise
     */
    public void disconnected(ChannelConnectionPoint ccp, boolean expected);

    /**
     * An unexpected exception was raised when reading objects from the socket or writing data to the other end.
     * Communications will be closed, and the channel module will be disconnected
     *
     * @param ccp ChannelConnectionPoint to which the error session is associated to
     * @param e   raised error
     */
    public void error(ChannelConnectionPoint ccp, CommError e);
}