package org.aanguita.jtcpserver.clientserver.test.lightserver;

import org.aanguita.jtcpserver.clientserver.server.LightServerActionByteArray;
import org.aanguita.jacuzzi.io.serialization.MutableOffset;
import org.aanguita.jacuzzi.io.serialization.Serializer;

import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 13/02/14
 * Time: 0:16
 * To change this template use File | Settings | File Templates.
 */
public class LightServerActionByteArrayImpl implements LightServerActionByteArray {

    @Override
    public byte[] newClientConnection(Socket clientSocket, byte[] data) throws Exception {
        String s = Serializer.deserializeString(data, new MutableOffset());
        return Serializer.serialize(s + " hello");
    }

    @Override
    public void TCPServerError(Exception e) {
        e.printStackTrace();
    }
}
