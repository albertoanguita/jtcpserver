package org.aanguita.jtcpserver.clientserver.test.lightserver;

import org.aanguita.jtcpserver.clientserver.server.LightServerActionObject;

import java.io.Serializable;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 13/02/14
 * Time: 0:11
 * To change this template use File | Settings | File Templates.
 */
public class LightServerActionObjectImpl implements LightServerActionObject {

    @Override
    public Serializable newClientRequest(Socket clientSocket, Object object) throws Exception {
        String s = (String) object;
        if (s.isEmpty()) {
            throw new Exception("bad");
        }
        return s + " hello";
    }

    @Override
    public void TCPServerError(Exception e) {
        e.printStackTrace();
    }
}
