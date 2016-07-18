package org.aanguita.jtcpserver.clientserver.client;

import org.aanguita.jtcpserver.communication.CommunicationModule;
import org.aanguita.jtcpserver.tcpconnection.client.TCPClient;
import org.aanguita.jacuzzi.network.IP4Port;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Light client for sending simple, stateless requests to a light server
 */
public class LightClient {

    public static Object sendRequest(IP4Port serverIp4Port, Object request) throws IOException, ClassNotFoundException {
        Socket socket = prepareRequest(serverIp4Port);
        return sendPreparedRequest(socket, request);
    }

    public static byte[] sendRequest(IP4Port serverIp4Port, byte[] request) throws IOException {
        Socket socket = prepareRequest(serverIp4Port);
        return sendPreparedRequest(socket, request);
    }

    public static Socket prepareRequest(IP4Port serverIp4Port) throws IOException {
        return TCPClient.connect(serverIp4Port.getIp(), serverIp4Port.getPort());
    }

    public static Object sendPreparedRequest(Socket serverSocket, Object request) throws IOException, ClassNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(serverSocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(serverSocket.getInputStream());
        oos.writeObject(request);
        Object answer = ois.readObject();
        serverSocket.close();
        return answer;
    }

    public static byte[] sendPreparedRequest(Socket serverSocket, byte[] request) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(serverSocket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(serverSocket.getInputStream());
        CommunicationModule.writeByteArrayToStream(oos, request);
        oos.flush();
        byte[] answer = CommunicationModule.readByteArrayFromStream(ois);
        serverSocket.close();
        return answer;
    }
}
