package org.aanguita.jtcpserver.channel.tests.test2;

import org.aanguita.jtcpserver.channel.ChannelConnectionPoint;
import org.aanguita.jtcpserver.channel.ChannelFSMAction;

/**
 *
 */
public class FSMClient implements ChannelFSMAction<FSMClient.FSMClientStates> {

    public enum FSMClientStates {
        NAME,
        BWTH,
        CONF,
        FINAL
    }


    private String version;

    private String clientName;

    private Integer clientBandwith;

    public FSMClient(String version, String clientName, Integer clientBandwith) {
        this.version = version;
        this.clientName = clientName;
        this.clientBandwith = clientBandwith;
    }

    public FSMClientStates processMessage(FSMClientStates currentState, byte channel, Object input, ChannelConnectionPoint cpp) throws IllegalArgumentException {

        System.out.println("Client: msg received. State: " + currentState);
        switch (currentState) {

            case BWTH:
                return caseBwth(input, cpp);
            case CONF:
                return caseConf(input, cpp);
            case FINAL:
                return FSMClientStates.FINAL;
            case NAME:
                return caseName(input, cpp);
        }
        try {
            throw new Exception("");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    private FSMClientStates caseName(Object msg, ChannelConnectionPoint cpp) {
        // expecting version confirmation from server
        if (!(msg instanceof String)) {
            // incorrect data from client --> inform client and disconnect
            System.out.println("Client: wrong data received. Disconnecting");
            cpp.write((byte) 0, "Wrong data");
            cpp.disconnect();
            return FSMClientStates.FINAL;
        }
        String serverNameConf = (String) msg;
        if (!serverNameConf.equals("Correct version")) {
            // incorrect version --> disconnect
            cpp.disconnect();
            return FSMClientStates.FINAL;
        }
        // correct version confirmation --> sending name
        cpp.write((byte) 0, clientName);
        return FSMClientStates.BWTH;
    }

    private FSMClientStates caseBwth(Object msg, ChannelConnectionPoint cpp) {
        // expecting name confirmation from server
        if (!(msg instanceof String)) {
            // incorrect data from client --> inform client and disconnect
            System.out.println("Client: wrong data received. Disconnecting");
            cpp.write((byte) 0, "Wrong data");
            cpp.disconnect();
            return FSMClientStates.FINAL;
        }
        String serverNameConf = (String) msg;
        if (!serverNameConf.equals("Name received")) {
            // incorrect version --> disconnect
            cpp.disconnect();
            return FSMClientStates.FINAL;
        }
        // correct name confirmation --> sending bandwith
        cpp.write((byte) 0, clientBandwith);
        return FSMClientStates.CONF;
    }

    private FSMClientStates caseConf(Object msg, ChannelConnectionPoint cpp) {
        // expecting bandwith confirmation from server
        if (!(msg instanceof String)) {
            // incorrect data from client --> inform client and disconnect
            System.out.println("Client: wrong data received. Disconnecting");
            cpp.write((byte) 0, "Wrong data");
            cpp.disconnect();
            return FSMClientStates.FINAL;
        }
        String serverNameConf = (String) msg;
        if (!serverNameConf.equals("Bandwith received")) {
            // incorrect version --> disconnect
            cpp.disconnect();
            return FSMClientStates.FINAL;
        }
        // correct bandwith confirmation
        return FSMClientStates.FINAL;
    }

    public FSMClientStates processMessage(FSMClientStates state, byte channel, byte[] data, ChannelConnectionPoint cpp) {
        // incorrect data from client --> inform client and disconnect
        System.out.println("Client: byte array not expected. Disconnecting");
        cpp.write((byte) 0, "Wrong data");
        cpp.disconnect();
        return FSMClientStates.FINAL;
    }

    public FSMClientStates init(ChannelConnectionPoint cpp) {
        cpp.write((byte) 0, version);
        return FSMClientStates.NAME;
    }

    public boolean isFinalState(FSMClientStates state, ChannelConnectionPoint cpp) {
        return state == FSMClientStates.FINAL;
    }

    @Override
    public void disconnected(ChannelConnectionPoint cpp) {
        System.out.println("FSM disconnected");
    }

    @Override
    public void raisedUnhandledException(Exception e, ChannelConnectionPoint cpp) {
        e.printStackTrace();
    }
}
