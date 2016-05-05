package jacz.commengine.channel;

/**
 * This interface contains the methods for FSMs to work with a channel module.
 * <p/>
 * A channel FSM is a finite state machine designed to work over a channel multiplexed link. Inputs for the FSM are the messages received
 * from the other end.
 * <p/>
 * The states of the FSM are generic (T)
 * <p/>
 * Concurrency-related considerations:
 * - The init method is always the first one invoked, by the same thread that registers the FSM. This thread does not maintain the channel module
 * synchronized. No other methods will be invoked until the init method has concluded
 * - Both processMessage methods can be called concurrently, by independent threads (this depends on how the concurrency of the channel module has
 * been set up). These do not hold the channel module synchronized either
 * - The isFinalState is invoked after the invocation of a processMessage method, by the same thread
 */
public interface ChannelFSMAction<T> {

    /**
     * This method is invoked when an object message arrives from the other end to this FSM
     *
     * @param currentState current state of the FSM
     * @param channel      channel by which the message arrives
     * @param message      the actual object message
     * @param ccp          channel connection point object that allows sending information to the other end
     * @return the new state of the FSM
     * @throws IllegalArgumentException if the state is not possible
     */
    T processMessage(T currentState, byte channel, Object message, ChannelConnectionPoint ccp) throws IllegalArgumentException;

    /**
     * This method is invoked when a byte array message arrives from the other end to this FSM
     *
     * @param currentState current state of the FSM
     * @param channel      channel by which the message arrives
     * @param data         the actual byte array message
     * @param ccp          channel connection point object that allows sending information to the other end
     * @return the new state of the FSM
     * @throws IllegalArgumentException if the state is not possible
     */
    T processMessage(T currentState, byte channel, byte[] data, ChannelConnectionPoint ccp) throws IllegalArgumentException;

    /**
     * This method is invoked before any other methods, just one time, for the FSM to initialize itself
     * <p/>
     * The invocation of this call is done by the thread that registered the FSM. That thread will NOT maintain the channel module in synchronized
     * mode during this invocation. No processMessage methods will be invoked until this method has completely finished
     *
     * @param ccp channel connection point object that allows sending information to the other end
     * @return the initial state of the FSM
     */
    T init(ChannelConnectionPoint ccp);

    /**
     * This method tells if a specific state is final or not
     *
     * @param state the state being queried
     * @param ccp   channel connection point object that allows sending information to the other end
     * @return true of the state is final, false otherwise
     */
    boolean isFinalState(T state, ChannelConnectionPoint ccp);

    /**
     * This method is called when we got disconnected from the other peer (it does not matter who disconnected) so the FSM has been stopped
     */
    void disconnected(ChannelConnectionPoint ccp);
}
