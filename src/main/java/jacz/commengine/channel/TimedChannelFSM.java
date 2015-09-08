package jacz.commengine.channel;

import jacz.util.fsm.GenericFSM;
import jacz.util.fsm.TimedFSMAction;

/**
 * This class implements the actions for a TimedFSM. The actual implementation is delegated to a TimedChannelFSMAction,
 * so in the end this class implements a timed FSM at the channel level.
 * <p/>
 * A timed channel FSM is similar to a channel FSM, but with the timeout functionality.
 */
public class TimedChannelFSM<T> extends ChannelFSM<T> implements TimedFSMAction<T, Object> {

    private ChannelModule channelModule;

    /**
     * Actions invoked upon the events of the timed FSM
     */
    private TimedChannelFSMAction<T> timedChannelFSMAction;

    /**
     * This object is needed in the timeout event, to be able to access the corresponding pausable element
     */
    private GenericFSM<?, Object> genericFSM;

    /**
     * Class constructor
     *
     * @param channelModule         the channel module for which this TimedChannelFSM works
     * @param timedChannelFSMAction actions invoked upon the events of the timed FSM
     * @param ccp                   ChannelConnectionPoint associated to this timed channel FSM
     */
    public TimedChannelFSM(ChannelModule channelModule, TimedChannelFSMAction<T> timedChannelFSMAction, ChannelConnectionPoint ccp) {
        super(timedChannelFSMAction, ccp);
        this.channelModule = channelModule;
        this.timedChannelFSMAction = timedChannelFSMAction;
    }

    public void setGenericFSM(GenericFSM<?, Object> genericFSM) {
        this.genericFSM = genericFSM;
    }

    @Override
    public void timedOut(T state) {
        channelModule.FSMTimedOut(genericFSM, timedChannelFSMAction, state);
    }
}
