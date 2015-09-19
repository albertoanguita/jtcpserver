package jacz.commengine.clientserver.server;

import jacz.commengine.communication.CommunicationModule;
import jacz.commengine.tcpconnection.server.TCPServer;
import jacz.commengine.tcpconnection.server.TCPServerAction;
import jacz.util.concurrency.task_executor.ParallelTask;
import jacz.util.concurrency.task_executor.ParallelTaskExecutor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * Class for a lightweight, stateless server. This server is capable of handling simple requests (such as a REST server) without maintaining
 * connection with clients. Clients send their parameters with the request, and the server provides an answer to the request
 * <p/>
 * The LightServer uses a TCPServer for implementing the server
 * <p/>
 */
public class LightServer implements TCPServerAction {

    /**
     * When the light server method returns an exception, an object of this class is returned containing the thrown exception
     */
    public static class LightServerException implements Serializable {

        public final Exception e;

        private LightServerException(Exception e) {
            this.e = e;
        }
    }

    private static class ParallelRequestAttender implements ParallelTask {

        private final Socket clientSocket;

        private final LightServerActionObject lightServerActionObject;

        private final LightServerActionByteArray lightServerActionByteArray;

        private final boolean isObjectRequest;

        private ParallelRequestAttender(Socket clientSocket, LightServerActionObject lightServerActionObject, LightServerActionByteArray lightServerActionByteArray, boolean isObjectRequest) {
            this.clientSocket = clientSocket;
            this.lightServerActionObject = lightServerActionObject;
            this.lightServerActionByteArray = lightServerActionByteArray;
            this.isObjectRequest = isObjectRequest;
        }

        @Override
        public void performTask() {
            LightServer.attendRequest(clientSocket, lightServerActionObject, lightServerActionByteArray, isObjectRequest);
        }
    }

    private final LightServerActionObject lightServerActionObject;

    private final LightServerActionByteArray lightServerActionByteArray;

    private final boolean isObjectRequest;

    private final boolean parallelRequests;

    private final TCPServer tcpServer;

    public LightServer(int port, LightServerActionObject lightServerActionObject, boolean parallelRequests) {
        this(port, lightServerActionObject, null, true, parallelRequests);
    }

    public LightServer(int port, LightServerActionByteArray lightServerActionByteArray, boolean parallelRequests) {
        this(port, null, lightServerActionByteArray, false, parallelRequests);
    }

    private LightServer(int port, LightServerActionObject lightServerActionObject, LightServerActionByteArray lightServerActionByteArray, boolean isObjectRequest, boolean parallelRequests) {
        this.lightServerActionObject = lightServerActionObject;
        this.lightServerActionByteArray = lightServerActionByteArray;
        this.isObjectRequest = isObjectRequest;
        this.parallelRequests = parallelRequests;
        tcpServer = new TCPServer(port, this);
    }

    /**
     * Starts the server. Clients can now connect to this server
     */
    public void start() {
        tcpServer.startServer();
    }

    /**
     * Stops the connection listening service (but connected clients remain as before). Is is guaranteed that after the invocation of this method,
     * no more clients will connect (even ongoing connections). The server can be started again
     */
    public void stop() {
        tcpServer.stopServer();
    }


    @Override
    public void processNewConnection(Socket clientSocket) {
        if (!parallelRequests) {
            attendRequest(clientSocket, lightServerActionObject, lightServerActionByteArray, isObjectRequest);
        } else {
            ParallelTaskExecutor.executeTask(new ParallelRequestAttender(clientSocket, lightServerActionObject, lightServerActionByteArray, isObjectRequest), "LightServer");
        }
    }


    private static void attendRequest(Socket clientSocket, LightServerActionObject lightServerActionObject, LightServerActionByteArray lightServerActionByteArray, boolean isObjectRequest) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            if (isObjectRequest) {
                Object clientMessage = ois.readObject();
                Object response = null;
                try {
                    response = lightServerActionObject.newClientRequest(clientSocket, clientMessage);
                } catch (Exception e) {
                    oos.writeObject(new LightServerException(e));
                }
                if (response != null) {
                    oos.writeObject(response);
                }
            } else {
                byte[] data = CommunicationModule.readByteArrayFromStream(ois);
                byte[] response = null;
                try {
                    response = lightServerActionByteArray.newClientConnection(clientSocket, data);
                } catch (Exception e) {
                    oos.writeObject(new LightServerException(e));
                }
                if (response != null) {
                    CommunicationModule.writeByteArrayToStream(oos, response);
                }
                oos.flush();
            }
        } catch (IOException e) {
            // the communication channel could not be correctly created. Ignore, since we don't want to maintain communication with the client
        } catch (ClassNotFoundException e) {
            try {
                oos.writeObject(new LightServerException(e));
            } catch (Exception e1) {
                // ignore
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // ignore, we don't care at this point
            }
        }
    }

    @Override
    public void error(Exception e) {
        if (lightServerActionObject != null) {
            lightServerActionObject.TCPServerError(e);
        } else {
            lightServerActionByteArray.TCPServerError(e);
        }
    }
}
