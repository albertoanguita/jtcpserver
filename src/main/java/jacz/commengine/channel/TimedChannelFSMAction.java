package jacz.commengine.channel;

/**
 * Extension of the classic channel FSMs which adds the timeout feature. At registration time the user can specify a timeout time. If the
 * FSM is inactive during that amount of time (no messages arrive to it), a timeout method is invoked
 * <p/>
 * Concurrency-related considerations:
 * - The timedOut method is invoked by an independent thread from the threads that execute init or processMessage. However, this invocation will
 * never happen simultaneously to those other threads (it will wait until running invocations are done).
 * - The timedOut method invocations does not hold the channel module synchronized
 * - No further invocations will follow this one
 */
public interface TimedChannelFSMAction<T> extends ChannelFSMAction<T> {

    /**
     * This message is invoked when timeout happens (no messages arrived to this FSM for certain amount of time). The FSM will be de-registered
     * from the channel module
     * <p/>
     * This method is invoked by an independent thread
     *
     * @param state the current state of the FSM
     * @param ccp   ChannelConnectionPoint to which this FSM is attached to
     */
    public void timedOut(T state, ChannelConnectionPoint ccp);
}
