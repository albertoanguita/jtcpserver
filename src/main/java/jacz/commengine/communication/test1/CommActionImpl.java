package jacz.commengine.communication.test1;

import jacz.commengine.communication.CommError;
import jacz.commengine.communication.CommunicationAction;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 08-abr-2010
 * Time: 18:29:40
 * To change this template use File | Settings | File Templates.
 */
public class CommActionImpl implements CommunicationAction {

    private String name;

    public CommActionImpl(String name) {
        this.name = name;
    }

    public void stopped(boolean expected) {
        System.out.println("Communication module " + name + " has been stopped, expected = " + expected);
    }

    public void error(CommError error) {
        System.out.println("Communication module " + name + " has an error: " + error);
    }
}
