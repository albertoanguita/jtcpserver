package jacz.commengine.clientserver.test.lightserver;

import jacz.commengine.clientserver.client.LightClient;
import jacz.commengine.clientserver.server.LightServer;
import org.aanguita.jacuzzi.concurrency.ThreadUtil;
import org.aanguita.jacuzzi.network.IP4Port;

import java.io.IOException;

/**
 * test
 */
public class LightServerTest {

    public static void main(String[] args) throws IOException {

        LightServer lightServer = new LightServer(55555, new LightServerActionObjectImpl(), true);
        lightServer.start();

        ThreadUtil.safeSleep(1000);

        try {
            LightClient.sendRequest(new IP4Port("127.0.0.1", 55555), "");
            //System.out.println((String) o);
            //byte[] answer = LightClient.sendRequest(new IP4Port("127.0.0.1", 55555), Serializer.serialize("Alberto"));
            //System.out.println(Serializer.deserializeString(answer, new MutableOffset()));

            lightServer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
