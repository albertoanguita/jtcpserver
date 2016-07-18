package org.aanguita.jtcpserver.clientserver.test;

import org.aanguita.jtcpserver.clientserver.client.LightClient;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Opens a hole in the NAT to a specific server and port, and returns the local port.
 * <p/>
 * Sends the local address and port to the server
 */
public class OpenHole {

    public static int openHole(String ip, int port, String localAddress, int localPort) throws Exception {
//        Socket serverSocket = LightClient.prepareRequest(new IP4Port(ip, port));
//        Socket serverSocket = new Socket(ip, port, null, localPort);
        Socket serverSocket = new Socket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(localAddress, localPort));
        System.out.println("Open hole from " + localPort);
        serverSocket.connect(new InetSocketAddress(ip, port));
        int generatedLocalPort = serverSocket.getLocalPort();
        String data = serverSocket.getLocalAddress().getHostAddress() + ":" + generatedLocalPort;
        LightClient.sendPreparedRequest(serverSocket, data);
        return generatedLocalPort;
    }
}
