package jacz.commengine.channel;

import jacz.commengine.communication.CommError;
import jacz.commengine.communication.CommunicationModule;
import jacz.util.concurrency.task_executor.ParallelTask;
import jacz.util.concurrency.task_executor.SequentialTaskExecutor;
import jacz.util.fsm.GenericFSM;
import jacz.util.fsm.TimedFSM;
import jacz.util.identifier.UniqueIdentifier;
import jacz.util.identifier.UniqueIdentifierFactory;
import jacz.util.io.object_serialization.Serializer;
import jacz.util.queues.event_processing.MessageProcessor;
import jacz.util.queues.event_processing.StopReadingMessages;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * This class offers client applications point-to-point channel-based communications. Communications are established
 * with other end point, and messages can be multiplexed over a byte-sized number of channels. Messages are either
 * serialized objects or byte arrays (a CommunicationModule is used for this, so all its characteristics are
 * inherited in this module). Other features offered by this module are:
 * <ul>
 * <li>
 * Action based handling of incoming messages: the client does not need to create threads for reading incoming
 * messages. He simply has to implement an interface describing how to handle incoming messages, and this module
 * will create the threads for executing them.
 * </li>
 * <li>
 * - Multithreading of channels: channels can be divided into channel sets, each channel set being handled by an
 * independent thread
 * </li>
 * <li>
 * ChannelConnectionPoint class for module handling: the ChannelConnectionPoint class contains the necessary methods
 * for using this module, and can be safely stored in HashMaps, as it uses a UniqueIdentifier for hashing. Clients
 * are recommended to access the ChannelModule through this class only.
 * </li>
 * <li>
 * Channel-based FSMs: clients can register FSMs that take care of incoming messages instead of the interface
 * implementation. These can be dynamically registered, and several FSM can monitor the same channel. Two interfaces
 * are offered for using these FSMs. One is for normal FSMs (its methods include the associated ChannelConnectionPoint
 * so it can be used in the implementations, other than that they behave as normal FSMs), the other includes the
 * timeout feature (timed channel FSMs).
 * </li>
 * </ul>
 * <p/>
 * Additional notes:
 * - Several FSMs are allowed to coexist in the same channel. A message in this channel will be sequentially processed
 * by all FSMs registered with the channel (in order of registering).
 * - It is planned to add priority values to channels in the future.
 */
public class ChannelModule {

    /**
     * This private class stores a pair <queue, message processor>. It is used when there are several concurrent
     * channel sets, for storing the individual concurrent channel set queues together with their message processor.
     * We will use objects of this class as the value in a map relating channels to these.
     * For redirecting messages, we need to be able to relate one channel to its queue, and for pausing and resuming
     * we need to be able to reach the message processor handling it.
     */
    private static class QueueAndMessageProcessor {

        private final UniqueIdentifier id;

        private final ArrayBlockingQueue<Object> queue;

        private final MessageProcessor messageProcessor;

