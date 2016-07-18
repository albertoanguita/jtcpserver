package org.aanguita.jtcpserver.clientserver.test;

import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.clientserver.server.ServerAction;
import org.aanguita.jtcpserver.clientserver.server.ServerModule;
import org.aanguita.jtcpserver.communication.CommError;
import org.aanguita.jacuzzi.network.IP4Port;

import java.io.IOException;

/**
 * Class description
 * <p/>
 * User: Alberto<br>
 * Date: 10-ene-2009<br>
 * Last Modified: 10-ene-2009
 */
class ServerActionImpl1 implements ServerAction {

    private ServerModule serverModule;

    public ServerActionImpl1() {
    }

    public void setServerModule(ServerModule serverModule) {
        this.serverModule = serverModule;
    }

    public void newMessage(String idCliente, ChannelConnectionPoint ccp, byte canal, Object mensaje) {
        System.out.println("Server1 - mensaje recibido de cliente " + idCliente + " por canal " + canal + ": " + mensaje);
        try {
            //serverModule.writeAll((byte) (canal + 1), new NotSerClass());
            serverModule.writeAll((byte) (canal + 1), "mensaje recibido, gracias");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newMessage(String idCliente, ChannelConnectionPoint ccp, byte canal, byte[] data) {
        System.out.println("Server1 - byte[] recibido de cliente " + idCliente + " por canal " + canal);
    }

    @Override
    public void channelFreed(String idCliente, ChannelConnectionPoint ccp, byte channel) {
        System.out.println("Channels freed " + channel);
    }

    public void newClientConnection(String idCliente, ChannelConnectionPoint channelConnectionPoint, IP4Port ip4Port) {
        System.out.println("Server1 - nuevo cliente conectado: " + idCliente + " (" + ip4Port.getIp() + ", " + ip4Port.getPort() + "). Informando a los clientes...");
        try {
            serverModule.writeAllBut((byte) 0, "Se ha conectado un nuevo cliente: " + idCliente, idCliente);
            serverModule.write(idCliente, (byte) 0, "Ahora estas conectado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientDisconnected(String idCliente, ChannelConnectionPoint ccp, boolean expected) {
        System.out.println("client disc: " + idCliente);
    }

    public void clientError(String idCliente, ChannelConnectionPoint channelConnectionPoint, CommError e) {
         System.out.println("serv error" + idCliente);
        //e.printStackTrace();
    }

    public void newConnectionError(Exception e, IP4Port ip4Port) {
        e.printStackTrace();
    }

    public void TCPServerError(Exception e) {
        System.out.println("TCP Server error");
        e.printStackTrace();
    }
}
