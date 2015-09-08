package jacz.commengine.tcpconnection.test;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Alberto
 * Date: 02-mar-2010
 * Time: 19:25:12
 * To change this template use File | Settings | File Templates.
 */
public class StrMsg implements Serializable {

    public String str;

    public StrMsg(String str) {
        this.str = str;
    }
}
