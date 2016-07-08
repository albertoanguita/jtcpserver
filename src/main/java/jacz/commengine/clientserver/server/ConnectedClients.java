package jacz.commengine.clientserver.server;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.channel.ChannelModule;
import org.aanguita.jacuzzi.network.IP4Port;

import java.util.*;

/**
 * This class handles the set of connected clients, storing the needed info of each of them (ip, port, ccp) and handling the access to this data
 * <p/>
 * Synchronization is not implemented at this level, so accessions to this class should be properly synchronized
 */
class ConnectedClients {

    /**
     * Data stored about each connected client (ip, port, ccp)
     */
    private static class ClientInfo {

        /**
         * Connection point used to send messages to the other point
         */
        private final ChannelConnectionPoint channelConnectionPoint;

        /**
         * Port of the client
         */
        private final IP4Port ip4Port;

        /**
         * Class constructor
         *
         * @param channelConnectionPoint channel connection point corresponding to this client (for extracting the ccp)
         * @param ip                     ip address of the client
         * @param port                   port of the client
         */
        private ClientInfo(ChannelConnectionPoint channelConnectionPoint, String ip, int port) {
            this.channelConnectionPoint = channelConnectionPoint;
            ip4Port = new IP4Port(ip, port);
        }
    }

    /**
     * Table with all stored clients
     */
    private final Map<String, ClientInfo> clients;

    /**
     * Default class constructor
     */
    public ConnectedClients() {
        clients = new HashMap<>();
    }

    /**
     * Checks if a specific client exists
     *
     * @param id id of the client to check
     * @return true if the specified client exists, false otherwise
     */
    public synchronized boolean existsClient(String id) {
        return clients.containsKey(id);
    }

    /**
     * Retrieves the number of connected clients
     *
     * @return the number of connected clients
     */
    public synchronized int getConnectedClientsCount() {
        return clients.size();
    }

    /**
     * Retrieves the client ids of the currently connected clients
     *
     * @return a set with the ids of the currently connected clients
     */
    public synchronized Set<String> getClientIDs() {
        return new HashSet<>(clients.keySet());
    }

    /**
     * Retrieves the currently connected client ids except for some specific values
     *
     * @param clientsNot set of ids to exclude from the final result
     * @return collection of connected client ids, excluding the values in clientsNot
     */
    public synchronized Set<String> getClientIDsExcept(Collection<String> clientsNot) {
        Set<String> clientIDs = new HashSet<>(clients.keySet());
        clientIDs.removeAll(clientsNot);
        return clientIDs;
    }

    /**
     * Retrieves the currently connected client ids except for some specific values
     *
     * @param clientsNot list of ids to exclude from the final result
     * @return list of connected client ids, excluding the values in clientsNot
     */
    public synchronized Set<String> getClientIDsExcept(String... clientsNot) {
        Collection<String> clientsNotSet = new HashSet<>(Arrays.asList(clientsNot));
        return getClientIDsExcept(clientsNotSet);
    }

    /**
     * Retrieves the channel connection point of a connected client
     *
     * @param clientID id of the client
     * @return the channel connection point of the requested client
     */
    public synchronized ChannelConnectionPoint getCCP(String clientID) {
        return existsClient(clientID) ? clients.get(clientID).channelConnectionPoint : null;
    }

    /**
     * Retrieves the ip and port of a connected client
     *
     * @param clientID id of the client
     * @return the IP4Port object of the requested client
     */
    public synchronized IP4Port getClientIP4Port(String clientID) {
        return existsClient(clientID) ? clients.get(clientID).ip4Port : null;
    }

    /**
     * adds a new client to the list of connected clients (does not check if this client already existed)
     *
     * @param channelModule channel module for this new client
     * @param ip            ip address of the client
     * @param port          port of the client
     */
    public synchronized void addClient(ChannelModule channelModule, String ip, int port) {
        ChannelConnectionPoint ccp = channelModule.getChannelConnectionPoint();
        clients.put(ccp.getId(), new ClientInfo(ccp, ip, port));
    }

    /**
     * Removes an existing client
     *
     * @param clientID id of the client to remove
     */
    public synchronized void removeClient(String clientID) {
        clients.remove(clientID);
    }
}