        private QueueAndMessageProcessor(ArrayBlockingQueue<Object> queue, MessageProcessor messageProcessor) {
            id = UniqueIdentifierFactory.getOneStaticIdentifier();
            this.queue = queue;
            this.messageProcessor = messageProcessor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            QueueAndMessageProcessor that = (QueueAndMessageProcessor) o;

            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    /**
     * Maximum capacity of the queues storing incoming messages
     */
    private final static int INCOMING_CAPACITY = 100;

    /**
     * Equitability of the queues storing incoming messages (see reference in class ArrayBlockingQueue)
     */
    private final static boolean INCOMING_FAIRNESS = false;

    /**
     * CommunicationModule employed for the low level data transfer
     */
    private final CommunicationModule commModule;

    /**
     * Actions carried out upon some events (new messages, freeing of channels, etc)
     */
    private final ChannelAction channelAction;

    /**
     * These queues store incoming messages temporarily, until they are properly handled.
     * There is a queue by each concurrent group of channels
     */

    /**
     * Set of channels employed if the all belong to the same concurrent channel set
     */
    private final Set<Byte> uniqueChannelSet;

    /**
     * Table storing all channels employed, only when there are several concurrent channel sets. For each channel,
     * a QueueAndMessageProcessor object is associated. This object stores the channel itself and the MessageProcessor
     * that handles this channel
     */
    private final Map<Byte, QueueAndMessageProcessor> channelQueuesAndMessageProcessors;

    private final Map<Byte, List<GenericFSM<?, Object>>> channelFSMs;

    private final Map<GenericFSM<?, Object>, Set<Byte>> FSMToChannel;

    private final ChannelConnectionPoint channelConnectionPoint;

    /**
     * Message processor employed when we only use a single concurrent channel set
     */
    private final MessageProcessor allChannelProcessor;

    /**
     * This processor is used when we have several concurrent channel sets. It is in charge of redirecting incoming
     * messages to individual queues (one queue per concurrent channel set). We keep this field for sake of clarity
     * and maintainability, although this field is so far not accessed
     */
    @SuppressWarnings({"FieldCanBeLocal"})
    private final MessageProcessor senderToQueues;

    /**
     * This set contains all MessageProcessors defined in this object (both in case of having only one, or several).
     * This allows invoking some actions to all defined processors easily (e.g. start). It also includes the
     * senderToQueues processor, in case it is defined.
     */
    private final Set<MessageProcessor> messageProcessorSet;

    private boolean noMoreFSMRegistrationAccepted = false;

    private final SequentialTaskExecutor sequentialTaskExecutor;

    /**
     * Creates a ChannelModule
     *
     * @param socket             communication socket with the other end
     * @param channelAction      actions to be invoked upon some events
     * @param concurrentChannels sets of channels handled by the same thread. A null value of an empty set
     *                           indicates that all channels are used, by a unique thread
     * @throws java.io.IOException an error establishing the communications
     */
    public ChannelModule(Socket socket, ChannelAction channelAction, Set<Set<Byte>> concurrentChannels) throws IOException {
        this("", socket, channelAction, concurrentChannels);
    }

    /**
     * Creates a ChannelModule with specific ChannelConnectionPoint id
     *
     * @param socket             communication socket with the other end
     * @param channelAction      actions to be invoked upon some events
     * @param concurrentChannels sets of channels handled by the same thread. A null value of an empty set
     *                           indicates that all channels are used, by a unique thread
     * @throws java.io.IOException an error establishing the communications
     */
    public ChannelModule(Socket socket, ChannelAction channelAction, Set<Set<Byte>> concurrentChannels, UniqueIdentifier id) throws IOException {
        this("", socket, channelAction, concurrentChannels, id);
    }

    /**
     * Creates a ChannelModule with specific name
     *
     * @param name               name of this channel module
     * @param socket             communication socket with the other end
     * @param channelAction      actions to be invoked upon some events
     * @param concurrentChannels sets of channels handled by the same thread. A null value of an empty set
     *                           indicates that all channels are used, by a unique thread
     * @throws java.io.IOException an error establishing the communications
     */
    public ChannelModule(String name, Socket socket, ChannelAction channelAction, Set<Set<Byte>> concurrentChannels) throws IOException {
        this(name, socket, channelAction, concurrentChannels, UniqueIdentifierFactory.getOneStaticIdentifier());
    }

    /**
     * Creates a ChannelModule with specific name and ChannelConnectionPoint id
     *
     * @param name               name of this channel module
     * @param socket             communication socket with the other end
     * @param channelAction      actions to be invoked upon some events
     * @param concurrentChannels sets of channels handled by the same thread. A null value of an empty set
     *                           indicates that all channels are used, by a unique thread
     * @throws java.io.IOException an error establishing the communications
     */
    public ChannelModule(String name, Socket socket, ChannelAction channelAction, Set<Set<Byte>> concurrentChannels, UniqueIdentifier id) throws IOException {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // first, the communication module in charge of low level communication, is created
        // then, MessageProcessors for handling incoming messages are created and initialized.
        // There are two possibilities: only one channel set, or multiple channel sets
        //
        // - In case only one channel set is used, only one MessageProcessor is needed, and the allChannelProcessor
        // is employed.
        //
        // - In case several channel sets are used, senderToQueues is created
        // to send incoming messages to specific queues for each channel set, and messageProcessorSet is populated
        // with MessageProcessors for handling the messages of each channel set (in addition, a queue is created
        // for each channel set, and stored in channelQueuesAndMessageProcessors)
        //
        // Finally, a ChannelConnectionPoint associated to this ChannelModule is created, as well as the attributes
        // that will store the used FSMs
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        commModule = new CommunicationModule(name, socket);

        this.channelAction = channelAction;
        messageProcessorSet = new HashSet<>();


        if (concurrentChannels == null) {
            concurrentChannels = new HashSet<>();
        }
        if (concurrentChannels.size() == 0) {
            // add one set with all possible channels
            Set<Byte> allChannels = new HashSet<>();
            byte b = Byte.MIN_VALUE;
            boolean finished = false;
            while (!finished) {
                if (allChannels.contains(b)) {
                    finished = true;
                } else {
                    allChannels.add(b);
                    b++;
                }
            }
            concurrentChannels.add(allChannels);
        }

        if (concurrentChannels.size() == 1) {
            // only one channel set, specific channels are used
            channelQueuesAndMessageProcessors = null;
            senderToQueues = null;
            uniqueChannelSet = new HashSet<>(concurrentChannels.iterator().next());
            allChannelProcessor = new MessageProcessor(name + "/chanMod", new MessageReaderImpl(this, commModule), new MessageHandlerImpl(this), false);
            messageProcessorSet.add(allChannelProcessor);
        } else {
            // several channel sets are used
            uniqueChannelSet = null;
            allChannelProcessor = null;
            channelQueuesAndMessageProcessors = new HashMap<>();

            for (Set<Byte> channelList : concurrentChannels) {
                ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(INCOMING_CAPACITY, INCOMING_FAIRNESS);
                MessageProcessor messageProcessor = new MessageProcessor(name + "/chanMod", new ReaderFromQueues(queue), new MessageHandlerImpl(this), false);
                QueueAndMessageProcessor queueAndMessageProcessor = new QueueAndMessageProcessor(queue, messageProcessor);
                for (Byte oneChannel : channelList) {
                    channelQueuesAndMessageProcessors.put(oneChannel, queueAndMessageProcessor);
                }
                messageProcessorSet.add(messageProcessor);
            }
            senderToQueues = new MessageProcessor(name + "/chanMod", new MessageReaderImpl(this, commModule), new SenderToQueues(this), false);
            messageProcessorSet.add(senderToQueues);
        }
        channelConnectionPoint = new ChannelConnectionPoint(this, id);
        channelFSMs = new HashMap<>();
        FSMToChannel = new HashMap<>();
        sequentialTaskExecutor = new SequentialTaskExecutor();
    }

    /**
     * Starts processing incoming messages
     */
    public void start() {
        for (MessageProcessor messageProcessor : messageProcessorSet) {
            messageProcessor.start();
        }
    }

    /**
     * Pauses a set of concurrent channels. It this set was already paused, nothing will happen.
     *
     * @param channel one of the channels of the concurrent set to be paused.
     * @throws IndexOutOfBoundsException if the channel is not supported in this ChannelModule
     */
    public void pause(byte channel) throws IndexOutOfBoundsException {
        // if only one thread is used for all channels, it is easy: simply pause that message processor completely.
        // If we are using several threads, then the main message processor for delivering messages to specific
        // queues must be left running (so no other concurrent channel sets are affected). We will only pause the
        // message processor accessing the involved queue.
        if (allChannelProcessor != null) {
            if (uniqueChannelSet.contains(channel)) {
                allChannelProcessor.pause();
            } else {
                throw new IndexOutOfBoundsException("Channel is not supported in this ChannelModule: " + channel);
            }
        } else {
            if (channelQueuesAndMessageProcessors.containsKey(channel)) {
                channelQueuesAndMessageProcessors.get(channel).messageProcessor.pause();
            } else {
                throw new IndexOutOfBoundsException("Channel is not supported in this ChannelModule: " + channel);
            }
        }
    }

    /**
     * Resumes a set of concurrent channels. It this set was already running, nothing will happen.
     *
     * @param channel one of the channels of the concurrent set to be resumed.
     * @throws IndexOutOfBoundsException if the channel is not supported in this ChannelModule
     */
    public void resume(byte channel) {
        // this is equivalent to the previous pause method, but resuming. The structure of the code is identical.
        if (allChannelProcessor != null) {
            if (uniqueChannelSet.contains(channel)) {
                allChannelProcessor.resume();
            } else {
                throw new IndexOutOfBoundsException("Channel is not supported in this ChannelModule: " + channel);
            }
        } else {
            if (channelQueuesAndMessageProcessors.containsKey(channel)) {
                channelQueuesAndMessageProcessors.get(channel).messageProcessor.resume();
            } else {
                throw new IndexOutOfBoundsException("Channel is not supported in this ChannelModule: " + channel);
            }
        }
    }

    /**
     * Retrieves the associated ChannelConnectionPoint object
     *
     * @return the associated ChannelConnectionPoint object
     */
    public ChannelConnectionPoint getChannelConnectionPoint() {
        return channelConnectionPoint;
    }

    /**
     * Disconnects this ChannelModule from the other connection point. This will eventually provoke the invocation
     * of the disconnected method in the associated ChannelAction
     */
    public void disconnect() {
        commModule.disconnect();
    }

    /**
     * Indicates that the message reader implementation that gets messages from the commModule has been disconnected
     * (either due to the CommunicationModule being disconnected as well or raising an error)
     * <p/>
     * Apart of this method, the disconnected or error actions in the CommunicationActionImpl will be respectively invoked, so
     * in this method we will only take care of stopping all message processors in this ChannelModule
     */
    void messageReaderStopped() {
        // first stop the channel module and notify the disconnection or the error to the client
        // if we are working with one MessageProcessor (all channels monitored by a single thread) then we don't need
        // to perform any additional action, because that MessageProcessor will die itself.
        // However, if we have additional MessageProcessors for different channel sets, these must be terminated
        // (by giving them a StopReadingMessages
        stopChannelModule();
        if (!commModule.isError()) {
            channelActionDisconnected(channelConnectionPoint, commModule.isManuallyDisconnected());
        } else {
            channelActionError(channelConnectionPoint, commModule.getError());
        }

        if (channelQueuesAndMessageProcessors != null) {
            // set with all different queue and message processors (so we only send one message to each queue)
            Set<QueueAndMessageProcessor> allQueueAndMessageProcessors = new HashSet<>(channelQueuesAndMessageProcessors.values());
            for (QueueAndMessageProcessor queueAndMessageProcessor : allQueueAndMessageProcessors) {
                try {
                    queueAndMessageProcessor.queue.put(new StopReadingMessages());
                    queueAndMessageProcessor.messageProcessor.resume();
                } catch (InterruptedException e) {
                    // ignore this exception
                }
            }
        }
    }

    private void stopChannelModule() {
        synchronized (this) {
            noMoreFSMRegistrationAccepted = true;
        }
        detachAllFSMs();
    }

    /**
     * Writes an object message through a given channel
     *
     * @param channel the channel through which the message is to be sent
     * @param message the message to send
     */
    void write(byte channel, Object message, boolean flush) {
        // the message is sent as a unique ChannelMessage object
        commModule.write(new ChannelMessage(channel, message), flush);
    }

    /**
     * Writes an array of bytes through a given channel
     *
     * @param channel the channel through which the data is to be sent
     * @param data    the data to send
     */
    void write(byte channel, byte[] data, boolean flush) {
        // the message is sent as a unique array of bytes, with the channel and the data together
        byte[] channelArray = Serializer.serialize(channel);
        byte[] channelAndData = Serializer.addArrays(channelArray, data);
        commModule.write(channelAndData, flush);
    }

    void flush() {
        commModule.flush();
    }

    /**
     * Handles an incoming message, submitting it to registered FSMs if necessary
     *
     * @param message the message to handle
     */
    void handleIncomingMessage(Object message) {
        // the received message can either be a ByteArrayWrapperChannel or a ChannelMessage, but the behaviour to
        // follow is quite similar. The difference is in the way the channel is obtained, and the way the message
        // is processed in the absence of FSMs.
        // this method does not need any synchronizing since no attributes are modified, only read
        boolean isByteArray;
        byte channel;
        if (message instanceof ByteArrayWrapperChannel) {
            isByteArray = true;
            channel = ((ByteArrayWrapperChannel) message).getChannel();
        } else {
            isByteArray = false;
            channel = ((ChannelMessage) message).canal;
        }
        // synchronize this section so nobody can register an FSM while this is running -> this allows
        // the final action of an FSM to state that a channel is free
        // in addition, this allows to complete the init of an FSM before it receives any message
        List<GenericFSM<?, Object>> fsmList = null;
        synchronized (this) {
            if (channelFSMs.containsKey(channel)) {
                // copy in another list to avoid concurrency exceptions when detaching (in the detach operation we
                // eliminate the FSM from the FSM list itself)
                fsmList = new ArrayList<>(channelFSMs.get(channel));
            }
        }
        if (fsmList != null) {
            // FSM
            for (GenericFSM<?, Object> fsm : fsmList) {
                if (!fsm.newInput(message)) {
                    // detach this GenericFSM
                    detachFSM(fsm, false);
                }
            }
        } else {
            // in the absence of any FSM monitoring the channel, simply send this message to the ChannelAction implementation.
            if (isByteArray) {
                channelActionNewMessage(channelConnectionPoint, channel, ((ByteArrayWrapperChannel) message).getData());
            } else {
                channelActionNewMessage(channelConnectionPoint, channel, ((ChannelMessage) message).message);
            }
        }
    }

    /**
     * Adds an incoming message to the queue corresponding to its synchronized channel set (only used when there
     * are several threads for handling different channel sets)
     *
     * @param channel the channel through which the message was received
     * @param message the message itself
     */
    void addMessageToChannelQueue(byte channel, Object message) {
        // if this channel is not registered, ignore the message.
        // if the channel is ok, put it in the corresponding queue
        try {
            if (channelQueuesAndMessageProcessors.containsKey(channel)) {
                channelQueuesAndMessageProcessors.get(channel).queue.put(message);
            }
        } catch (InterruptedException e) {
            // ignore -> never invoked
        }
    }

    /**
     * Checks whether a given channels has any FSM monitoring it
     *
     * @param channel the channel to check
     * @return true if there is at least one FSM monitoring the given channel, false otherwise
     */
    synchronized boolean isChannelRegistered(byte channel) {
        return channelFSMs.containsKey(channel);
    }

    /**
     * Registers a new ChannelFSM in this ChannelModule
     *
     * @param channelFSMAction the actions of the ChannelFSM to register
     * @param name             name of the new GenericFSM
     * @param involvedChannels the channels that the given FSM will monitor
     * @param <T>              the type of the FMS states
     * @throws IllegalArgumentException if any of the following occurs:
     *                                  - No channels are given (null or empty set)
     *                                  - Any of the given channels is not supported in this ChannelModule
     *                                  - Two of the given channels belong to different handling threads
     */
    <T> void registerNewFSM(ChannelFSMAction<T> channelFSMAction, String name, Set<Byte> involvedChannels) throws IllegalArgumentException {
        boolean canRegister;
        synchronized (this) {
            canRegister = !noMoreFSMRegistrationAccepted;
        }
        if (canRegister) {
            ChannelFSM<T> channelFSM = new ChannelFSM<>(channelFSMAction, channelConnectionPoint);
            GenericFSM<T, Object> genericFSM = new GenericFSM<>(name, channelFSM);
            registerFSM(genericFSM, involvedChannels);
        }
    }

    /**
     * Registers a new timed ChannelFSM in this ChannelModule
     *
     * @param timedChannelFSMAction the actions of the timed ChannelFSM to register
     * @param timeoutMillis         the timeout for this timed ChannelFSM (in millis)
     * @param name                  name of the new TimedFSM
     * @param involvedChannels      the channels that the given FSM will monitor
     * @param <T>                   the type of the FMS states
     * @throws IllegalArgumentException if any of the following occurs:
     *                                  - No channels are given (null or empty set)
     *                                  - Any of the given channels is not supported in this ChannelModule
     *                                  - Two of the given channels belong to different handling threads
     */
    <T> void registerNewFSM(TimedChannelFSMAction<T> timedChannelFSMAction, long timeoutMillis, String name, Set<Byte> involvedChannels) throws IllegalArgumentException {
        boolean canRegister;
        synchronized (this) {
            canRegister = !noMoreFSMRegistrationAccepted;
        }
        if (canRegister) {
            TimedChannelFSM<T> timedChannelFSM = new TimedChannelFSM<>(this, timedChannelFSMAction, channelConnectionPoint);
            TimedFSM<T, Object> timedFSM = new TimedFSM<>(name, timedChannelFSM, timeoutMillis);
            timedChannelFSM.setGenericFSM(timedFSM);
            registerFSM(timedFSM, involvedChannels);
        }
    }

    /**
     * @param genericFSM       the FSM to register in this ChannelModule
     * @param involvedChannels the channels that this FSM will be monitoring. These must be supported by this
     *                         ChannelModule, and they all must be synchronized with each other (one thread
     *                         handling them)
     * @param <T>              the type of the FSM states
     * @throws IllegalArgumentException if any of the following occurs:
     *                                  - No channels are given (null or empty set)
     *                                  - Any of the given channels is not supported in this ChannelModule
     *                                  - Two of the given channels belong to different handling threads
     */
    private <T> void registerFSM(GenericFSM<T, Object> genericFSM, Set<Byte> involvedChannels) throws IllegalArgumentException {
        // check channels are supported and synchronized
        if (involvedChannels == null || involvedChannels.size() == 0) {
            throw new IllegalArgumentException("At least one channel required");
        }
        // channel queue associated to the given channels
        ArrayBlockingQueue channelQueue = null;
        for (Byte oneChannel : involvedChannels) {
            if (uniqueChannelSet != null) {
                if (!uniqueChannelSet.contains(oneChannel)) {
                    throw new IllegalArgumentException("Channel " + oneChannel + " is not supported in this channel module");
                }
            } else {
                if (!channelQueuesAndMessageProcessors.containsKey(oneChannel)) {
                    throw new IllegalArgumentException("Channel " + oneChannel + " is not supported in this channel module");
                }
                if (channelQueue == null) {
                    channelQueue = channelQueuesAndMessageProcessors.get(oneChannel).queue;
                } else if (channelQueue != channelQueuesAndMessageProcessors.get(oneChannel).queue) {
                    throw new IllegalArgumentException("Channels must be synchronized (channel " + oneChannel + "");
                }
            }
        }
        // correct channels -> register them with the new FSM, and stored the already received messages
        synchronized (this) {
            for (Byte oneChannel : involvedChannels) {
                if (!channelFSMs.containsKey(oneChannel)) {
                    channelFSMs.put(oneChannel, new ArrayList<GenericFSM<?, Object>>(1));
                }
                channelFSMs.get(oneChannel).add(genericFSM);
                //channelFSMs.put(oneChannel, genericFSM);
            }
            Set<Byte> involvedChannelsCopy = new HashSet<>(involvedChannels);
            FSMToChannel.put(genericFSM, involvedChannelsCopy);
        }
        // once we checked everything is correct, start the received FSM (it should not be already started)
        // if after started the FSM is no longer active, detach
        if (!genericFSM.start()) {
            detachFSM(genericFSM, false);
        }
    }

    /**
     * Detaches a given FSM from this ChannelModule
     *
     * @param genericFSM the FSM to detach
     */
    private void detachFSM(GenericFSM<?, Object> genericFSM, boolean issueDisconnection) {
        // the received genericFSM is no longer used, so it is eliminated from the active FSM lists. First we check
        // that this FSM is actually active in this ChannelModule (otherwise, ignore)
        // The channels which no longer have any FSM associated are notified to be free
        Set<Byte> freedChannels;
        synchronized (this) {
            if (!FSMToChannel.containsKey(genericFSM)) {
                return;
            }
            if (issueDisconnection) {
                // manually stop the timer so that the disconnected event is raised
                genericFSM.stop();
            }
            freedChannels = new HashSet<>();
            for (Byte oneChannel : FSMToChannel.get(genericFSM)) {
                channelFSMs.get(oneChannel).remove(genericFSM);
                if (channelFSMs.get(oneChannel).size() == 0) {
                    channelFSMs.remove(oneChannel);
                    freedChannels.add(oneChannel);
                }
            }
            FSMToChannel.remove(genericFSM);
        }
        if (!freedChannels.isEmpty()) {
            channelActionChannelsFreed(channelConnectionPoint, freedChannels);
        }
    }

    /**
     * Detaches all currently active FSMs in this ChannelModule. This method is called upon disconnection or error, so no more input will be
     * received
     */
    private void detachAllFSMs() {
        // the FSMs must be first copied in a secondary array, since the FSMToChannel map itself is going to be
        // modified during the process
        Set<GenericFSM<?, Object>> fsmToRemoveSet;
        synchronized (this) {
            fsmToRemoveSet = new HashSet<>(FSMToChannel.keySet());
        }
        for (GenericFSM<?, Object> fsm : fsmToRemoveSet) {
            detachFSM(fsm, true);
        }
    }

    <T> void FSMTimedOut(GenericFSM<?, Object> genericFSM, TimedChannelFSMAction<T> timedChannelFSMAction, T state) {
        // if the FSM was already detached (disconnection, completion, etc), we don't have a concurrency controller for it
        detachFSM(genericFSM, false);
        timedChannelFSMAction.timedOut(state, channelConnectionPoint);
    }

    private void channelActionNewMessage(final ChannelConnectionPoint ccp, final byte channel, final Object message) {
        sequentialTaskExecutor.executeTask(new ParallelTask() {
            @Override
            public void performTask() {
                channelAction.newMessage(ccp, channel, message);
            }
        });
    }

    private void channelActionNewMessage(final ChannelConnectionPoint ccp, final byte channel, final byte[] data) {
        sequentialTaskExecutor.executeTask(new ParallelTask() {
            @Override
            public void performTask() {
                channelAction.newMessage(ccp, channel, data);
            }
        });
    }

    public void channelActionChannelsFreed(final ChannelConnectionPoint ccp, final Set<Byte> channels) {
        sequentialTaskExecutor.executeTask(new ParallelTask() {
            @Override
            public void performTask() {
                channelAction.channelsFreed(ccp, channels);
            }
        });
    }

    public void channelActionDisconnected(final ChannelConnectionPoint ccp, final boolean expected) {
        sequentialTaskExecutor.executeTask(new ParallelTask() {
            @Override
            public void performTask() {
                channelAction.disconnected(ccp, expected);
            }
        });
    }

    public void channelActionError(final ChannelConnectionPoint ccp, final CommError e) {
        sequentialTaskExecutor.executeTask(new ParallelTask() {
            @Override
            public void performTask() {
                channelAction.error(ccp, e);
            }
        });
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stopChannelModule();
    }
}
