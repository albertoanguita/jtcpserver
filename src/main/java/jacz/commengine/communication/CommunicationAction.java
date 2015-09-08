package jacz.commengine.communication;

/**
 * Actions invoked by the CommunicationModule upon certain events. This methods must be implemented by the client
 * of the CommunicationModule in order to use it.
 * <p/>
 * Thread-related considerations:
 * - Only one of stopped or error methods will ever be invoked, and only once. This invocation will be carried out by an independent thread.
 * - It is guaranteed that whenever any of the methods of this interface is invoked, the invoking thread will not be holding the communication
 * module mail class in synchronized mode
 */
public interface CommunicationAction {

    /**
     * The comm module has stopped due to this or the other communication end closing the connection. A boolean
     * parameter indicates if the disconnection was due to or end requesting it, or the other end disconnecting
     * from us (either due to an error or due to the other client requesting such disconnection)
     * <p/>
     * A StopReadingMessages will still be sent upwards
     *
     * @param expected indicate if the disconnection was expected (was due to our end requesting such
     *                 disconnection)
     */
    public void stopped(boolean expected);

    /**
     * An unexpected exception was raised when reading objects from the socket, or sending data.
     * Communications will be closed.
     * <p/>
     * A StopReadingMessages will still be sent upwards
     *
     * @param commError raised commError
     */
    public void error(CommError commError);
}
