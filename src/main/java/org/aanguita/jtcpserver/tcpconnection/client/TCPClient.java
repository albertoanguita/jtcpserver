package org.aanguita.jtcpserver.tcpconnection.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This class implements a method for connecting to a TCP server. The method is static, as no data is stored in its objects
 */
public class TCPClient {

    /**
     * Attempts to connect to an open TCP server
     *
     * @param ip   ip address of the server
     * @param port port of the server
     * @return the socket for communicating with the other communication end
     * @throws java.io.IOException           there was an IO error when attempting to open the communication channel with the TCP server
     * @throws java.net.UnknownHostException the host could not be found
     */
    @SuppressWarnings("DuplicateThrows")
    public static Socket connect(String ip, int port) throws UnknownHostException, IOException {
        return new Socket(ip, port);
    }

    /**
     * Attempts to connect to an open TCP server
     *
     * @param ip   ip address of the server
     * @param port port of the server
     * @return the socket for communicating with the other communication end
     * @throws java.io.IOException           there was an IO error when attempting to open the communication channel with the TCP server
     * @throws java.net.UnknownHostException the host could not be found
     */
    @SuppressWarnings("DuplicateThrows")
    public static Socket connect(String ip, int port, int timeout) throws UnknownHostException, IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), timeout);
        return socket;
    }
}
