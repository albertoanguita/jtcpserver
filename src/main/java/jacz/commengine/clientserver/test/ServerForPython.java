package jacz.commengine.clientserver.test;

import jacz.commengine.clientserver.server.ServerModule;
import org.aanguita.jacuzzi.io.IOUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alberto on 16/10/2015.
 */
public class ServerForPython {

    public static void main(String[] args) throws IOException {


        ServerActionImpl1 serverActionImpl1 = new ServerActionImpl1();
        Set<Byte> allChannels = new HashSet<>();
        for (Byte channel = Byte.MIN_VALUE; channel < Byte.MAX_VALUE; channel++) {
            allChannels.add(channel);
        }
        allChannels.add(Byte.MAX_VALUE);
        Set<Set<Byte>> concurrentChannels = new HashSet<>();
        concurrentChannels.add(allChannels);

        ServerModule serverModule = new ServerModule(45000, serverActionImpl1, concurrentChannels);
        serverActionImpl1.setServerModule(serverModule);

        serverModule.startListeningConnections();

        IOUtil.pauseEnter("Press enter...");

    }
}
