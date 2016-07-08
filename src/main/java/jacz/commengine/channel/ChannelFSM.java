package jacz.commengine.channel;

import jacz.util.fsm.GenericFSMAction;

/**
 * This class implements a channel FSM through the interface of a generic FSM. Actions are delegated to a ChannelFSMAction object
 */
public class ChannelFSM<T> implements GenericFSMAction<T, Object> {

    /**
     * Actions to be performed by the FSM
     */
    private ChannelFSMAction<T> channelFSMAction;

    /**
     * ChannelConnectionPoint object available in the actions performed by the ChannelFSMAction
     */
    private ChannelConnectionPoint ccp;

    /**
     * Class constructor
     *
     * @param channelFSMAction actions to be performed by the FSM
     * @param ccp              ChannelConnectionPoint object available in the actions performed by the ChannelFSMAction
     */
    public ChannelFSM(ChannelFSMAction<T> channelFSMAction, ChannelConnectionPoint ccp) {
        this.channelFSMAction = channelFSMAction;
        this.ccp = ccp;
    }

    public T processInput(T state, Object msg) throws IllegalArgumentException {
        if (msg instanceof ByteArrayWrapperChannel) {
            return channelFSMAction.processMessage(state, ((ByteArrayWrapperChannel) msg).getChannel(), ((ByteArrayWrapperChannel) msg).getData(), ccp);
        } else {
            return channelFSMAction.processMessage(state, ((ChannelMessage) msg).canal, ((ChannelMessage) msg).message, ccp);
        }
    }

    public T init() {
        return channelFSMAction.init(ccp);
    }

    public boolean isFinalState(T state) {
        return channelFSMAction.isFinalState(state, ccp);
    }

    @Override
    public void stopped() {
        // this FSM has been stopped due to the disconnection of this channel module -> notify
        channelFSMAction.disconnected(ccp);
    }

    @Override
    public void raisedUnhandledException(Exception e) {
        channelFSMAction.raisedUnhandledException(e, ccp);
    }
}
