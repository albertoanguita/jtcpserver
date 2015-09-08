package jacz.commengine.channel.tests.test2;

import jacz.commengine.channel.ChannelConnectionPoint;
import jacz.commengine.channel.ChannelFSMAction;

/**
 *
 */
public class FSMServer implements ChannelFSMAction<FSMServer.FSMServerStates> {

    public enum FSMServerStates {
        INIT,
        NAME,
        BWTH,
        FINAL
    }

    private String version;

    private String clientName;

    private Integer clientBandwith;

    public FSMServer(String version) {
        this.version = version;
        clientName = null;
        clientBandwith = null;
    }

    public String getClientName() {
        return clientName;
    }

    public Integer getClientBandwidth() {
        return clientBandwith;
    }

    public FSMServerStates processMessage(FSMServerStates currentState, byte channel, Object input, ChannelConnectionPoint cpp) throws IllegalArgumentException {

        System.out.println("Server: msg received. State: " + currentState);
        switch (currentState) {

            case BWTH:
                return caseBwth(input, cpp);
            case FINAL:
                return FSMServerStates.FINAL;
            case INIT:
                return caseInit(input, cpp);
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

    public FSMServerStates processMessage(FSMServerStates currentState, byte channel, byte[] data, ChannelConnectionPoint cpp) throws IllegalArgumentException {
        // incorrect data from client --> inform client and disconnect
        System.out.println("Server: byte array not expected. Disconnecting");
        cpp.write((byte) 0, "Wrong data");
        cpp.disconnect();
        return FSMServerStates.FINAL;
    }

    private FSMServerStates caseInit(Object msg, ChannelConnectionPoint cpp) {
        // expecting version from client
        if (!(msg instanceof String)) {
            // incorrect data from client --> inform client and disconnect
            System.out.println("Server: wrong data received. Disconnecting");
            cpp.write((byte) 0, "Wrong data");
            cpp.disconnect();
            return FSMServerStates.FINAL;
        }
        String clientVersion = (String) msg;
        if (!clientVersion.equals(version)) {
            System.out.println("Server: client version not mathcing server version: " + clientVersion);
            // incorrect version --> disconnect
            cpp.write((byte) 0, "Required version: " + version);
            cpp.disconnect();
            return FSMServerStates.FINAL;
        }
        // correct version --> inform and wait for further data
        System.out.println("Server: correct client version");
        cpp.write((byte) 0, "Correct version");
        return FSMServerStates.NAME;
    }

    private FSMServerStates caseName(Object msg, ChannelConnectionPoint cpp) {
        // expecting name from client
        if (!(msg instanceof String)) {
            // incorrect data from client --> inform client and disconnect
            System.out.println("Server: wrong data received. Disconnecting");
            cpp.write((byte) 0, "Wrong data");
            cpp.disconnect();
            return FSMServerStates.FINAL;
        }
        this.clientName = (String) msg;
        System.out.println("Server: client name received: " + clientName);
        cpp.write((byte) 0, "Name received");
        return FSMServerStates.BWTH;
    }

    private FSMServerStates caseBwth(Object msg, ChannelConnectionPoint cpp) {
        // expecting bandwith from client
        if (!(msg instanceof Integer)) {
            // incorrect data from client --> inform client and disconnect
            System.out.println("Server: wrong data received. Disconnecting");
            cpp.write((byte) 0, "Wrong data");
            cpp.disconnect();
            return FSMServerStates.FINAL;
        }
        this.clientBandwith = (Integer) msg;
        System.out.println("Server: client bandwith received: " + clientBandwith);
        cpp.write((byte) 0, "Bandwith received");
        return FSMServerStates.FINAL;
    }

    public FSMServerStates init(ChannelConnectionPoint cpp) {
        return FSMServerStates.INIT;
    }

    public boolean isFinalState(FSMServerStates state, ChannelConnectionPoint cpp) {
        return state == FSMServerStates.FINAL;
    }

    @Override
    public void disconnected(ChannelConnectionPoint cpp) {
        System.out.println("FSM disconnected");
    }
}
