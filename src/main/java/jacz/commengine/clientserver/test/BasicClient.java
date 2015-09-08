package jacz.commengine.clientserver.test;

/**
 * Created with IntelliJ IDEA.
 * User: Alberto
 * Date: 13/05/14
 * Time: 17:14
 * To change this template use File | Settings | File Templates.
 */
public class BasicClient {

    public static void main(String[] args) {

        for (int i = 0; i < 1; i++) {

            String ip = "95.22.51.7";
//            String ip = "138.100.11.51";

            try {
                System.out.println("Trying to open hole...");
                int localPort = OpenHole.openHole("95.22.51.7", 64905, "138.100.11.51", 60000);
                System.out.println("hole was opened from local port: " + localPort);
                Thread.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Client end");
    }

}
